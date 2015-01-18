package com.jlab.message

import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorRef, ActorSystem}
import com.rabbitmq.client.Channel

import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by scorpiovn on 12/29/14.
 * publish to broker
 */
object URLPublisher {
  def start = {
    val connection = RabbitMQConnection.getConnection()
    val urlChannel = connection.createChannel()
    // make sure the queue exists we want to send to
    urlChannel.queueDeclare(Config.RABBITMQ_QUEUE_URL, false, false, false, null)

    createSender(channel = urlChannel, queue = Config.RABBITMQ_QUEUE_URL)
  }

  private def createSender(channel: Channel, queue: String): Unit = {
    val system: ActorSystem = ActorSystem("MySystem")
    val sendingActor: ActorRef = system.actorOf(Props(new URLPublishActor(channel, queue)))
//    sendingActor !
    /*system.scheduler.schedule(
      Duration.create(2, TimeUnit.MILLISECONDS)
      , Duration.create(30, TimeUnit.SECONDS)
      , sendingActor
      , "MSG to Queue")*/
  }
}

/*object URLPublisher {
  def start = {
    val connection = RabbitMQConnection.getConnection()
    val urlChannel = connection.createChannel()
    // make sure the queue exists we want to send to
    urlChannel.queueDeclare(Config.RABBITMQ_QUEUE_URL, false, false, false, null)

    createSender(channel = urlChannel, queue = Config.RABBITMQ_QUEUE_URL)
  }

  private def createSender(channel: Channel, queue: String): Unit = {
    val system: ActorSystem = ActorSystem("MySystem")
    val sendingActor: ActorRef = system.actorOf(Props(new URLPublishActor(channel, queue)))
    system.scheduler.schedule(
      Duration.create(2, TimeUnit.MILLISECONDS)
      , Duration.create(30, TimeUnit.SECONDS)
      , sendingActor
      , "MSG to Queue")
  }
}*/

