package com.jlab.fetch

import com.rabbitmq.client.{MessageProperties, Channel}

// Prefetching an AMQP queue
class amqpBatchPrefetcher(config: Config
                          , control: Control
                          , chan: Channel)
      extends genericBatchProducer[Data](
        config.getInt("batch_size")
        , config.getInt("threshold_in_batches")
        , config.getLongOption("timeout_ms")
        , control) {

  //val log = Logging(context.system, this)
  val queue = config("queue")

  override def init() = {
    // InitAMPQ
  }

  class EmptyQueue extends Exception

  def getMessage(): Data = {
    val autoAck = false
    val response = chan.basicGet(queue, autoAck)

    if (null == response) { throw new EmptyQueue }

    val body = response.getBody()
    val deliveryTag= response.getEnvelope().getDeliveryTag()
    log.info("Fetched message from " + queue +" with delivery tag " + deliveryTag)
    val d = Data.fromBytes(body)
    log.info(d.toJson())
    d + ("fetch_queue_delivery_tag", deliveryTag.toString)
  }

  override def getBatch(sz:Int):Option[List[Data]] = {
    def rec(c:Int, l:List[Data]):List[Data] = { try { if(0==c) { l } else { rec(c-1, getMessage()::l ) } } catch { case e:EmptyQueue => l } }

    val l= rec(sz, List[Data]())
    val ls= l.length
    if(ls>0) { log.info("Fetched "+ls.toString+" message(s) from "+queue) }
    if(l isEmpty) { None } else { return Some(l) }
  }
  
}

// Writing back to an AMQP exchange
class amqpBatchWriteback(config: Config, control: Control, chan: Channel) extends genericBatchReseller[Data](control) {

  //val log = Logging(context.system, this)
  val exch = config("exchange")
  log.info(exch)

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
//        log.info(x(fqp))
        val fullkey = if(x exists fqp) { x(fqp)+ ":" + key } else { key }
//        log.info(x("fetch_data"))
        chan.basicPublish(exch, fullkey, MessageProperties.PERSISTENT_TEXT_PLAIN, x.toBytes)
        chan.basicAck(deliveryTag, false)
        log.info("Publishing message to " + exch + " and acking delivery tag " + deliveryTag)
        resell(xs) 
      }
      case Nil => ()
    }
  }

}

