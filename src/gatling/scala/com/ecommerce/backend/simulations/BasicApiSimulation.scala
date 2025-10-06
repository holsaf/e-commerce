package com.ecommerce.backend.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicApiSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")

  val basicTest = scenario("Basic API Test")
    .exec(
      http("Health Check")
        .get("/actuator/health")
        .check(status.is(200))
    )
    .pause(1.second)
    .exec(
      http("Get Products")
        .get("/api/products")
        .check(status.is(200))
    )
    .pause(1.second)
    .exec(
      http("Get Swagger Docs")
        .get("/v3/api-docs")
        .check(status.is(200))
    )

  setUp(
    basicTest.inject(
      atOnceUsers(1),
      rampUsers(5) during (10.seconds)
    )
  ).protocols(httpProtocol)
}