package org.hyperledger.identus.mediator.db

import reactivemongo.api.MongoConnection.ParsedURIWithDB
import reactivemongo.api.{AsyncDriver, DB, MongoConnection}
import zio.{Task, TaskLayer, ZIO, ZLayer}

import scala.concurrent.duration.DurationInt

trait ReactiveMongoApi {
  def driver: AsyncDriver
  def connection: MongoConnection
  def database: Task[DB]
}

case class ReactiveMongoLive(
    asyncDriver: AsyncDriver,
    mongoParsedUri: ParsedURIWithDB,
    mongoConnection: MongoConnection
) extends ReactiveMongoApi {

  lazy val driver: AsyncDriver = asyncDriver

  lazy val connection: MongoConnection = mongoConnection

  def database: Task[DB] = ZIO.fromFuture(implicit ec => connection.database(mongoParsedUri.db))

}

object ReactiveMongoApi {

  private def acquire(connectionString: String) = (
    for {
      asyncDriver <- ZIO.service[AsyncDriver]
      mongoParsedUri <- ZIO.fromFuture(implicit ec => MongoConnection.fromStringWithDB(connectionString))
      connection <- ZIO.fromFuture(_ =>
        asyncDriver.connect(mongoParsedUri, Some(mongoParsedUri.db), strictMode = false)
      )
      reactiveMongo = ReactiveMongoLive(asyncDriver, mongoParsedUri, connection)
    } yield reactiveMongo
  ).uninterruptible

  private def release(api: ReactiveMongoApi) = ZIO
    .fromFuture(_ => api.connection.close()(using 10.seconds))
    .orDie
    .unit

  def layer(
      connectionString: String
  ): ZLayer[AsyncDriver, Throwable, ReactiveMongoApi] =
    ZLayer.scoped(
      ZIO.acquireRelease(acquire(connectionString))(release(_))
    )
}
