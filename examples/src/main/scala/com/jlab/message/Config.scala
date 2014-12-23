package com.jlab.message

import com.typesafe.config.ConfigFactory

/**
 * Created by scorpiovn on 12/22/14.
 */
object Config {
  val RABBITMQ_HOST = ConfigFactory.load().getString("rabbitmq.host");
  val RABBITMQ_QUEUE = ConfigFactory.load().getString("rabbitmq.queue");
  val RABBITMQ_EXCHANGEE = ConfigFactory.load().getString("rabbitmq.exchange");
}
