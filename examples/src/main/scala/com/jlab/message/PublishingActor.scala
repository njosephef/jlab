package com.jlab.message

/**
 * Created by scorpiovn on 12/22/14.
 */
class PublishingActor(channel: Channel, exchange: String) extends Actor {

  /**
   * When we receive a message we sent it using the configured channel
   */
  def receive = {
    case some: String => {
      val msg = (some + " : " + System.currentTimeMillis());
      channel.basicPublish(exchange, "", null, msg.getBytes());
      Logger.info(msg);
    }
    case _ => {}
  }
}
