package com.jlab.fetch

import akka.actor.{ActorRef, ActorSystem, Props}
import com.rabbitmq.client.ConnectionFactory

case class ClassNotFound(klass: String) extends Exception

class System(config: Config) {

  // Build the system
  val asys = System.actorSystem
  val name = config("instance")
  val control = new PipelineControl(config)

  val pipelines = if (config exists "proxies") {
      val proxies = (config unwrapArray "proxies") map (p => (p("host"), p("port")))
      proxies map (pkv =>
        (config unwrapArray "pipelines") map (c => new Pipeline(c ++ (("proxy_host", pkv._1) ::("proxy_port", pkv._2) :: Nil), control, asys))) flatten
    }
    else (config unwrapArray "pipelines") map (new Pipeline(_, control, asys))

  def start(): Unit = {
    pipelines map ((p) => p.start)
  }

}

object System {
  val actorSystem = ActorSystem("Fetcher")
  var identifier = 0

  def unique(s: String) = {
    identifier += 1
    s + identifier.toString
  }
}

class Pipeline(config: Config, control: Control, actorSystem: ActorSystem) {
  val amqpConnection = {
    (config exists "amqp") match {
      case true => createAmqpConnection(config.getObject("amqp"))
      case _ => null
    }
  }

  val prefetch = createPrefetcher(config.getObject("prefetcher"))

  val writeback = createWriteback(config.getObject("writeback"))

  val httpManager = new HttpManager(config.getObject("httpManager"), config)

  val fetchers = (config unwrapArray "httpFetchers") map (f => actorSystem.actorOf(Props(new HttpFetcher(f, prefetch, writeback, httpManager)) /*, name= "fetcher"*/))

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

  def createPrefetcher(config: Config): ActorRef = {
    val klass = config("class")
    klass match {
      case "fileBatchPrefetcher" => actorSystem.actorOf(Props(new fileBatchPrefetcher(config, control)))
      case "amqpBatchPrefetcher" => actorSystem.actorOf(Props(new amqpBatchPrefetcher(config, control, amqpConnection._2)))
      case (e: String) => throw (new ClassNotFound(e))
    }
  }

  def createWriteback(config: Config): ActorRef = {
    val klass = config("class")
    println("klass >>> " + klass)
    klass match {
      case "fileBatchWriteback" => actorSystem.actorOf(Props(new fileBatchWriteback(config, control)))
      case "amqpBatchWriteback" =>
        amqpConnection._2.exchangeDeclare(config("exchange"), "direct")
        actorSystem.actorOf(Props(new amqpBatchWriteback(config, control, amqpConnection._2)))
      case (e: String) => throw (new ClassNotFound(e))
    }
  }

  def createAmqpConnection(config: Config) = {
    val factory = new ConnectionFactory() // This will come from the config
    /* factory.setUri() doesn't work, not sure why
     (config getOption "uri") match {
      case Some(uri:String) => //factory.setUri(uri)
      case _ => ()
    } */
    
    val listUserConfig = List(("user", factory.setUsername(_)),
        ("password", factory.setPassword(_)),
        ("host", factory.setHost(_)),
        ("port", (s: String) => factory.setPort(s.toInt)),
        ("virtualHost", factory.setVirtualHost(_))
      )
    
    listUserConfig foreach ( x => if ( config exists x._1 ) { x._2(config(x._1)) } )

    val conn = factory.newConnection()
    val chan = conn.createChannel()

    println(conn)
    println(chan)

    (conn, chan)
  }
}