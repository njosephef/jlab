package com.jlab.common

import akka.actor.ActorSystem
import com.jlab.process.{Pipeline, PipelineControl}
import configuration.Config

/**
 * Created by scorpiovn on 12/13/14.
 */
class System(config: Config) {

  // Build the system
  val asys = System.asys
  val name = config("instance")
  val control = new PipelineControl(config)

  val pipelines =
    if (config exists "proxies") {
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

  val asys = ActorSystem("Fureteur")
  var i = 0

  def unique(s: String) = {
    i += 1
    s + i.toString
  }
}
