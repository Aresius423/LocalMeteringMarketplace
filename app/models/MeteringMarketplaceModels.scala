package models

import play.api.libs.json._

case class UsageRecord(
  id: Option[Int] = None,
  reportedTimestamp: Option[Long] = None,
  Timestamp: Long,
  CustomerIdentifier: String,
  Dimension: String,
  Quantity: Long
)

case class BatchMeterUsageData(
  ProductCode: String,
  UsageRecords: List[UsageRecord]
)

case class UsageResult(
  MeteringRecordId: String,
  Status: String,
  UsageRecord: UsageRecord
)

case class BatchMeterUsageResponseData(
  Results: List[UsageResult],
  UnprocessedRecords: List[UsageRecord]
)

case class ResolveCustomerData(
  RegistrationToken: String
)

case class ResolveCustomerResponseData(
  CustomerIdentifier: String,
  ProductCode: String
)
