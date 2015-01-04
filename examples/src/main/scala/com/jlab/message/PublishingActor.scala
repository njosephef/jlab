package com.jlab.message

import akka.actor.Actor
import akka.event.Logging
import com.rabbitmq.client.Channel

/**
 * Created by scorpiovn on 12/22/14.
 */
class PublishingActor(channel: Channel, exchange: String) extends Actor {

  val log = Logging(context.system, this)
  /**
   * When we receive a message we sent it using the configured channel
   */
  def receive = {
    case some: String => {
      val msg = (some + " : " + System.currentTimeMillis())
      channel.basicPublish(exchange, "", null, msg.getBytes())
//      log.info(msg)
    }
    case _ => {}
  }
}

