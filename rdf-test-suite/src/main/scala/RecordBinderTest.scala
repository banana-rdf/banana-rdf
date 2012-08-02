package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import scalaz.{ Validation, Failure, Success }
import java.util.UUID

abstract class RecordBinderTest[Rdf <: RDF]()(implicit diesel: Diesel[Rdf]) extends WordSpec with MustMatchers {

  import diesel._

  val objects = new ObjectExamples
  import objects._

  val city = City("Paris", Set("Panam", "Lutetia"))
  val verifiedAddress = VerifiedAddress("32 Vassar st", city)
  val person = Person("Alexandre Bertails")
  val personWithNickname = person.copy(nickname = Some("betehess"))

  "serializing and deserializing a City" in {
    city.toPG.as[City] must be(Success(city))
  }

  "serializing and deserializing a VerifiedAddress" in {
    verifiedAddress.toPG.as[VerifiedAddress] must be(Success(verifiedAddress))
  }

  "serializing and deserializing a VerifiedAddress as an Address" in {
    verifiedAddress.toPG.as[Address] must be(Success(verifiedAddress))
  }

  "serializing and deserializing an Unknown address" in {
    Unknown.toPointedGraph.as[Unknown.type] must be(Success(Unknown))
  }

  "serializing and deserializing an Unknown address as an Address" in {
    Unknown.toPointedGraph.as[Address] must be(Success(Unknown))
  }

  "serializing and deserializing a Person" in {
    person.toPointedGraph.as[Person] must be(Success(person))
  }

  "serializing and deserializing a Person with a nickname" in {
    personWithNickname.toPointedGraph.as[Person] must be(Success(personWithNickname))
  }

}
