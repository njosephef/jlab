package main.scala.org.jlab.rpc

import akka.actor.{Actor, Props, ActorSystem}
import org.jlab.model.Message

/**
 * Created by scorpiovn on 1/14/15.
 */
object RemoteFetchService extends App {
  val system = ActorSystem("simple-rpc")
  val remoteActor = system.actorOf(Props[RemoteFetchActor], name = "RemoteFetchActor")
}

class RemoteFetchActor extends Actor {
  def receive = {
    case Message(msg) =>
      println(s"RemoteActor received message '$msg'")
      sender ! Message("Hello from the RemoteActor")
    case _ => println("unknown message")
  }
}
