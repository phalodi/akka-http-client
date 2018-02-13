package org.phalodi.queing.client

import java.util.concurrent.TimeUnit

import scala.util.{Failure, Success, Try}
import scala.concurrent.{Await, Future, Promise}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, OverflowStrategy, QueueOfferResult, ThrottleMode}
import akka.stream.scaladsl._

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class APIRequest(identifier: String, parameters: List[String])


trait ClientImplicits {
  implicit val system = ActorSystem()

  import system.dispatcher

  implicit val materializer = ActorMaterializer()

}

class QueuingServiceClient {

  val clientQueue = new ClientQueue
  val pricingQueue = new PricingQueue
  val orderQueue = new OrderQueue

  def serviceClient(aPIRequest: List[APIRequest]): Future[List[HttpResponse]] = {

    Future.sequence(aPIRequest.map { api =>
      val uri = Uri(s"/${api.identifier}").withQuery(Query("q" -> api.parameters.mkString(",")))
      api.identifier match {
        case "clients" => clientQueue.queueRequest(HttpRequest(uri = uri))
        case "order" => orderQueue.queueRequest(HttpRequest(uri = uri))
        case "pricing" => orderQueue.queueRequest(HttpRequest(uri = uri))
      }
    })
  }

}


class ClientQueue extends ClientImplicits {

  val QueueSize = 4

  val poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpResponse]](host = "localhost", port = 8080)
  val queue =
    Source.queue[(HttpRequest, Promise[HttpResponse])](QueueSize, OverflowStrategy.backpressure)
      .via(poolClientFlow).groupedWithin(4, FiniteDuration(5, TimeUnit.SECONDS))
      .toMat(Sink.foreach(_.map {
        case ((Success(resp), p)) => p.success(resp)
        case ((Failure(e), p)) => p.failure(e)
      }))(Keep.left)
      .run()

  def queueRequest(request: HttpRequest): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    queue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued => responsePromise.future
      case QueueOfferResult.Dropped => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) => Future.failed(ex)
      case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }

  def serviceClient(aPIRequest: APIRequest): Future[HttpResponse] = {
    val uri = Uri(s"/${aPIRequest.identifier}").withQuery(Query("q" -> aPIRequest.parameters.mkString(",")))
    queueRequest(HttpRequest(uri = uri))

  }
}


class PricingQueue extends ClientImplicits {

  val QueueSize = 4

  val poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpResponse]](host = "localhost", port = 8080)
  val queue =
    Source.queue[(HttpRequest, Promise[HttpResponse])](QueueSize, OverflowStrategy.backpressure)
      .via(poolClientFlow).groupedWithin(4, FiniteDuration(5, TimeUnit.SECONDS))
      .toMat(Sink.foreach(_.map{
        case ((Success(resp), p)) => p.success(resp)
        case ((Failure(e), p)) => p.failure(e)
      }))(Keep.left)
      .run()

  def queueRequest(request: HttpRequest): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    queue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued => responsePromise.future
      case QueueOfferResult.Dropped => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) => Future.failed(ex)
      case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }

  def serviceClient(aPIRequest: APIRequest): Future[HttpResponse] = {
    val uri = Uri(s"/${aPIRequest.identifier}").withQuery(Query("q" -> aPIRequest.parameters.mkString(",")))
    queueRequest(HttpRequest(uri = uri))

  }
}

class OrderQueue extends ClientImplicits {

  val QueueSize = 4

  val poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpResponse]](host = "localhost", port = 8080)

  val queue =
    Source.queue[(HttpRequest, Promise[HttpResponse])](QueueSize, OverflowStrategy.backpressure)
      .via(poolClientFlow).groupedWithin(4, FiniteDuration(5, TimeUnit.SECONDS))
      .toMat(Sink.foreach(_.map{
        case ((Success(resp), p)) => p.success(resp)
        case ((Failure(e), p)) => p.failure(e)
      }))(Keep.left)
      .run()

  def queueRequest(request: HttpRequest): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    queue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued => responsePromise.future
      case QueueOfferResult.Dropped => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) => Future.failed(ex)
      case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }

  def serviceClient(aPIRequest: APIRequest): Future[HttpResponse] = {
    val uri = Uri(s"/${aPIRequest.identifier}").withQuery(Query("q" -> aPIRequest.parameters.mkString(",")))
    queueRequest(HttpRequest(uri = uri))

  }
}