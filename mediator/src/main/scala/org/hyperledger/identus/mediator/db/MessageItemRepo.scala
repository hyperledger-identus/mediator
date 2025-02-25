package org.hyperledger.identus.mediator.db

import fmgp.did.*
import fmgp.did.comm.{EncryptedMessage, SignedMessage}
import org.hyperledger.identus.mediator.db.MessageType.Mediator
import org.hyperledger.identus.mediator.{DuplicateMessage, StorageCollection, StorageError, StorageThrowable}
import reactivemongo.api.bson.*
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, CursorProducer}
import zio.*
import reactivemongo.core.errors.DatabaseException

import scala.concurrent.ExecutionContext

object MessageItemRepo {
  def layer: ZLayer[ReactiveMongoApi, Throwable, MessageItemRepo] =
    ZLayer {
      for {
        ref <- ZIO.service[ReactiveMongoApi]
      } yield MessageItemRepo(ref)(using scala.concurrent.ExecutionContext.global)
    }
}

class MessageItemRepo(reactiveMongoApi: ReactiveMongoApi)(using ec: ExecutionContext) extends MongoHealth {
  def collectionName: String = "messages"

  def collection: IO[StorageCollection, BSONCollection] = reactiveMongoApi.database
    .map(_.collection(collectionName))
    .mapError(ex => StorageCollection(ex))

  def insert(
      msg: SignedMessage | EncryptedMessage,
      messageType: MessageType = Mediator
  ): IO[StorageError, WriteResult] = {
    for {
      _ <- ZIO.logInfo(s"insert $messageType")
      xRequestId <- ZIO.logAnnotations.map(_.get(XRequestId.value))
      coll <- collection
      result <- ZIO
        .fromFuture(implicit ec => coll.insert.one(MessageItem(msg, messageType, xRequestId)))
        .tapError(err => ZIO.logError(s"insert :  ${err.getMessage}"))
        .mapError {
          case ex: DatabaseException if (ex.code.contains(DuplicateMessage.code)) => DuplicateMessage(ex)
          case ex                                                                 => StorageThrowable(ex)
        }
    } yield result
  }

  def findById(id: HASH): IO[StorageError, Option[MessageItem]] = {
    def selector: BSONDocument = BSONDocument("_id" -> id)
    def projection: Option[BSONDocument] = None

    for {
      _ <- ZIO.logInfo("findById")
      coll <- collection
      result <- ZIO
        .fromFuture(implicit ec =>
          coll
            .find(selector, projection)
            .cursor[MessageItem]()
            .collect[Seq](1, Cursor.FailOnError[Seq[MessageItem]]())
        )
        .tapError(err => ZIO.logError(s"findById :  ${err.getMessage}"))
        .mapError(ex => StorageThrowable(ex))

    } yield result.headOption

  }

  def findByIds(ids: Seq[HASH]): IO[StorageError, Seq[MessageItem]] = {
    def selector: BSONDocument = {
      BSONDocument("_id" -> BSONDocument("$in" -> ids))
    }
    def projection: Option[BSONDocument] = None

    for {
      _ <- ZIO.logInfo("findByIds")
      coll <- collection
      result <- ZIO
        .fromFuture(implicit ec =>
          coll
            .find(selector, projection)
            .cursor[MessageItem]()
            .collect[Seq](-1, Cursor.FailOnError[Seq[MessageItem]]())
        )
        .tapError(err => ZIO.logError(s"findByIds :  ${err.getMessage}"))
        .mapError(ex => StorageThrowable(ex))
    } yield result

  }

}
