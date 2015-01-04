package com.gravity.goose

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import com.gravity.goose.utils.Filter
import com.jlab.message.{Config, RabbitMQConnection}
import com.rabbitmq.client.{Channel, MessageProperties}
import org.json4s._
import org.json4s.native.Serialization

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

//import org.jlab.model.{TextMessage, HTMLMessage, Message}

//case class Message(url: String, html: String)

case class HTMLMessage(url: String, html: String)

case class TextMessage(title: String, body: String)

object HTMLFilter {
  def main(args: Array[String]) {
    try {
      val connection = RabbitMQConnection.getConnection()
      val listenChannel2 = connection.createChannel()
      listenChannel2.exchangeDeclare( Config.RABBITMQ_EXCHANGE_HTML, "direct");

      // create another channel for a listener and setup the second listener
      listenChannel2.queueDeclare(Config.RABBITMQ_QUEUE_HTML, false, false, false, null)
      println("queue " + Config.RABBITMQ_QUEUE_HTML)

      val callback = (x: Message) => extract(x)
      setupListener(listenChannel2, Config.RABBITMQ_QUEUE_HTML, Config.RABBITMQ_EXCHANGE_HTML, callback);

    }
    catch {
      case e: Exception => {
        System.out.println("Make sure you pass in a valid URL: " + e.toString)
      }
    }
  }

  private def setupListener(channel: Channel, queueName : String, exchange: String, func: (Message) => Any) {
    channel.queueBind(queueName, exchange, "200");
    val system: ActorSystem = ActorSystem("MySystem")
    system.scheduler.scheduleOnce(Duration.create(1, TimeUnit.SECONDS),
      system.actorOf(Props(new OnMessage(channel, queueName, func))), "")
  }

  private def extract(message: Message): Unit = {
    println("The callback function called")
    implicit val formats = Serialization.formats(
      ShortTypeHints(
        List(
          classOf[TextMessage],
          classOf[Message]
        )
      )
    )

//    printf("before pretty "+x)
//    printf("x can be null " + message)
//    val json = JsonMethods.parse(message)
//    val htmlMsg = json.extract[Message]
//  println(">>> " + message.url)

    val config: Configuration = new Configuration
    config.enableImageFetching = false
    val filter = new Filter(config)
    val article = filter.filter(message.url, message.html)
    println(article.cleanedArticleText)

    val textMsg = new TextMessage(message.url, article.cleanedArticleText)
  val json = Serialization.writePretty(textMsg)

//    FileUtils.write(new File("test.json" + Random.nextLong()), Serialization.writePretty(textMsg).toString, "UTF-8")

    val connection = RabbitMQConnection.getConnection()
    val channel = connection.createChannel()
    channel.exchangeDeclare("WritableOut", "direct");

    // create another channel for a listener and setup the second listener
    // channel.queueDeclare("WritableOut", false, false, false, null)

    channel.basicPublish("WritableOut", "writable", MessageProperties.PERSISTENT_TEXT_PLAIN, json.getBytes())

//    channel.basicPublish(exch, fullkey, MessageProperties.PERSISTENT_TEXT_PLAIN, json.getBytes())
//    channel.basicAck(deliveryTag, false)

    printf("finished clean up")
  }
}


