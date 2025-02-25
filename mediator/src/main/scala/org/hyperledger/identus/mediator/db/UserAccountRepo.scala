package org.hyperledger.identus.mediator.db

import fmgp.crypto.error.*
import fmgp.did.*
import fmgp.did.comm.*
import org.hyperledger.identus.mediator.*
import reactivemongo.api.bson.*
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, CursorProducer}
import zio.*

import scala.concurrent.ExecutionContext

object UserAccountRepo {
  def layer: ZLayer[ReactiveMongoApi, Throwable, UserAccountRepo] =
    ZLayer {
      for {
        ref <- ZIO.service[ReactiveMongoApi]
      } yield UserAccountRepo(ref)(using scala.concurrent.ExecutionContext.global)
    }
}

class UserAccountRepo(reactiveMongoApi: ReactiveMongoApi)(using ec: ExecutionContext) extends MongoHealth {
  def collectionName: String = "user.account"

  def collection: IO[StorageCollection, BSONCollection] = reactiveMongoApi.database
    .map(_.collection(collectionName))
    .tapError(err => ZIO.logError(s"Couldn't get collection ${err.getMessage}"))
    .mapError(ex => StorageCollection(ex))

  /** create or return account for a  DIDSubject */
  def createOrFindDidAccount(did: DIDSubject): IO[StorageError, Either[String, DidAccount]] = {
    def projection: Option[BSONDocument] = None
    def selectorConditionToInsert = BSONDocument(
      Seq(
        "$or" -> BSONArray(
          BSONDocument(Seq("did" -> BSONString(did.did))),
          BSONDocument(Seq("alias" -> BSONString(did.did))) // TODO test
        )
      )
    )

    for {
      _ <- ZIO.logInfo("newDidAccount")
      coll <- collection
      findR <- ZIO // TODO this should be atomic
        .fromFuture(implicit ec =>
          coll
            .find(selectorConditionToInsert, projection)
            .cursor[DidAccount]()
            .collect[Seq](1, Cursor.FailOnError[Seq[DidAccount]]()) // Just one
            .map(_.headOption)
        )
        .tapError(err => ZIO.logError(s"Insert newDidAccount (check condition step):  ${err.getMessage}"))
        .mapError(ex => StorageThrowable(ex))
      result <- findR match
        case Some(data) if data.did != did => ZIO.left(s"Fail found document: $data")
        case Some(old)                     => ZIO.right(old)
        case None =>
          val value = DidAccount(
            did = did,
            alias = Seq.empty,
            messagesRef = Seq.empty
          )
          ZIO
            .fromFuture(implicit ec => coll.insert.one(value))
            .map(e =>
              e.n match
                case 1 => Right(value)
                case _ => Left(s"Fail to insert: ${e.toString}")
            )
            .tapError(err => ZIO.logError(s"Insert newDidAccount :  ${err.getMessage}"))
            .mapError(ex => StorageThrowable(ex))
    } yield result
  }

  def getDidAccountFromAlias(alias: DIDSubject): IO[StorageError, Option[DidAccount]] = {
    def selector: BSONDocument = BSONDocument("alias" -> alias.did) // BSONDocument("$in" -> BSONArray(alias.did)))
    def projection: Option[BSONDocument] = None

    for {
      _ <- ZIO.logInfo("getDidAccountFromAlias")
      coll <- collection
      result <- ZIO
        .fromFuture(implicit ec =>
          coll
            .find(selector, projection)
            .cursor[DidAccount]()
            .collect[Seq](1, Cursor.FailOnError[Seq[DidAccount]]()) // Just one
        )
        .tapError(err => ZIO.logError(s"getDidAccount :  ${err.getMessage}"))
        .mapError(ex => StorageThrowable(ex))
    } yield result.headOption
  }

