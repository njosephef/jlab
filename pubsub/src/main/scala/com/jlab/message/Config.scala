package com.jlab.message

import com.typesafe.config.ConfigFactory

/**
 * Created by scorpiovn on 12/22/14.
 */
object Config {
  val conf = ConfigFactory.load()
//  val config = ConfigFactory.parseFile(new File("message.conf")).resolve()
  val RABBITMQ_HOST = conf.getString("rabbitmq.host")
  val RABBITMQ_QUEUE_URL = conf.getString("rabbitmq.queue.url")
  val RABBITMQ_QUEUE_HTML = conf.getString("rabbitmq.queue.html")
  val RABBITMQ_EXCHANGE_HTML = conf.getString("rabbitmq.exchange.html")
}
