package dao

import akka.actor.ActorSystem
import javax.inject.Inject
import models.{BatchMeterUsageData, UsageRecord}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcBackend, JdbcProfile}
import slick.lifted.{ProvenShape, Rep, TableQuery, Tag}
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class Database @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
) {
  val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]
  val db: JdbcBackend#DatabaseDef = dbConfig.db

  class UsageTable(tag: Tag) extends Table[UsageRecord](tag, "USAGERECORDS") {
    def id =
      column[Option[Int]]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def reportedTimestamp = column[Option[Long]]("reportedTimestamp")
    def timestamp = column[Long]("timestamp")
    def customerId = column[String]("customerId")
    def dimension = column[String]("dimension")
    def quantity = column[Long]("quantity")
    // Every table needs a * projection with the same type as the table's type parameter
    override def * : ProvenShape[UsageRecord] =
      (id, reportedTimestamp, timestamp, customerId, dimension, quantity) <> (UsageRecord.tupled, UsageRecord.unapply)
  }
  val usageRecords = TableQuery[UsageTable]

  db.run(usageRecords.schema.create)

  def storeBatchMetering(data: BatchMeterUsageData): Unit = {
    db.run(
      usageRecords ++=
        data.UsageRecords
          .map(
            _.copy(reportedTimestamp = Some(System.currentTimeMillis))
          )
    )
  }

  def getBatchMeteringItems(customerId: String, timestampFrom: Long, timestampTo: Long): Future[Seq[UsageRecord]] = {
    return db.run(
      usageRecords
        .filter(_.customerId === customerId)
        .filter(_.reportedTimestamp <= timestampTo)
        .filter(_.reportedTimestamp >= timestampFrom)
        .result)
  }

  def listBatchMeteringItems(): Future[Seq[UsageRecord]] = {
    return db.run(usageRecords.result)
  }

  def cleanup() = {
    // entry ttl: 30 secs

    db.run(
      usageRecords.filter(_.reportedTimestamp < (System.currentTimeMillis()) - 30 * 1000).delete
    )
  }
}

class CleanupTask @Inject()(actorSystem: ActorSystem, database: Database)(implicit executionContext: ExecutionContext) {
  actorSystem.scheduler.schedule(initialDelay = 10.minutes, interval = 30.minutes) {
    database.cleanup
  }
}
