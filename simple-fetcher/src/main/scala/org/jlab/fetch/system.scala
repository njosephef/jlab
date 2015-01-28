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
    actorSystem.actorOf(Props(new BatchPrefetcher(config, control)))
  }

  def createWriteback(config: Config): ActorRef = {
    actorSystem.actorOf(Props(new BatchWriteback(config, control)))
  }
}