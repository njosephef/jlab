package org.jlab.fetch

import argonaut.{Parse, CodecJson}
import argonaut._, Argonaut._

/**
 * Created by scorpiovn on 2/3/15.
 */
object QuickStartExample extends App {
/*  val input = """
[
{ "name": "Mark", "age": 191 },
{ "name": "Fred", "age": 33, "greeting": "hey ho, lets go!" },
{ "name": "Barney", "age": 35, "address": {
"street": "rock street", "number": 10, "post_code": 2039
}}
]
              """*/

  val input = """
{ "name": "Mark", "age": 191 }
              """

  // parse the string as json, attempt to decode it to a list of person,
  // otherwise just take it as an empty list.
  val people = Parse.decodeOption[Person](input).getOrElse(Nil)

  // work with your data types as you normally would
  /*val nice = people.map(person =>
    person.copy(greeting = person.greeting.orElse(Some("Hello good sir!"))))*/

  // convert back to json, and then to a pretty printed string, alternative
  // ways to print may be nospaces, spaces2, or a custom format
println(people)
//  val result = people.asJson
//  println(result.spaces4)

//  assert(result.array.exists(_.length == 3))
}


case class Address(street: String, number: Int, postcode: Int)

object Address {
  // Define codecs easily from case classes
  implicit def AddressCodecJson: CodecJson[Address] =
    casecodec3(Address.apply, Address.unapply)("street", "number", "post_code")
}

case class Person(name: String, age: Int, address: Option[Address], greeting: Option[String])

object Person {
  implicit def PersonCodecJson: CodecJson[Person] =
    casecodec4(Person.apply, Person.unapply)("name", "age", "address", "greeting")
}
