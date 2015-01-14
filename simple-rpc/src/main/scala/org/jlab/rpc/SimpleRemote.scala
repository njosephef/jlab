package main.scala.org.jlab.rpc

import akka.actor.{Actor, Props, ActorSystem}

/**
 * Created by scorpiovn on 1/14/15.
 */
object SimpleRemote extends App {
  val system = ActorSystem("simple-rpc")
  val remoreActor = system.actorOf(Props[RemoteActor], name = "RemoteActor")
}

class RemoteActor extends Actor {
  def receive = {
    case msg: String =>
      println(s"RemoteActor received message '$msg'")
      sender ! "Hello from the RemoteActor"
  }
}
