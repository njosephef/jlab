package com.netaporter.core

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{OneForOneStrategy, ActorRef, Actor, ActorLogging}
import com.netaporter.clients.CleanClient.{CleanArticle, GetCleanArticle}
import com.netaporter.clients.CrawlClient.{CrawlArticle, GetCrawlArticle}
import com.netaporter.{GetArticle, PrettyArticle}
import org.jlab.model.Article

/**
 * The job of this Actor in our application core is to service a request
 * that asks for a list of pets by their names along with their owners.
 *
 * This actor will have the responsibility of making two requests and then aggregating them together:
 *  - One requests for a list the pets by their names
 *  - A separate request for a list of owners by their pet names
 */
class GetPrettyArticleActor(crawlService: ActorRef, cleanService: ActorRef) extends Actor with ActorLogging {

  var article: Article = new Article("", "")
  var extracted: Article = new Article("", "")

  def receive = {
    case GetArticle(article) => {
      log.info("get article request"); crawlService ! GetCrawlArticle(article)
//      log.info("get extract for article"); ownerService ! GetCleanArticle(article)
      log.info("--> waiting responses"); context.become(waitingResponses)
    }
  }

  def waitingResponses: Receive = {
    case someObject: CrawlArticle => {
      article = someObject.article
      log.info("get extract for article"); cleanService ! GetCleanArticle(someObject.article)
      replyIfReady
    }

    case someObject: CleanArticle => {
      extracted = someObject.article
      replyIfReady
    }

//    case f: Validation => context.parent ! f
  }

  def replyIfReady = if(article.content.nonEmpty && extracted.content.nonEmpty) {
//      val petSeq = extracted.head
//      val ownerSeq = extracted.head

//      val enrichedPets = (petSeq zip ownerSeq).map { case (pet, owner) => pet.withOwner(owner) }
//      val pretty = extracted.head
      context.parent ! PrettyArticle(extracted)
    }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}