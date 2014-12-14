package com.jlab.amqpio

import com.jlab.common.genericBatchProducer
import com.jlab.process.{Data, Control}
import com.rabbitmq.client.Channel
import configuration.Config

/**
 * Created by scorpiovn on 12/13/14.
 */
class AmqpBatchPrefetcher(config: Config,
                          control: Control,
                          chan: Channel)
  extends genericBatchProducer[Data](config.getInt("batch_size"), config.getInt("threshold_in_batches"), config.getLongOption("timeout_ms"), control) {

  //val log = Logging(context.system, this)
  val queue= config("queue")

  override def init() = {
    // InitAMPQ
  }

  class EmptyQueue extends Exception

  def getMessage():Data = {
    val autoAck= false
    val response= chan.basicGet(queue, autoAck)
    if (null==response) { throw new EmptyQueue }
    val ra= response.getBody()
    val deliveryTag= response.getEnvelope().getDeliveryTag()
    log.info("Fetched message from "+queue+" with delivery tag "+deliveryTag)
    val d= Data.fromBytes(ra)
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


