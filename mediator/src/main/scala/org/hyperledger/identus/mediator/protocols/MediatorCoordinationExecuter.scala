package org.hyperledger.identus.mediator.protocols

import fmgp.crypto.error.*
import fmgp.did.*
import fmgp.did.comm.*
import fmgp.did.comm.Operations.*
import fmgp.did.comm.protocol.*
import fmgp.did.comm.protocol.mediatorcoordination2.*
import org.hyperledger.identus.mediator.*
import org.hyperledger.identus.mediator.db.UserAccountRepo
import org.hyperledger.identus.mediator.db.DidAccount

import zio.*
import zio.json.*
import org.hyperledger.identus.mediator.db.MessageItemRepo

object MediatorCoordinationExecuter extends ProtocolExecuter[Agent & UserAccountRepo, MediatorError | StorageError] {

  override def supportedPIURI: Seq[PIURI] = Seq(
    MediateRequest.piuri,
    MediateGrant.piuri,
    MediateDeny.piuri,
    KeylistUpdate.piuri,
    KeylistResponse.piuri,
    KeylistQuery.piuri,
    Keylist.piuri,
  )

  override def program(plaintextMessage: PlaintextMessage) = {
    // the val is from the match to be definitely stable
    val piuriMediateRequest = MediateRequest.piuri
    val piuriMediateGrant = MediateGrant.piuri
    val piuriMediateDeny = MediateDeny.piuri
    val piuriKeylistUpdate = KeylistUpdate.piuri
    val piuriKeylistResponse = KeylistResponse.piuri
    val piuriKeylistQuery = KeylistQuery.piuri
    val piuriKeylist = Keylist.piuri

    (plaintextMessage.`type` match {
      case `piuriMediateRequest`  => plaintextMessage.toMediateRequest
      case `piuriMediateGrant`    => plaintextMessage.toMediateGrant
      case `piuriMediateDeny`     => plaintextMessage.toMediateDeny
      case `piuriKeylistUpdate`   => plaintextMessage.toKeylistUpdate
      case `piuriKeylistResponse` => plaintextMessage.toKeylistResponse
      case `piuriKeylistQuery`    => plaintextMessage.toKeylistQuery
      case `piuriKeylist`         => plaintextMessage.toKeylist
    }).map {
      case m: MediateGrant =>
        ZIO.logWarning("MediateGrant") *> ZIO.succeed(NoReply) *>
          ZIO.succeed(
            Reply(
              Problems
                .unsupportedProtocolRole(
                  from = m.to.asFROM,
                  to = m.from.asTO,
                  pthid = m.id, // TODO CHECK pthid
                  piuri = m.piuri,
                )
                .toPlaintextMessage
            )
          )
      case m: MediateDeny =>
        ZIO.logWarning("MediateDeny") *> ZIO.succeed(NoReply) *>
          ZIO.succeed(
            Reply(
              Problems
                .unsupportedProtocolRole(
                  from = m.to.asFROM,
                  to = m.from.asTO,
                  pthid = m.id, // TODO CHECK pthid
                  piuri = m.piuri,
                )
                .toPlaintextMessage
            )
          )
      case m: MediateRequest =>
        for {
          _ <- ZIO.logInfo("MediateRequest")
          repo <- ZIO.service[UserAccountRepo]
          result: Either[String, DidAccount] <- repo.createOrFindDidAccount(m.from.asDIDURL.toDID)
          reply <- result match
            case Left(errorStr) =>
              ZIO.log(s"MediateDeny: $errorStr") *> ZIO.succeed(m.makeRespondMediateDeny.toPlaintextMessage)
            case Right(value) =>
              ZIO.log(s"MediateGrant: $value") *>
                ZIO.succeed(m.makeRespondMediateGrant.toPlaintextMessage)
        } yield Reply(reply)
      case m: KeylistUpdate =>
        for {
          _ <- ZIO.logInfo("KeylistUpdate")
          didRequestingKeyListUpdate = m.from.asFROMTO
          repo <- ZIO.service[UserAccountRepo]
          mayBeDidAccount <- repo.getDidAccount(didRequestingKeyListUpdate.toDID)
          res <-
            mayBeDidAccount match
              case None =>
                ZIO.succeed(
                  Problems
                    .notEnroledError(
                      from = m.to.asFROM,
                      to = Some(m.from.asTO),
                      pthid = m.id, // TODO CHECK pthid
                      piuri = m.piuri,
                      didNotEnrolled = didRequestingKeyListUpdate.asFROM.toDIDSubject,
                    )
                    .toPlaintextMessage
                )
              case Some(didAccount) =>
                for {
                  updateResponse <- ZIO.foreach(m.updates) {
                    case (fromto, KeylistAction.add) =>
                      repo.addAlias(m.from.toDID, fromto.toDID).map {
                        case Left(value)     => (fromto, KeylistAction.add, KeylistResult.server_error)
                        case Right(0)        => (fromto, KeylistAction.add, KeylistResult.no_change)
                        case Right(newState) => (fromto, KeylistAction.add, KeylistResult.success)
                      }
                    case (fromto, KeylistAction.remove) =>
                      repo.removeAlias(m.from.toDID, fromto.toDID).map {
                        case Left(value)     => (fromto, KeylistAction.remove, KeylistResult.server_error)
                        case Right(0)        => (fromto, KeylistAction.remove, KeylistResult.no_change)
                        case Right(newState) => (fromto, KeylistAction.remove, KeylistResult.success)
                      }
                  }
                  result <- ZIO.succeed(m.makeKeylistResponse(updateResponse).toPlaintextMessage)
                } yield result

        } yield Reply(res)
      case m: KeylistResponse =>
        ZIO.logWarning("KeylistResponse") *> ZIO.succeed(NoReply) *>
          ZIO.succeed(
            Reply(
              Problems
                .unsupportedProtocolRole(
                  from = m.to.asFROM,
                  to = m.from.asTO,
                  pthid = m.id, // TODO CHECK pthid
                  piuri = m.piuri,
                )
                .toPlaintextMessage
            )
          )
      case m: KeylistQuery =>
        for {
          _ <- ZIO.logInfo("KeylistQuery")
          repo <- ZIO.service[UserAccountRepo]
          mAccount <- repo.getDidAccount(m.from.toDID)
          mResponse = mAccount.map { account =>
            Keylist(
              thid = m.id,
              from = m.to.asFROM,
              to = m.from.asTO,
              keys = account.alias.map(e => Keylist.RecipientDID(e)),
              pagination = None,
            )
          }
        } yield mResponse match
          case None           => NoReply // TODO error report
          case Some(response) => Reply(response.toPlaintextMessage)
      case m: Keylist => ZIO.logWarning("Keylist") *> ZIO.succeed(NoReply)
    } match
      case Left(error)    => ZIO.logError(error) *> ZIO.succeed(NoReply) // TODO error report
      case Right(program) => program
  }

}
