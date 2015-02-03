package org.jlab.fetch

import argonaut._, Argonaut._
import scalaz._, Scalaz._
import com.typesafe.config.ConfigFactory
import org.jlab.domain.{Prefetcher, Pipelines}

/**
 * Created by scorpiovn on 2/2/15.
 */
object SimpleApp extends App {

  val conf = ConfigFactory.load("application.json")
  println("The answer is: " + conf.getString("httpManager.maxConnection"))

  val config = io.Source.fromInputStream(getClass.getResourceAsStream("/application.json")).mkString
  val pipelines = Parse.decodeOption[Pipelines](config).getOrElse(Nil) // prefer this
//  println(pipelines)

//  val pipelines: Option[Pipelines] = config.decodeOption[Pipelines]

  val pip2 = pipelines.asInstanceOf[Pipelines]
  println(pip2.prefetcher)
  println(pip2.httpFetchers)

//  val pref = pip2.prefetcher
//  val pref2 = pref.asInstanceOf[Prefetcher]
//  println(pref2.batchSize)

  for (value <- pip2.prefetcher) {
    println(value.batchSize)
  }



  println(pip2.prefetcher.map(_.batchSize).getOrElse(""))



  pipelines match {
    case Pipelines(httpManager, prefetcher, httpFetchers) =>
      println(httpManager)
      println(prefetcher)
      println(httpFetchers)
    case _ =>
  }

//  val personNoFavouriteNumbers = pipelines.copy(httpManager)

//  val httpManager: Option[HttpManager] =  Parse.decodeWithMessage[HttpManager, Pipelines](config, _.copy(), _)

//  println(httpManager)

//  pipelines.

//  pipelines.
//  println("The answer is: " + conf.getString("pipelines.httpManager"))
//  println("The answer is: " + conf.getString("pipelines"))
}
