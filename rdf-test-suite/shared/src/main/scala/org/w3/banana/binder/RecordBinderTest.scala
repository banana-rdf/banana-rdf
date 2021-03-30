package org.w3.banana.binder

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import org.w3.banana._

import scala.util._


class RecordBinderTest[Rdf <: RDF](implicit
  ops: RDFOps[Rdf],
  recordBinder: RecordBinder[Rdf]
) extends AnyWordSpec with Matchers {

  import ops._

  val objects = new ObjectExamples
  import objects._

  val city = City("Paris", Set("Panam", "Lutetia"))
  val verifiedAddress = VerifiedAddress("32 Vassar st", city)
  val person = Person("Alexandre Bertails")
  val personWithNickname = person.copy(nickname = Some("betehess"))
  val me = Me("Name")

  "serializing and deserializing a City" in {
    city.toPG.as[City] shouldEqual Success(city)
    val paris = URI("http://ontology.example/Paris")
    val parisPG: PointedGraph[Rdf] = (
      paris.a(City.clazz)
      -- foaf("cityName") ->- "Paris"
      -- foaf("otherNames") ->- "Panam"
      -- foaf("otherNames") ->- "Lutetia"
    )
    println("parisPG ="+parisPG.graph)
    println("city.toPG = "+city.toPG.graph)
    city.toPG.graph.isIsomorphicWith(parisPG.graph) shouldEqual true
    parisPG.as[City] shouldEqual Success(city)
  }

  "graph constant pointer" in {
    me.toPG.pointer shouldEqual URI("http://example.com#me")
  }

  "graph pointer based on record fields" in {
    city.toPG.pointer shouldEqual URI("http://example.com/Paris")
  }

  "serializing and deserializing a VerifiedAddress" in {
    verifiedAddress.toPG.as[VerifiedAddress] shouldEqual Success(verifiedAddress)
  }

  "serializing and deserializing a VerifiedAddress as an Address" in {
    verifiedAddress.toPG.as[Address] shouldEqual Success(verifiedAddress)
  }

  "serializing and deserializing an Unknown address" in {
    Unknown.toPointedGraph.as[Unknown.type] shouldEqual Success(Unknown)
  }

  "serializing and deserializing an Unknown address as an Address" in {
    Unknown.toPointedGraph.as[Address] shouldEqual Success(Unknown)
  }

  "serializing and deserializing a Person" in {
    person.toPointedGraph.as[Person] shouldEqual Success(person)
  }

  "serializing and deserializing a Person with a nickname" in {
    personWithNickname.toPointedGraph.as[Person] shouldEqual Success(personWithNickname)
  }

}
