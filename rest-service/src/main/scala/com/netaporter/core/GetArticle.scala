package com.netaporter.core

import akka.actor.{Actor, ActorLogging, ActorRef}
import org.jlab.model.Article

/**
 * Created by ngvtien on 1/20/2015.
 * This job is to serve a request that asks for an article
 * - crawl the article
 * - clean up the article
 */
class GetArticle(crawlService: ActorRef, cleanService: ActorRef) extends Actor with ActorLogging {
  def receive = {
    crawlService | Article
    cleanService | Article
  }
}
