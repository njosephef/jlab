package com.gravity.goose

import java.io.File
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import com.gravity.goose.utils.Filter
import com.jlab.message.{Config, RabbitMQConnection}
import com.rabbitmq.client.Channel
import org.apache.commons.io.FileUtils
import org.json4s._
import org.json4s.native.Serialization

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Random

object HTMLFilter {
  /**
  * you can use this method if you want to run goose from the command line to extract html from a bashscript
  * or to just test it's functionality
  * you can run it like so
  * cd into the goose root
  * mvn compile
  * MAVEN_OPTS="-Xms256m -Xmx2000m"; mvn exec:java -Dexec.mainClass=com.gravity.goose.TalkToMeGoose -Dexec.args="http://techcrunch.com/2011/05/13/native-apps-or-web-apps-particle-code-wants-you-to-do-both/" -e -q > ~/Desktop/gooseresult.txt
  *
  * Some top gun love:
  * Officer: [in the midst of the MIG battle] Both Catapults are broken, sir.
  * Stinger: How long will it take?
  * Officer: It'll take ten minutes.
  * Stinger: Bullshit ten minutes! This thing will be over in two minutes! Get on it!
  *
  * @param args
  */

  def main(args: Array[String]) {
    try {
      val connection = RabbitMQConnection.getConnection()
      val listenChannel2 = connection.createChannel()
      listenChannel2.exchangeDeclare( Config.RABBITMQ_EXCHANGE_HTML, "direct");

      // create another channel for a listener and setup the second listener
      listenChannel2.queueDeclare(Config.RABBITMQ_QUEUE_HTML, false, false, false, null)
      println("queue " + Config.RABBITMQ_QUEUE_HTML)

      val callback4 = (x: HTMLContent) => extract(x)
      setupListener(listenChannel2, Config.RABBITMQ_QUEUE_HTML, Config.RABBITMQ_EXCHANGE_HTML, callback4);

    }
    catch {
      case e: Exception => {
        System.out.println("Make sure you pass in a valid URL: " + e.toString)
      }
    }
  }

  private def setupListener(channel: Channel, queueName : String, exchange: String, func: (HTMLContent) => Any) {
    channel.queueBind(queueName, exchange, "200");
    val system: ActorSystem = ActorSystem("MySystem")
    system.scheduler.scheduleOnce(Duration.create(1, TimeUnit.SECONDS),
      system.actorOf(Props(new OnMessage(channel, queueName, func))), "");
  }

  private def extract(html: HTMLContent): Unit = {
    println("The callback function called")
    implicit val formats = Serialization.formats(
      ShortTypeHints(
        List(
          classOf[TextContent],
          classOf[HTMLContent]
        )
      )
    )

//    printf("before pretty "+x)
    printf("x can be null")
//    val json = parse(x)
//    val htmlContent = json.extract[HTMLContent]

    val config: Configuration = new Configuration
    config.enableImageFetching = false
    val filter = new Filter(config)
    val article = filter.filter(html.url, html.html)
    println(article.cleanedArticleText)

    val textContent = new TextContent(html.url, article.cleanedArticleText)

    FileUtils.write(new File("test.json" + Random.nextLong()), Serialization.writePretty(textContent).toString, "UTF-8")
    printf("finished")
  }
}


