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
object ReadExchange {

  def startReading = {
    val connection = RabbitMQConnection.getConnection()

    val callback4 = (x: String) => Logger.info("Recieved on exchange callback 4: " + x);

    // create another channel for a listener and setup the second listener
    val listenChannel2 = connection.createChannel();
    println(Config.RABBITMQ_QUEUE)
    setupListener(listenChannel2,Config.RABBITMQ_QUEUE, Config.RABBITMQ_EXCHANGEE, callback4);
  }

  private def setupListener(channel: Channel, queueName : String, exchange: String, f: (String) => Any) {
    channel.queueBind(queueName, exchange, "");
    val system: ActorSystem = ActorSystem("MySystem")
    system.scheduler.scheduleOnce(Duration.create(2, TimeUnit.SECONDS),
      system.actorOf(Props(new ListeningActor(channel, queueName, f))), "");
  }
}