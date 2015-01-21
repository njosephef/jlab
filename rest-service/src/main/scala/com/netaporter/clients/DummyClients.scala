package com.netaporter.clients

import akka.actor.{ActorLogging, Actor}
import com.netaporter._
import com.netaporter.clients.CleanClient.{PlainArticle, GetPlainArticle}
import com.netaporter.clients.CrawlClient.{OriginalArticle, GetOriginalArticle}
import com.netaporter.clients.PetClient._
import com.netaporter.clients.OwnerClient._
import org.jlab.model.Article

/**
 * This could be:
 *  - a REST API we are the client for
 *  - a Database
 *  - anything else that requires IO
 */
class PetClient extends Actor {
  def receive = {
    case GetPets("Lion" :: _)     => sender ! Validation("Lions are too dangerous!")
    case GetPets("Tortoise" :: _) => () // Never send a response. Tortoises are too slow
    case GetPets(petNames)        => Thread.sleep(9000); sender ! Pets(petNames.map(Pet.apply))
  }
}
object PetClient {
  case class GetPets(petNames: List[String])
  case class Pets(pets: Seq[Pet])
}

/**
 * This could be:
 *  - a REST API we are the client for
 *  - a Database
 *  - anything else that requires IO
 */
class OwnerClient extends Actor {
  def receive = {
    case GetOwnersForPets(petNames) => {
      val owners = petNames map {
        case "Lassie"        => Thread.sleep(5000); Owner("Jeff Morrow")
        case "Brian Griffin" => Thread.sleep(7000); Owner("Peter Griffin")
        case "Tweety"        => Owner("Granny")
        case _               => Owner("Jeff") // Jeff has a lot of pets
      }
      sender ! OwnersForPets(owners)
    }
  }
}
object OwnerClient {
  case class GetOwnersForPets(petNames: Seq[String])
  case class OwnersForPets(owners: Seq[Owner])
}

class CrawlClient extends Actor with ActorLogging {
  def receive = {
    case GetOriginalArticle(urlRequest) => {
      log.info("start crawling the web page by the url " + urlRequest.url)
      Thread.sleep(5000)
      val article = new Article(urlRequest.url, "html")
      log.info("Crawler finishes its job and send the content back")
      sender ! OriginalArticle(article)
    }
  }
}
object CrawlClient {
  case class GetOriginalArticle(urlRequest: UrlRequest)
  case class OriginalArticle(article: Article)
}

class CleanClient extends Actor with ActorLogging {
  def receive = {
    case GetPlainArticle(article) => {
      Thread.sleep(5000); log.info("cleaning up the html and send back")
      sender ! PlainArticle(article)
    }
  }
}
object CleanClient {
  case class GetPlainArticle(article: Article)
  case class PlainArticle(article: Article)
}