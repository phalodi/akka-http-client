package org.phalodi.queing

import akka.http.scaladsl.model.StatusCodes
import org.phalodi.queing.client.{APIRequest, QueuingServiceClient}
import org.scalatest.{BeforeAndAfterAll, WordSpec}

import scala.concurrent.{Await, TimeoutException}
import scala.concurrent.duration._


class QueuingServiceClientSpec extends WordSpec with BeforeAndAfterAll {

  val server = new ServerStart

  override def beforeAll(): Unit = {
    server.startServer
  }

  val queueingClient = new QueuingServiceClient

  "Queueing Client" should {

    "Call API for clients request" in {
      val res = Await.result(queueingClient.serviceClient(List(APIRequest("clients", List("123456782", "123456789")))), 10 seconds)
      assert(res.map(_.status) === List(StatusCodes.OK))
    }
  }

  "Call API for order request" in {
    val res = Await.result(queueingClient.serviceClient(List(APIRequest("order", List("123456782", "123456780")))), 10 seconds)
    assert(res.map(_.status) === List(StatusCodes.OK))
  }

  "Call API for pricing request" in {
    val res = Await.result(queueingClient.serviceClient(List(APIRequest("pricing", List("NL", "IN")))), 10 seconds)
    assert(res.map(_.status) === List(StatusCodes.OK))
  }


  "Call API for pricing,order and clients request" in {
    val res = Await.result(queueingClient.serviceClient(List(APIRequest("clients", List("123456782", "123456789")),
      APIRequest("order", List("123456782", "123456780")),
      APIRequest("pricing", List("NL", "IN")))), 10 seconds)

    assert(res.map(_.status) === List(StatusCodes.OK, StatusCodes.OK, StatusCodes.OK))
  }

  "Queue hold the api requests for 5 second if not reach cap of 5 request" in {
    intercept[TimeoutException] {
      Await.result(queueingClient.serviceClient(List(APIRequest("clients", List("123456782", "123456789")),
        APIRequest("clients", List("123456782", "123456780")))), 4 seconds)
    }
  }

  "Queue sent all the request when reach at cap of 5 requests" in {

    val res = Await.result(queueingClient.serviceClient(List(APIRequest("clients", List("123456782", "123456789")),
      APIRequest("clients", List("123456782", "123456789")),
      APIRequest("clients", List("123456782", "123456789")),
      APIRequest("clients", List("123456782", "123456789")),
      APIRequest("clients", List("123456782", "123456789")),
      APIRequest("clients", List("123456782", "123456789")))), 4 seconds)
    assert(res.map(_.status) === List(StatusCodes.OK, StatusCodes.OK, StatusCodes.OK,StatusCodes.OK,StatusCodes.OK,StatusCodes.OK))
  }

}
