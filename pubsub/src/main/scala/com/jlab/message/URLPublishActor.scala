package com.jlab.message

import akka.actor.Actor
import akka.event.Logging
import com.rabbitmq.client.Channel

import scala.io.Source

/**
 * Created by scorpiovn on 12/29/14.
 */
class URLPublishActor(channel: Channel, queue: String) extends Actor {
  val log = Logging(context.system, this)
  def receive = {
    case url: String =>
      log.info(url)
      val msg = """{"fetch_url" : """ + url + """}"""
      channel.basicPublish("", queue, null, msg.getBytes)

    case _ => {}
  }
}

/*class URLPublishActor(channel: Channel, queue: String) extends Actor {
  val log = Logging(context.system, this)
  def receive = {
    case some: String => {
      log.info(some)
      def homeDir(envar: Option[String]) = envar match {
        case Some(envar) => envar
        case _ => """C:\"""
      }

      Source.fromFile(homeDir(Option(System.getenv("HOME"))) + "/data/url.txt").getLines().foreach {
        line => {
          log.info (line)
          val msg = line
          channel.basicPublish("", queue, null, msg.getBytes)
        }
      }
    }
    case _ => {}
  }
}*/
