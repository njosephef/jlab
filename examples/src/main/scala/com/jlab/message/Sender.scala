package com.jlab.message

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import com.rabbitmq.client.Channel
import play.api.Logger
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration.FiniteDuration


/**
 * Created by scorpiovn on 12/22/14.
 */
object Sender {

  def startSending = {

    println(Config.RABBITMQ_QUEUE)
    println(Config.RABBITMQ_HOST)
    println(Config.RABBITMQ_EXCHANGEE)

    // create the connection
    val connection = RabbitMQConnection.getConnection()

    // create the channel we use to send
    val sendingChannel = connection.createChannel()

    // make sure the queue exists we want to send to
    sendingChannel.queueDeclare(Config.RABBITMQ_QUEUE, false, false, false, null)

    val callback1 = (x: String) => Logger.info("Recieved on queue callback 1: " + x)

    setupListener(connection.createChannel(),Config.RABBITMQ_QUEUE, callback1)

    // create an actor that starts listening on the specified queue and passes the
    // received message to the provided callback
    val callback2 = (x: String) => Logger.info("Recieved on queue callback 2: " + x)

    // setup the listener that sends to a specific queue using the SendingActor
    setupListener(connection.createChannel(),Config.RABBITMQ_QUEUE, callback2)

    Akka.system.scheduler.schedule(FiniteDuration(2.toLong, TimeUnit.SECONDS), FiniteDuration(1.toLong, TimeUnit.SECONDS)
      , Akka.system.actorOf(
          Props(new SendingActor(channel = sendingChannel, queue = Config.RABBITMQ_QUEUE))
        )
      , "MSG to Queue")

    // create a new sending channel on which we declare the exchange
    val sendingChannel2 = connection.createChannel()
    sendingChannel2.exchangeDeclare(Config.RABBITMQ_EXCHANGEE, "fanout")

    // define the two callbacks for our listeners
    val callback3 = (x: String) => Logger.info("Recieved on exchange callback 3: " + x)
    val callback4 = (x: String) => Logger.info("Recieved on exchange callback 4: " + x)

    // create a channel for the listener and setup the first listener
    val listenChannel1 = connection.createChannel()
    setupListener(listenChannel1,listenChannel1.queueDeclare().getQueue(),
      Config.RABBITMQ_EXCHANGEE, callback3)

    // create another channel for a listener and setup the second listener
    val listenChannel2 = connection.createChannel()
    setupListener(listenChannel2,listenChannel2.queueDeclare().getQueue(),
      Config.RABBITMQ_EXCHANGEE, callback4)

    // create an actor that is invoked every two seconds after a delay of
    // two seconds with the message "msg"
    Akka.system.scheduler.schedule(FiniteDuration(2.toLong, TimeUnit.SECONDS), FiniteDuration(1.toLong, TimeUnit.SECONDS)
      , Akka.system.actorOf(
          Props(new PublishingActor(channel = sendingChannel2, exchange = Config.RABBITMQ_EXCHANGEE))
        )
      , "MSG to Exchange")
  }

  private def setupListener(receivingChannel: Channel, queue: String, f: (String) => Any) {
    Akka.system.scheduler.scheduleOnce(FiniteDuration(2.toLong, TimeUnit.SECONDS),
      Akka.system.actorOf(Props(new ListeningActor(receivingChannel, queue, f))), "")
  }

  private def setupListener(channel: Channel, queueName : String, exchange: String, f: (String) => Any) {
    channel.queueBind(queueName, exchange, "")

    Akka.system.scheduler.scheduleOnce(FiniteDuration(2.toLong, TimeUnit.SECONDS),
      Akka.system.actorOf(Props(new ListeningActor(channel, queueName, f))), "")
  }
}

class SendingActor(channel: Channel, queue: String) extends Actor {
  def receive = {
    case some: String => {
      val msg = (some + " : " + System.currentTimeMillis)
      channel.basicPublish("", queue, null, msg.getBytes())
      Logger.info(msg)
    }
    case _ => {}
  }
}
