package com.netaporter.routing

import akka.actor.{Props, Actor}
import com.netaporter._
import org.jlab.model.Article
import spray.routing.{Route, HttpService}
import com.netaporter.core.{GetPrettyArticleActor, GetPetsWithOwnersActor}
import com.netaporter.clients.{CrawlClient, CleanClient, OwnerClient, PetClient}

class RestRouting extends HttpService with Actor with PerRequestCreator {

  implicit def actorRefFactory = context

  def receive = runRoute(route)

  val petService = context.actorOf(Props[PetClient])
  val ownerService = context.actorOf(Props[OwnerClient])

  val crawlService = context.actorOf(Props[CrawlClient])
  val cleanService = context.actorOf(Props[CleanClient])

  val route = {
    get {
      path("pets") {
        parameters('names) { names =>
          petsWithOwner {
            GetPetsWithOwners(names.split(',').toList)
          }
        }
      } ~
      path("article") {
        parameter('url) { url =>
          cleanedArticle {
            GetArticle(new UrlRequest(url))
          }
        }
      }
    }
  }

  def petsWithOwner(message : RestMessage): Route =
    ctx => perRequest(ctx, Props(new GetPetsWithOwnersActor(petService, ownerService)), message)

  def cleanedArticle(message : RestMessage): Route =
    ctx => perRequest(ctx, Props(new GetPrettyArticleActor(crawlService, cleanService)), message)

}