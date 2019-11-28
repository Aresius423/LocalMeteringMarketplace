package models

import play.api.libs.json.{JsValue, Json, Reads}

object Implicits {
  implicit val usageRecordFormat = Json.format[UsageRecord]
  implicit val usageResultFormat = Json.format[UsageResult]
  implicit val batchMeterUsageDataFormat = Json.format[BatchMeterUsageData]
  implicit val batchMeterUsageResponseDataFormat = Json.format[BatchMeterUsageResponseData]

  implicit val resolveCustomerDataFormat = Json.format[ResolveCustomerData]
  implicit val resolveCustomerResponseDataFormat = Json.format[ResolveCustomerResponseData]
}
