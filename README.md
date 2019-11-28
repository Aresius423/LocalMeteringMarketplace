# LocalMeteringMarketplace
A simple webapp for AWS MeteringMarketplace integration testing

# Setup

There are no external dependencies. Run simply with ```sbt run```
    
# Usage

## Supported endpoints

The currently supported endpoints are:
  - [batch-meter-usage](https://docs.aws.amazon.com/cli/latest/reference/meteringmarketplace/batch-meter-usage.html)
  - [resolve-customer](https://docs.aws.amazon.com/cli/latest/reference/meteringmarketplace/resolve-customer.html)
  
To test your application, configure it to use localhost:9000/meteringmarketplace as the AWS API endpoint.

## Retrieving test data

### localhost:9000/data/meteringmarketplace/list

Calling GET on this endpoint will return a dump of the metering requests received.  
  
### localhost:9000/data/meteringmarketplace/:customerId/:timestampFrom/:timestampTo

Calling GET on this endpoint will return a dump of the metering requests received with the specified customerId, between the specified timestamps.  
Timestamps are expected to be in a string format that is understandable by Datetime.parse, preferably ```%Y-%m-%dT%H:%M:%S.%fZ```.
