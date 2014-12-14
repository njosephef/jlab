package com.jlab.process

import FileIO.{FileBatchWriteback, FileBatchPrefetcher}
import akka.actor.{ActorRef, Props, ActorSystem}
import com.jlab.amqpio.{AmqpBatchWriteback, AmqpBatchPrefetcher}
import com.jlab.http.{HttpFetcher, HttpManager}
import configuration.Config

/**
 * Created by scorpiovn on 12/13/14.
 */

case class ClassNotFound(klass: String) extends Exception

class Pipeline(config: Config, control: Control, asys: ActorSystem) {


  val amqpConnection = {
    (config exists "amqp") match {
      case true => newAmqpConnection(config.getObject("amqp"))
      case _ => null
    }
  }

  val prefetch = newPrefetcher(config.getObject("prefetcher"))

  val writeback = newWriteback(config.getObject("writeback"))

  val httpManager = new HttpManager(config.getObject("httpManager"), config)

  val fetchers = (config unwrapArray "httpFetchers") map (f => asys.actorOf(Props(new HttpFetcher(f, prefetch, writeback, httpManager)) /*, name= "fetcher"*/))

  def start(): Unit = {
    //prefetch.start
    //writeback.start
    //fetchers map ( f => f.start )
  }

  def stop(): Unit = {
    //prefetch.stop
    //writeback.stop
    //fetchers map ( f => f.stop )
  }

  def newPrefetcher(config: Config): ActorRef = {
    val klass = config("class")
    klass match {
      case "FileBatchPrefetcher" => asys.actorOf(Props(new FileBatchPrefetcher(config, control)))
      case "AmqpBatchPrefetcher" => asys.actorOf(Props(new AmqpBatchPrefetcher(config, control, amqpConnection._2)))
      case (e: String) => throw (new ClassNotFound(e))
    }
  }

  def newWriteback(config: Config): ActorRef = {
    val klass = config("class")
    klass match {
      case "FileBatchWriteback" => asys.actorOf(Props(new FileBatchWriteback(config, control)))
      case "AmqpBatchWriteback" => asys.actorOf(Props(new AmqpBatchWriteback(config, control, amqpConnection._2)))
      case (e: String) => throw (new ClassNotFound(e))
    }
  }

  def newAmqpConnection(config: Config) = {
    import com.rabbitmq.client._
    val factory = new ConnectionFactory() // This will come from the config
    /* factory.setUri() doesn't work, not sure why
     (config getOption "uri") match {
      case Some(uri:String) => //factory.setUri(uri)
      case _ => ()
    } */
    val l = List(("user", factory.setUsername(_)),
        ("password", factory.setPassword(_)),
        ("host", factory.setHost(_)),
        ("port", ((s: String) => factory.setPort(s.toInt))),
        ("virtualHost", factory.setVirtualHost(_))
      )
    l foreach (x => if (config exists x._1) {
      x._2(config(x._1))
    })

    val conn = factory.newConnection()
    val chan = conn.createChannel()
    println(conn)
    println(chan)
    (conn, chan)
  }

}
