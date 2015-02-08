package org.jlab.fetch

import argonaut._, Argonaut._
import scalaz._, Scalaz._
import com.typesafe.config.ConfigFactory
import org.jlab.domain.{Pipelines}

/**
 * Created by scorpiovn on 2/2/15.
 */
object SimpleApp extends App {

  val conf = ConfigFactory.load("application.json")
  println("The answer is: " + conf.getString("httpManager.maxConnection"))

  val config = io.Source.fromInputStream(getClass.getResourceAsStream("/application.json")).mkString
  val pipelines = Parse.decodeOption[Pipelines](config).getOrElse(Nil) // prefer this
//  val pipelines: Option[Pipelines] = config.decodeOption[Pipelines]

  // 1
  val pip2 = pipelines.asInstanceOf[Pipelines]
  println(pip2.prefetcher)
  println(pip2.httpFetchers)

  // 2
  for (value <- pip2.prefetcher) {
    println(value.batchSize)
  }

  // 3
  println(pip2.prefetcher.map(_.batchSize).getOrElse(""))

  // 4
  pipelines match {
    case Pipelines(httpManager, prefetcher, httpFetchers) =>
      println(httpManager)
      println(prefetcher)
      println(httpFetchers)
    case _ =>
  }

}
