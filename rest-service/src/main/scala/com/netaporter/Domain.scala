package com.netaporter

import org.jlab.model.{Article}

// Messages

trait RestMessage

case class GetPetsWithOwners(petNames: List[String]) extends RestMessage
case class PetsWithOwners(pets: Seq[EnrichedPet]) extends RestMessage

case class GetArticle(urlRequest: UrlRequest) extends RestMessage
case class GetExtractedArticle(article: Article) extends RestMessage
case class PrettyArticle(article: Article) extends RestMessage

case class UrlRequest(url: String) extends RestMessage

// Domain objects

case class Pet(name: String) {
  def withOwner(owner: Owner) = EnrichedPet(name, owner)
}

case class Owner(name: String)

case class EnrichedPet(name: String, owner: Owner)

case class Error(message: String)

case class Validation(message: String)

// Exceptions

case object PetOverflowException extends Exception("PetOverflowException: OMG. Pets. Everywhere.")