  def getDidAccount(did: DIDSubject): IO[StorageError, Option[DidAccount]] = {
    def selector: BSONDocument = BSONDocument("did" -> did)
    def projection: Option[BSONDocument] = None

    for {
      _ <- ZIO.logInfo("getDidAccount")
      coll <- collection
      result <- ZIO
        .fromFuture(implicit ec =>
          coll
            .find(selector, projection)
            .cursor[DidAccount]()
            .collect[Seq](1, Cursor.FailOnError[Seq[DidAccount]]()) // Just one
        )
        .tapError(err => ZIO.logError(s"getDidAccount :  ${err.getMessage}"))
        .mapError(ex => StorageThrowable(ex))
    } yield result.headOption
  }

  def addAlias(owner: DIDSubject, newAlias: DIDSubject): ZIO[Any, StorageError, Either[String, Int]] = {
    def selector: BSONDocument = BSONDocument("did" -> owner)

    def update: BSONDocument = BSONDocument(
      "$addToSet" -> BSONDocument(
        "alias" -> newAlias
      )
    )

    for {
      _ <- ZIO.logInfo("addAlias")
      coll <- collection
      result <- ZIO
        .fromFuture(implicit ec =>
          coll.update
            .one(selector, update) // Just one
        )
        .tapError(err => ZIO.logError(s"addAlias :  ${err.getMessage}"))
        .mapError(ex => StorageThrowable(ex))
    } yield Right(result.nModified)

  }

  def removeAlias(owner: DIDSubject, newAlias: DIDSubject): ZIO[Any, StorageError, Either[String, Int]] = {
    def selector: BSONDocument = BSONDocument("did" -> owner)
    def update: BSONDocument = BSONDocument(
      "$pull" -> BSONDocument(
        "alias" -> newAlias
      )
    )

    for {
      _ <- ZIO.logInfo("removeAlias")
      coll <- collection
      result <- ZIO
        .fromFuture(implicit ec =>
          coll.update
            .one(selector, update) // Just one
        )
        .tapError(err => ZIO.logError(s"removeAlias :  ${err.getMessage}"))
        .mapError(ex => StorageThrowable(ex))
    } yield Right(result.nModified)

  }

  /** @return
    *   number of documents updated in DB
    */
  def addToInboxes(
      recipients: Set[DIDSubject],
      msg: SignedMessage | EncryptedMessage
  ): ZIO[Any, StorageError, Int] = {
    def selector =
      BSONDocument(
        "alias" -> BSONDocument("$in" -> recipients.map(_.did)),
        "messagesRef" ->
          BSONDocument(
            "$not" ->
              BSONDocument(
                "$elemMatch" ->
                  BSONDocument(
                    "hash" -> msg.sha256,
                    "recipient" -> BSONDocument("$in" -> recipients.map(_.did))
                  )
              )
          )
      )

    def update(xRequestId: Option[XRequestID]): BSONDocument = BSONDocument(
      "$push" -> BSONDocument(
        "messagesRef" -> BSONDocument(
          "$each" ->
            recipients.map(recipient => MessageMetaData(msg.sha256, recipient, xRequestId))
        )
      )
    )

    for {
      _ <- ZIO.logInfo("addToInboxes")
      xRequestId <- ZIO.logAnnotations.map(_.get(XRequestId.value))
      coll <- collection
      result <- ZIO
        .fromFuture(implicit ec =>
          coll.update
            .one(selector, update(xRequestId)) // Just one
        )
        .tapError(err => ZIO.logError(s"addToInboxes :  ${err.getMessage}"))
        .mapError(ex => StorageThrowable(ex))
    } yield result.nModified

  }

  def markAsDelivered(didAccount: DIDSubject, hashes: Seq[HASH]): ZIO[Any, StorageError, Int] = {
    def selector = BSONDocument("did" -> didAccount.did, "messagesRef.hash" -> BSONDocument("$in" -> hashes))
    def update: BSONDocument = BSONDocument("$set" -> BSONDocument("messagesRef.$.state" -> true))

    for {
      _ <- ZIO.logInfo("markAsDelivered")
      coll <- collection
      result <- ZIO
        .fromFuture(implicit ec => coll.update.one(selector, update)) // Just one
        .tapError(err => ZIO.logError(s"markAsDelivered :  ${err.getMessage}"))
        .mapError(ex => StorageThrowable(ex))
    } yield result.nModified
  }
}
