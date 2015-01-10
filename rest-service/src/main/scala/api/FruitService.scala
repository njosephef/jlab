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
                    {"url": "http://alvinalexander.com/blog/post/java/how-encode-java-string-send-web-server-safe-url", "content": "// one easy string, one that's a little bit harder"},
                    {"url": "http://alvinalexander.com/blog/post/java/how-encode-java-string-send-web-server-safe-url", "content": "// one easy string, one that's a little bit harder"},
                    {"url": "http://alvinalexander.com/blog/post/java/how-encode-java-string-send-web-server-safe-url", "content": "// one easy string, one that's a little bit harder"}
                  ]"""
          }
        }
      }
    } ~
    path("articles") {
      get {
        respondWithMediaType(`application/json`) {
          _.complete {
            """ [
                    {"url": "http://alvinalexander.com/blog/post/java/how-encode-java-string-send-web-server-safe-url", "content": "// one easy string, one that's a little bit harder"},
                    {"url": "http://alvinalexander.com/blog/post/java/how-encode-java-string-send-web-server-safe-url", "content": "// one easy string, one that's a little bit harder"},
                    {"url": "http://alvinalexander.com/blog/post/java/how-encode-java-string-send-web-server-safe-url", "content": "// one easy string, one that's a little bit harder"}
                  ]"""
          }
        }
      }
    }
}
