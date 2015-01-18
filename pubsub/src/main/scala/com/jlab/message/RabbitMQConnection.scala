package com.jlab.message

import com.rabbitmq.client.{Connection, ConnectionFactory}

/**
 * Created by scorpiovn on 12/22/14.
 */
object RabbitMQConnection {

  private val connection: Connection = null;

  /**
   * Return a connection if one doesn't exist.
   * Else create a new one
   */
  def getConnection(): Connection = {
    connection match {
      case null => {
        val factory = new ConnectionFactory();
        factory.setHost(Config.RABBITMQ_HOST);
        factory.newConnection();
      }
      case _ => connection
    }
  }
}
