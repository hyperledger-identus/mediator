package org.hyperledger.identus.mediator.db

import zio.*

import reactivemongo.api.CollectionStats
import reactivemongo.api.bson.collection.BSONCollection
import org.hyperledger.identus.mediator.StorageCollection

trait MongoHealth {
  def collection: IO[StorageCollection, BSONCollection]
  def stats: IO[StorageCollection, CollectionStats] =
    collection.flatMap(c => ZIO.fromFuture(c.stats()).mapError(ex => StorageCollection(ex)))
}
