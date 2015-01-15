package org.jlab.model

import akka.actor.Actor

/**
 * Created by scorpiovn on 1/15/15.
 */
class LazyRemoteActor extends Actor {
  def receive = {
    case Message(msg) =>
      println(msg)
      sender ! Message(s"Lazy actor replies to '$msg'")
    case _ =>
  }
}
