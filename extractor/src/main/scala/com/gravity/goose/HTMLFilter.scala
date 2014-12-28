package com.gravity.goose

import java.io.File
import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorSystem}
import com.gravity.goose.utils.Filter
import com.jlab.message.{RabbitMQConnection, Config}
import com.rabbitmq.client.Channel
import org.apache.commons.io.FileUtils
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.native.Serialization

import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
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

      val callback4 = (x: String) => extract(x)

      // create another channel for a listener and setup the second listener
      val listenChannel2 = connection.createChannel();
      println(Config.RABBITMQ_QUEUE)
      setupListener(listenChannel2,Config.RABBITMQ_QUEUE, Config.RABBITMQ_EXCHANGEE, callback4);

    }
    catch {
      case e: Exception => {
        System.out.println("Make sure you pass in a valid URL: " + e.toString)
      }
    }
  }

  private def setupListener(channel: Channel, queueName : String, exchange: String, f: (String) => Any) {
    channel.queueBind(queueName, exchange, "");
    val system: ActorSystem = ActorSystem("MySystem")
    system.scheduler.scheduleOnce(Duration.create(1, TimeUnit.SECONDS),
      system.actorOf(Props(new OnMessage(channel, queueName, f))), "");
  }

  private def extract(x: String): Unit = {
    implicit val formats = Serialization.formats(
      ShortTypeHints(
        List(
          classOf[TextContent],
          classOf[HTMLContent]
        )
      )
    )

    printf(x)
    val json = parse(x)
    val htmlContent = json.extract[HTMLContent]

    val config: Configuration = new Configuration
    config.enableImageFetching = false
    val filter = new Filter(config)
    val article = filter.filter(htmlContent.url, htmlContent.html);
    println(article.cleanedArticleText)

    val textContent = new TextContent(htmlContent.url, article.cleanedArticleText)

    FileUtils.write(new File("test.json" + Random.nextLong()), Serialization.writePretty(textContent).toString, "UTF-8")

  }
}


