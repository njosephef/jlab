package com.gravity.goose

import akka.actor.{Actor, Props}
import com.rabbitmq.client.{Channel, QueueingConsumer}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.native.Serialization

/**
 * Created by scorpiovn on 12/22/14.
 */
class OnMessage(channel: Channel, queue: String, func: (HTMLContent) => Any) extends Actor {

  // called on the initial run
  def receive = {
    case _ => startReceving
  }

  def startReceving = {
    val consumer = new QueueingConsumer(channel);
    channel.basicConsume(queue, true, consumer);

    implicit val formats = Serialization.formats(
      ShortTypeHints(
        List(
          classOf[TextContent],
          classOf[HTMLContent]
        )
      )
    )

    while (true) {
      // wait for the message
      val delivery = consumer.nextDelivery();
      val msg = new String(delivery.getBody());

      val json = parse(msg)
      val htmlContent = json.extract[HTMLContent]

      println(">>>>>> msg " + msg)
      // send the message to the provided callback function
      // and execute this in a subactor

      context.actorOf(Props(new Actor {
        def receive = {
          case htmlContent: HTMLContent => func(htmlContent)
               println("actor")
        }
      })) ! htmlContent
    }
  }
}
