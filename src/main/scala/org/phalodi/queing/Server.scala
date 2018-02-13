package org.phalodi.queing

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.io.StdIn

object Server extends App {
  new ServerStart().startServer
}

class ServerStart extends QueuingServiceRoutes {

  val apiService = new APIServices

  def startServer = {
    val apiService = new APIServices
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    Http().bindAndHandle(route, "localhost", 8080)

  }
}