package core

import akka.actor.{Props, Actor}
import java.util.UUID
import core.FruitActor.FruitPojo

object FruitActor {

  case class FruitPojo(url:String, content:String) {}

}

class FruitActor extends Actor {
  def receive: Receive = {
    case _  =>
      sender ! new FruitPojo("banana", "abc")
  }
}




