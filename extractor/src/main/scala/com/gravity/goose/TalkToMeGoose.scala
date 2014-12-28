package com.gravity.goose

import java.io.File

import org.apache.commons.io.FileUtils
import org.json4s.ShortTypeHints
import org.json4s.native.Serialization

/**
 * Created by Jim Plush
 * User: jim
 * Date: 5/13/11
 */

case class TextContent(title: String, body: String)
case class HTMLContent(val url: String, val html: String)

object TalkToMeGoose {
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
      val url: String = args(0)
      val config: Configuration = new Configuration
      config.enableImageFetching = false
      val goose = new Goose(config)
      val article = goose.extractContent(url)
      println(article.cleanedArticleText)

      implicit val formats = Serialization.formats(
        ShortTypeHints(
          List(
            classOf[TextContent]
          )
        )
      )

      val textContent = new TextContent(url, article.cleanedArticleText)
//      val json = Serialization.write(textContent)

      FileUtils.write(new File("test.json"), Serialization.writePretty(textContent).toString, "UTF-8")
    }
    catch {
      case e: Exception => {
        System.out.println("Make sure you pass in a valid URL: " + e.toString)
      }
    }
  }
}


