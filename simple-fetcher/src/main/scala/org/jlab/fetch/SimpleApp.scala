package org.jlab.fetch

import argonaut.Parse
import com.typesafe.config.ConfigFactory
import org.jlab.domain.{Pipelines}

/**
 * Created by scorpiovn on 2/2/15.
 */
object SimpleApp extends App {

  val conf = ConfigFactory.load("application.json")
  println("The answer is: " + conf.getString("httpManager.maxConnection"))

  val config = io.Source.fromInputStream(getClass.getResourceAsStream("/application.json")).mkString
println(config)
  val setting = Parse.decodeOption[Pipelines](config).getOrElse(Nil)
  println(setting)
//  println("The answer is: " + conf.getString("pipelines.httpManager"))
//  println("The answer is: " + conf.getString("pipelines"))
}
