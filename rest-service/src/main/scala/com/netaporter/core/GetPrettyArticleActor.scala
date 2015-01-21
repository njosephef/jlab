package com.netaporter.core

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{OneForOneStrategy, ActorRef, Actor, ActorLogging}
import com.netaporter.clients.CleanClient.{PlainArticle, GetPlainArticle}
import com.netaporter.clients.CrawlClient.{OriginalArticle, GetOriginalArticle}
import com.netaporter.{GetArticle, PrettyArticle}
import org.jlab.model.Article

/**
 * The job of this Actor in our application core is to service a request
 * that asks for content of web page by its url after cleaning.
 *
 * This actor will have the responsibility of making two requests and then aggregating them together:
 *  - One requests for a list the pets by their names
 *  - A separate request for a list of owners by their pet names
 */
class GetPrettyArticleActor(crawlService: ActorRef, cleanService: ActorRef) extends Actor with ActorLogging {

  var original: Article = new Article("", "")
  var plain: Article = new Article("", "")

  def receive = {
    case GetArticle(urlRequest) => {
      log.info("send a crawling request"); crawlService ! GetOriginalArticle(urlRequest)
//      log.info("get extract for article"); ownerService ! GetCleanArticle(article)
      log.info("--> waiting responses"); context.become(waitingResponses)
    }
  }

  def waitingResponses: Receive = {
    case OriginalArticle(article) => {
      original = article
      log.info("request clean up the article"); cleanService ! GetPlainArticle(article)
      replyIfReady
    }

    case PlainArticle(article) => {
      plain = article
      replyIfReady
    }

//    case f: Validation => context.parent ! f
  }

  def replyIfReady = if(original.content.nonEmpty && plain.content.nonEmpty) {
//      val petSeq = extracted.head
//      val ownerSeq = extracted.head

//      val enrichedPets = (petSeq zip ownerSeq).map { case (pet, owner) => pet.withOwner(owner) }
//      val pretty = extracted.head
      context.parent ! PrettyArticle(plain)
    }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}