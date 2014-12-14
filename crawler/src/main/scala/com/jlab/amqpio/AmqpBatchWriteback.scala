package com.jlab.amqpio

import com.jlab.common.genericBatchReseller
import com.jlab.process.{Data, Control}
import com.rabbitmq.client.{MessageProperties, Channel}
import configuration.Config

/**
 * Created by scorpiovn on 12/13/14.
 */
class AmqpBatchWriteback(config: Config, control: Control, chan: Channel) extends genericBatchReseller[Data](control) {

  //val log = Logging(context.system, this)
  val exch= config("exchange")

  def resell(batch: List[Data]) = {
    batch match {
      case x::xs => {
        val deliveryTag= (x get "fetch_queue_delivery_tag").toLong
        val key= try {
          (x get "fetch_status_code") match {
            case "200" => "200"
            case s if s.matches("^4\\d\\d") => "4xx"
            case _ => "Error"
          }
        } catch { case _ => "Error" }
        val fqp= "fetch_routing_key"
        val fullkey = if(x exists fqp) { x(fqp)+":"+key } else { key }
        chan.basicPublish(exch, fullkey, MessageProperties.PERSISTENT_TEXT_PLAIN, x.toBytes)
        chan.basicAck(deliveryTag, false)
        log.info("Publishing message to "+exch+" and acking delivery tag "+deliveryTag)
        resell(xs)
      }
      case Nil => ()
    }
  }

}

