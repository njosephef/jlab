package com.jlab.message

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Actor, Props}
import akka.event.Logging
import com.rabbitmq.client.Channel
import play.api.Logger

import scala.concurrent.duration.Duration
import scala.io.Source

//import akka.actor.{Actor, Props}
//import akka.util.duration._
//import akka.actor.{Actor, Props, ActorSystem}
//import com.rabbitmq.client.Channel
//import play.api.Logger
//import play.libs.Akka

//import play.api.Play.current
//import play.api.libs.concurrent.Akka

//import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by scorpiovn on 12/22/14.
 */
object Sender {

  def startSending = {
    val connection = RabbitMQConnection.getConnection()
    val sendingChannel = connection.createChannel()

    // make sure the queue exists we want to send to
//    sendingChannel.queueDeclare(Config.RABBITMQ_QUEUE, false, false, false, null)

    val system: ActorSystem = ActorSystem("MySystem")

    val sendingActor: ActorRef = system.actorOf(Props(new SendingActor(channel = sendingChannel, queue = Config.RABBITMQ_QUEUE)))

    /*system.scheduler.schedule(
      Duration.create(100, TimeUnit.MILLISECONDS)
      , Duration.create(1, TimeUnit.SECONDS)
      , sendingActor
      , "MSG to Queue")*/

    val callback1 = (x: String) => Logger.info("Recieved on queue callback 1: " + x);

    setupListener(connection.createChannel(), Config.RABBITMQ_QUEUE, callback1);

    // create an actor that starts listening on the specified queue and passes the
    // received message to the provided callback
    val callback2 = (x: String) => Logger.info("Recieved on queue callback 2: " + x);

    // setup the listener that sends to a specific queue using the SendingActor
    setupListener(connection.createChannel(), Config.RABBITMQ_QUEUE, callback2);

    // create a new sending channel on which we declare the exchange
    val sendingChannel2 = connection.createChannel();
    sendingChannel2.exchangeDeclare(Config.RABBITMQ_EXCHANGEE, "fanout");

    // define the two callbacks for our listeners
    val callback3 = (x: String) => Logger.info("Recieved on exchange callback 3: " + x);

    // create a channel for the listener and setup the first listener
    val listenChannel1 = connection.createChannel();
    setupListener(listenChannel1,listenChannel1.queueDeclare().getQueue(), Config.RABBITMQ_EXCHANGEE, callback3);

    val callback4 = (x: String) => Logger.info("Recieved on exchange callback 4: " + x);

    // create another channel for a listener and setup the second listener
    val listenChannel2 = connection.createChannel();
    setupListener(listenChannel2,listenChannel2.queueDeclare().getQueue(), Config.RABBITMQ_EXCHANGEE, callback4);

    // create an actor that is invoked every two seconds after a delay of
    // two seconds with the message "msg"
    system.scheduler.schedule(Duration.create(2, TimeUnit.SECONDS), Duration.create(1, TimeUnit.SECONDS), system.actorOf(Props(
      new PublishingActor(channel = sendingChannel2
        , exchange = Config.RABBITMQ_EXCHANGEE))),
      "MSG to Exchange")
  }

  private def setupListener(receivingChannel: Channel, queue: String, f: (String) => Any) {
    val system: ActorSystem = ActorSystem("MySystem")
    val listeningActor: ActorRef = system.actorOf(Props(new ListeningActor(receivingChannel, queue, f)))
    system.scheduler.scheduleOnce(Duration.create(2, TimeUnit.SECONDS),
    listeningActor, "")
  }

  private def setupListener(channel: Channel, queueName : String, exchange: String, f: (String) => Any) {
    channel.queueBind(queueName, exchange, "");
    val system: ActorSystem = ActorSystem("MySystem")
    system.scheduler.scheduleOnce(Duration.create(2, TimeUnit.SECONDS),
      system.actorOf(Props(new ListeningActor(channel, queueName, f))), "");
  }
}

object URLReading {
  def readFromFile = {
    val connection = RabbitMQConnection.getConnection()
    val urlChannel = connection.createChannel()
    // make sure the queue exists we want to send to
    urlChannel.queueDeclare(Config.RABBITMQ_QUEUE, false, false, false, null)

    createSender(channel = urlChannel, queue = Config.RABBITMQ_QUEUE)
  }

  private def createSender(channel: Channel, queue: String): Unit = {
    val system: ActorSystem = ActorSystem("MySystem")
    val sendingActor: ActorRef = system.actorOf(Props(new SendingActor(channel, queue)))
    system.scheduler.schedule(
      Duration.create(2, TimeUnit.MILLISECONDS)
      , Duration.create(15, TimeUnit.SECONDS)
      , sendingActor
      , "MSG to Queue")
  }
}

class SendingActor(channel: Channel, queue: String) extends Actor {
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