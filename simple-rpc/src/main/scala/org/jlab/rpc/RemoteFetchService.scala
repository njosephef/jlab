package main.scala.org.jlab.rpc

import akka.actor._
import com.jlab.message.{URLPublishActor, Config, RabbitMQConnection}
import com.rabbitmq.client.Channel
import org.jlab.model.{HTMLContent, Article, Message}

/**
 * Created by scorpiovn on 1/14/15.
 */
object RemoteFetchService extends App {
  val system = ActorSystem("simple-rpc")
  val remoteActor = system.actorOf(Props[RemoteFetchActor], name = "RemoteFetchActor")
}

class RemoteFetchActor extends Actor {
  val connection = RabbitMQConnection.getConnection()
  val channel = connection.createChannel()
  channel.queueDeclare(Config.RABBITMQ_QUEUE_URL, false, false, false, null)

  def receive = {
    case Article(url, content) =>
      println(s"RemoteActor received message '$url' and '$content'")
      val system = ActorSystem("simple-url")
      val sendingActor: ActorRef = system.actorOf(Props(new URLPublishActor(channel, Config.RABBITMQ_QUEUE_URL)))
      sendingActor ! url

    case HTMLContent(url, html) =>
      sender ! Article(url, html)

    case _ => println("unknown message")
  }
}
