package org.jlab.store

import akka.actor.{Actor, Props}
import com.rabbitmq.client.{Channel, QueueingConsumer}
import org.json4s.ShortTypeHints
import org.json4s.native.{JsonMethods, Serialization}
import scala.concurrent.ExecutionContext.Implicits.global

/*import org.jlab.model.Message
import org.json4s._
import org.json4s.jackson.JsonMethods
import org.json4s.native.Serialization*/

/**
 * Created by scorpiovn on 12/22/14.
 */
class OnMessage(channel: Channel, queue: String, func: (TextMessage) => Any) extends Actor {

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
          classOf[TextMessage]
        )
      )
    )

    while (true) {
      // wait for the message
      val delivery = consumer.nextDelivery()
      val msg = new String(delivery.getBody())
println(msg)
      val json = JsonMethods.parse(msg)
      val html: TextMessage = json.extract[TextMessage]

      println(">>>>>> msg " + msg.length)

      context.actorOf(Props(new Actor {
        def receive = {
          case message: TextMessage =>
            func(message)
        }
      })) ! html // """{"url": "url", "html":"data"}"""
    }
  }
}
