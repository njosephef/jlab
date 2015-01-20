package com.netaporter.core

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy}
import com.netaporter.clients.OwnerClient.{GetOwnersForPets, OwnersForPets}
import com.netaporter.clients.PetClient.{GetPets, Pets}
import com.netaporter.{GetPetsWithOwners, Owner, Pet, _}

/**
 * The job of this Actor in our application core is to service a request
 * that asks for a list of pets by their names along with their owners.
 *
 * This actor will have the responsibility of making two requests and then aggregating them together:
 *  - One requests for a list the pets by their names
 *  - A separate request for a list of owners by their pet names
 */
class GetPetsWithOwnersActor(petService: ActorRef, ownerService: ActorRef) extends Actor with ActorLogging {

  var pets = Option.empty[Seq[Pet]]
  var owners = Option.empty[Seq[Owner]]

  def receive = {
    case GetPetsWithOwners(names) if names.size > 2 => throw PetOverflowException

    case GetPetsWithOwners(names) => {
      log.info("get pets request"); petService ! GetPets(names)
      log.info("get owners for pets"); ownerService ! GetOwnersForPets(names)
      log.info("--> waiting responses"); context.become(waitingResponses)
    }
  }

  def waitingResponses: Receive = {
    case Pets(petSeq) => {
      pets = Some(petSeq)
      replyIfReady
    }

    case OwnersForPets(ownerSeq) => {
      owners = Some(ownerSeq)
      replyIfReady
    }

    case f: Validation => context.parent ! f
  }

  def replyIfReady = if(pets.nonEmpty && owners.nonEmpty) {
      val petSeq = pets.head
      val ownerSeq = owners.head

      val enrichedPets = (petSeq zip ownerSeq).map { case (pet, owner) => pet.withOwner(owner) }
      context.parent ! PetsWithOwners(enrichedPets)
    }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}