package io.iohk.atala.mediator.protocols

import fmgp.crypto.error.*
import fmgp.did.*
import fmgp.did.comm.*
import fmgp.did.comm.protocol.*
import fmgp.did.comm.protocol.routing2.*
import fmgp.did.comm.protocol.reportproblem2.ProblemReport
import io.iohk.atala.mediator.*
import io.iohk.atala.mediator.db.*
import zio.*
import zio.json.*

object ForwardMessageExecuter
    extends ProtocolExecuter[Agent & UserAccountRepo & MessageItemRepo, MediatorError | StorageError] {

  override def supportedPIURI: Seq[PIURI] = Seq(ForwardMessage.piuri)

  override def program(plaintextMessage: PlaintextMessage) = {
    // the val is from the match to be definitely stable
    val piuriForwardMessage = ForwardMessage.piuri

    (plaintextMessage.`type` match { case `piuriForwardMessage` => plaintextMessage.toForwardMessage }) match
      case Left(error) => ZIO.logError(error) *> ZIO.succeed(NoReply)
      case Right(m: ForwardMessage) =>
        for {
          _ <- ZIO.logInfo("ForwardMessage")
          repoMessageItem <- ZIO.service[MessageItemRepo]
          repoDidAccount <- ZIO.service[UserAccountRepo]
          recipientsSubject = Set(m.next) // m.msg.recipientsSubject
          numbreOfUpdated <- repoDidAccount.addToInboxes(recipientsSubject, m.msg)
          msg <-
            if (numbreOfUpdated > 0) { // Or maybe we can add all the time
              for {
                _ <- repoMessageItem.insert(m.msg)
                _ <- ZIO.logInfo("Add next msg (of the ForwardMessage) to the Message Repo")
              } yield NoReply
            } else {
              for {
                _ <- ZIO.logWarning("Note: No update on the DidAccount of the recipients")
                agent <- ZIO.service[Agent]
                problem = plaintextMessage.from match {
                  case Some(to) =>
                    Problems.notEnroledError(
                      to = Some(to.asTO),
                      from = agent.id,
                      pthid = plaintextMessage.id,
                      piuri = plaintextMessage.`type`,
                      didNotEnrolled = m.next,
                    )
                  case None =>
                    Problems.notEnroledError(
                      to = None,
                      from = agent.id,
                      pthid = plaintextMessage.id,
                      piuri = plaintextMessage.`type`,
                      didNotEnrolled = m.next,
                    )
                }
              } yield Reply(problem.toPlaintextMessage)
            }
        } yield msg
  }

}
