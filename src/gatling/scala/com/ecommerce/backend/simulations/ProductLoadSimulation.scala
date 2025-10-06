package com.ecommerce.backend.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class ProductLoadSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")

  val getProducts = scenario("Get Products")
    .exec(
      http("Get All Products")
        .get("/api/products")
        .check(status.is(200))
    )

  setUp(
    getProducts.inject(atOnceUsers(1000))
  ).protocols(httpProtocol)
}