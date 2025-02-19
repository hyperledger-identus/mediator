package org.hyperledger.identus.mediator.db

import zio.*

import reactivemongo.api.CollectionStats
import reactivemongo.api.bson.collection.BSONCollection
import org.hyperledger.identus.mediator.{StorageCollection, StorageThrowable, StorageError}

trait MongoHealth {
  def collection: IO[StorageCollection, BSONCollection]
  def stats: IO[StorageError, CollectionStats] =
    collection.flatMap(c => ZIO.fromFuture(c.stats()).mapError(ex => StorageThrowable(ex)))
}
