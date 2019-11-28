package controllers

import javax.inject._
import dao.Database
import models._
import play.api._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import models.Implicits._
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class MeteringMarketplace @Inject()(cc: ControllerComponents, database: Database)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def apiHandler() = Action(parse.tolerantJson) { implicit request: Request[JsValue] =>
    request.headers.get("X-Amz-Target") match {
      case Some("AWSMPMeteringService.BatchMeterUsage") =>
        Json
          .fromJson[BatchMeterUsageData](request.body)
          .fold(_ => BadRequest, batchMeterUsage)
      case Some("AWSMPMeteringService.ResolveCustomer") =>
        Json
          .fromJson[ResolveCustomerData](request.body)
          .fold(_ => BadRequest, resolveCustomer)
      case _ => NotImplemented
    }
  }

  def batchMeterUsage(data: BatchMeterUsageData): Result = {
    database.storeBatchMetering(data)
    Ok(
      Json.toJson(
        BatchMeterUsageResponseData(
          data.UsageRecords.map(record => UsageResult(Random.alphanumeric.take(20).mkString, "Success", record)),
          List()
        )
      ))
  }

  def resolveCustomer(data: ResolveCustomerData): Result = {
    Ok(
      Json.toJson(ResolveCustomerResponseData(Random.alphanumeric.take(11).mkString, "product_code"))
    )
  }

  def getResults(customerId: String, timestampFrom: String, timestampTo: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      try {
        val timestampFromEpoch: Long = DateTime
          .parse(timestampFrom)
          .getMillis

        val timestampToEpoch: Long = DateTime
          .parse(timestampTo)
          .getMillis

        database
          .getBatchMeteringItems(customerId, timestampFromEpoch, timestampToEpoch)
          .map(res => Ok(Json.toJson(res)))
      } catch {
        case e: IllegalArgumentException => Future(BadRequest)
        case _: Throwable => Future(InternalServerError)
      }
  }

  def listResults(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    database.listBatchMeteringItems.map(res => Ok(Json.toJson(res)))
  }

  def cleanup() = Action.async { implicit request =>
    database.cleanup.map(x => Ok(x.toString))
  }
}
