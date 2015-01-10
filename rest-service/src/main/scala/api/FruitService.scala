package api

import core.MessengerActor.SendMessage
import core.MessengerActor
import spray.routing.Directives
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import spray.http.MediaTypes._


/**
 * Created by antoine on 3/17/14.
 */
class FruitService(fruit: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {


  implicit val sendMessageFormat = jsonFormat2(SendMessage)

  val fruitroute =
    path("fruits") {
      get {
        respondWithMediaType(`application/json`) {
          _.complete {
            """ [
                    {"name": "banana", "price": "0.79"},
                     {"name": "apple", "price": "1.89"},
                    {"name": "raspberry", "price": "12.50"}
                  ]"""
          }
        }
      }
    } ~
    path("articles") {
      get {
        respondWithMediaType(`application/json`) {
          _.complete {
            """[
                {"url": "http", "content": "You."},
                {"url": "http", "content": "To play life ."},
              ]"""
          }
        }
      }
    }
}
