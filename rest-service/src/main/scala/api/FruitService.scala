package api

import core.FruitActor.FruitPojo
import core.MessengerActor.SendMessage
import org.jlab.model.{Article, Start}
import org.jlab.rpc.RequestActor
import spray.routing.Directives
import scala.concurrent.ExecutionContext
import akka.actor.{Actor, Props, ActorSystem, ActorRef}
import spray.http.MediaTypes._
import akka.pattern.ask


/**
 * Created by antoine on 3/17/14.
 */

class FruitService(fruit: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {



  implicit val sendMessageFormat = jsonFormat2(SendMessage)
  implicit val sendFruitFormat = jsonFormat2(FruitPojo)
  implicit val sendArticleFormat = jsonFormat2(Article)

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
      } ~
      post {
        entity(as[Article]) { someObject =>
          println(">>>>>>> post")
//          requestActor ! someObject
//          complete(someObject)
          complete(someObject)
        }
      }
    }
}
