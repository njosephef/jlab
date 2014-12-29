package com.jlab.message

import akka.actor.Actor
import akka.event.Logging
import com.rabbitmq.client.Channel

import scala.io.Source

/**
 * Created by scorpiovn on 12/29/14.
 */
class URLReadingActor(channel: Channel, queue: String) extends Actor {
  val log = Logging(context.system, this)
  def receive = {
    case some: String => {
      log.info(some)
      Source.fromFile(System.getenv ("HOME") + "/data/url.txt").getLines.foreach {
        line => log.info(line)
          val msg = line
          channel.basicPublish("", queue, null, msg.getBytes());
      }
    }
    case _ => {}
  }
}