package org.jlab.store

import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorSystem}
import com.jlab.message.{RabbitMQConnection}
import com.rabbitmq.client.{Channel}
import org.jlab.service.MongoWorker
import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration.Duration

/**
 * Created by scorpiovn on 1/3/15.
 */
case class TextMessage(title:String, body:String)

object DatabaseWriter {
  def main(args: Array[String]): Unit = {
    val connection = RabbitMQConnection.getConnection()
    val channel = connection.createChannel()
    channel.exchangeDeclare("WritableOut", "direct");

    // create another channel for a listener and setup the second listener
    channel.queueDeclare("WritableQueue", false, false, false, null)

    val callback = (x: TextMessage) => writer(x)
    setupListener(channel, "WritableQueue", "WritableOut", callback);

  }

  private def setupListener(channel: Channel, queueName : String, exchange: String, func: (TextMessage) => Any) {
    channel.queueBind(queueName, exchange, "writable");
    val system: ActorSystem = ActorSystem("MySystem")
    system.scheduler.scheduleOnce(Duration.create(1, TimeUnit.SECONDS),
      system.actorOf(Props(new OnMessage(channel, queueName, func))), "")
  }

  private def writer(message: TextMessage): Unit = {
    println("The callback function called")
    implicit val formats = Serialization.formats(
      ShortTypeHints(
        List(
          classOf[TextMessage]
        )
      )
    )

//    val json = Serialization.writePretty(textMsg)

    MongoWorker.getInstance().save(message);

    printf("finished writing")
  }
}
