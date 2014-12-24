package com.jlab.message

import java.io.File

import com.typesafe.config.ConfigFactory

/**
 * Created by scorpiovn on 12/22/14.
 */
object Config {
  val conf = ConfigFactory.load()
//  println("The answer is: " + conf.getString("simple-app.answer"))
//  val config = ConfigFactory.parseFile(new File("message.conf")).resolve()
  val RABBITMQ_HOST = conf.getString("rabbitmq.host")
  val RABBITMQ_QUEUE = conf.getString("rabbitmq.queue")
  val RABBITMQ_EXCHANGEE = conf.getString("rabbitmq.exchange")
}
