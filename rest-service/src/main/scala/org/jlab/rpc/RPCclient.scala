package org.jlab.rpc

import akka.actor.{Actor, ActorSystem, Props}

/**
 * Created by scorpiovn on 1/14/15.
 */
object RPCclient extends App {
  implicit val system = ActorSystem("LocalSystem")
  val localActor = system.actorOf(Props[ClientActor], name = "LocalActor") // the local actor
  localActor ! "START"
}

class ClientActor extends Actor {
  // create the remote actor
  val remote = context.actorSelection("akka.tcp://HelloRemoteSystem@127.0.0.1:5150/user/RemoteActor")
  var counter = 0

  def receive = {
    case "START" =>
      remote ! "Hello from the LocalActor"
    case msg: String =>
      println(s"LocalActor received message: '$msg'")
      if (counter < 5) {
        sender ! "Hello back to you"
        counter += 1
      }
  }
}