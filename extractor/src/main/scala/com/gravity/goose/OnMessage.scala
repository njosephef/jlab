package com.gravity.goose

import akka.actor.{Actor, Props}
import com.rabbitmq.client.{Channel, QueueingConsumer}

/**
 * Created by scorpiovn on 12/22/14.
 */
class OnMessage(channel: Channel, queue: String, func: (String) => Any) extends Actor {

  // called on the initial run
  def receive = {
    case _ => startReceving
  }

  def startReceving = {
    val consumer = new QueueingConsumer(channel);
    channel.basicConsume(queue, true, consumer);

    while (true) {
      // wait for the message
      val delivery = consumer.nextDelivery();
      val msg = new String(delivery.getBody());

      // send the message to the provided callback function
      // and execute this in a subactor
      context.actorOf(Props(new Actor {
        def receive = {
          case some: String => func(some);
        }
      })) ! msg
    }
  }
}
