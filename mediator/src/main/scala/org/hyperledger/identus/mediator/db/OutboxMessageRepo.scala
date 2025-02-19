package org.hyperledger.identus.mediator.db

import fmgp.did.*
import org.hyperledger.identus.mediator.{StorageCollection, StorageError, StorageThrowable}
import reactivemongo.api.bson.*
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, CursorProducer}
import zio.*

import scala.concurrent.ExecutionContext
object OutboxMessageRepo {
  def layer: ZLayer[ReactiveMongoApi, Throwable, OutboxMessageRepo] =
    ZLayer {
      for {
        ref <- ZIO.service[ReactiveMongoApi]
      } yield OutboxMessageRepo(ref)(using scala.concurrent.ExecutionContext.global)
    }
}

class OutboxMessageRepo(reactiveMongoApi: ReactiveMongoApi)(using ec: ExecutionContext) extends MongoHealth {
  def collectionName: String = "messages.outbound"

  def collection: IO[StorageCollection, BSONCollection] = reactiveMongoApi.database
    .map(_.collection(collectionName))
    .mapError(ex => StorageCollection(ex))

  def insert(value: SentMessageItem): IO[StorageError, WriteResult] = {
    for {
      _ <- ZIO.logInfo("insert")
      coll <- collection
      result <- ZIO
        .fromFuture(implicit ec => coll.insert.one(value))
        .tapError(err => ZIO.logError(s"insert :  ${err.getMessage}"))
        .mapError(ex => StorageThrowable(ex))
    } yield result
  }

}
