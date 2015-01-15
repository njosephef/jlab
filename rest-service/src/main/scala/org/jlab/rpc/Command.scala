package org.jlab.rpc

import akka.actor._
import akka.remote.RemoteScope
import org.jlab.model.{Syn, LazyRemoteActor, Message}

/**
 * Created by scorpiovn on 1/15/15.
 */
object Command extends App{
  val system = ActorSystem("simple-rpc")
  val cncActor = system.actorOf(Props[CommandActor])
  cncActor ! Syn
}

class CommandActor extends Actor {

  def receive = {
    case Syn =>
      //Get the remote actorsystem path
      val addr = AddressFromURIString("akka.tcp://simple-rpc@127.0.0.1:5150/user/LazyRemoteActor")
      //Push the actor to the remote system
      val remoteActor = context.actorOf((Props[LazyRemoteActor]).withDeploy(
        Deploy(scope = RemoteScope(addr))))

      remoteActor ! Message("command request")
    case Message(msg) =>
      println(msg)
  }
}

