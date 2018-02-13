package org.phalodi.queing

import akka.http.scaladsl.server.Directives.{pathEnd, _}
import com.typesafe.config.ConfigFactory

import collection.JavaConversions._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol

trait QueuingServiceRoutes extends DefaultJsonProtocol {

  val apiService: APIServices

  val products = ConfigFactory.load.getStringList("products")
  val status = ConfigFactory.load.getStringList("status")

  val route =
    pathPrefix("clients") {
      get {
        pathEnd {
          parameter('q) { q =>
            val clientNumbers = q.split(",").toList
            validate(!clientNumbers.exists(_.length != 9), "Invalid client id") {
              val res = clientNumbers.map { clientNumber =>
                (clientNumber, apiService.getProduct(clientNumber.toInt % 10, products.toList))
              }.toMap
              complete(res)
            }
          }
        }
      }
    } ~ pathPrefix("order") {
      get {
        pathEnd {
          parameter('q) { q =>
            val clientNumbers = q.split(",").toList
            validate(!clientNumbers.exists(_.length != 9), "Invalid client id") {
              val res = clientNumbers.map { clientNumber =>
                (clientNumber, apiService.getStatus(clientNumber.toInt % 10, status.toList))
              }.toMap
              complete(res)
            }
          }
        }
      }
    } ~ pathPrefix("pricing") {
      get {
        pathEnd {
          parameter('q) { q =>
            val countries = q.split(",").toList
            validate(!countries.exists(_.length != 2), "Invalid country code") {
              val res = countries.map { country =>
                (country, apiService.getPrice)
              }.toMap
              complete(res)
            }
          }
        }
      }

    }
}