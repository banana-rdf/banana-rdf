package org.w3.banana.binder

import org.w3.banana._
import syntax._
import diesel._
import org.scalatest.WordSpec

import scala.util._

class RecordBinderTest[Rdf <: RDF](implicit
  ops: RDFOps[Rdf],
  recordBinder: RecordBinder[Rdf]
) extends WordSpec {

  import ops._

  val objects = new ObjectExamples
  import objects._

  val city = City("Paris", Set("Panam", "Lutetia"))
  val verifiedAddress = VerifiedAddress("32 Vassar st", city)
  val person = Person("Alexandre Bertails")
  val personWithNickname = person.copy(nickname = Some("betehess"))
  val me = Me("Name")

  "serializing and deserializing a City" in {
    city.toPG.as[City] === (Success(city))

    val expectedGraph = (
      URI("http://example.com/Paris").a(City.clazz)
      -- foaf("cityName") ->- "Paris"
      -- foaf("otherNames") ->- "Panam"
      -- foaf("otherNames") ->- "Lutetia"
    ).graph
    assert(city.toPG.graph.isIsomorphicWith(expectedGraph))
  }

  "graph constant pointer" in {
    assert(me.toPG.pointer === (URI("http://example.com#me")))
  }

  "graph pointer based on record fields" in {
    assert(city.toPG.pointer === (URI("http://example.com/Paris")))
  }

  "serializing and deserializing a VerifiedAddress" in {
    assert(verifiedAddress.toPG.as[VerifiedAddress] === (Success(verifiedAddress)))
  }

  "serializing and deserializing a VerifiedAddress as an Address" in {
    assert(verifiedAddress.toPG.as[Address] === (Success(verifiedAddress)))
  }

  "serializing and deserializing an Unknown address" in {
    assert(Unknown.toPointedGraph.as[Unknown.type] === (Success(Unknown)))
  }

  "serializing and deserializing an Unknown address as an Address" in {
    assert(Unknown.toPointedGraph.as[Address] === (Success(Unknown)))
  }

  "serializing and deserializing a Person" in {
    assert(person.toPointedGraph.as[Person] === (Success(person)))
  }

  "serializing and deserializing a Person with a nickname" in {
    assert(personWithNickname.toPointedGraph.as[Person] === (Success(personWithNickname)))
  }

}
