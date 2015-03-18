package org.w3.banana.binder

import org.w3.banana._, syntax._, diesel._
import scala.util._
import com.inthenow.zcheck.SpecLite

class RecordBinderTest[Rdf <: RDF](implicit
  ops: RDFOps[Rdf],
  recordBinder: RecordBinder[Rdf]
) extends SpecLite {

  import ops._

  val objects = new ObjectExamples
  import objects._

  val city = City("Paris", Set("Panam", "Lutetia"))
  val verifiedAddress = VerifiedAddress("32 Vassar st", city)
  val person = Person("Alexandre Bertails")
  val personWithNickname = person.copy(nickname = Some("betehess"))
  val me = Me("Name")

  "serializing and deserializing a City" in {
    city.toPG.as[City] must_==(Success(city))

    val expectedGraph = (
      URI("http://example.com/Paris").a(City.clazz)
      -- foaf("cityName") ->- "Paris"
      -- foaf("otherNames") ->- "Panam"
      -- foaf("otherNames") ->- "Lutetia"
    ).graph
    check(city.toPG.graph.isIsomorphicWith(expectedGraph))
  }

  "graph constant pointer" in {
    me.toPG.pointer must_==(URI("http://example.com#me"))
  }

  "graph pointer based on record fields" in {
    city.toPG.pointer must_==(URI("http://example.com/Paris"))
  }

  "serializing and deserializing a VerifiedAddress" in {
    verifiedAddress.toPG.as[VerifiedAddress] must_==(Success(verifiedAddress))
  }

  "serializing and deserializing a VerifiedAddress as an Address" in {
    verifiedAddress.toPG.as[Address] must_==(Success(verifiedAddress))
  }

  "serializing and deserializing an Unknown address" in {
    Unknown.toPointedGraph.as[Unknown.type] must_==(Success(Unknown))
  }

  "serializing and deserializing an Unknown address as an Address" in {
    Unknown.toPointedGraph.as[Address] must_==(Success(Unknown))
  }

  "serializing and deserializing a Person" in {
    person.toPointedGraph.as[Person] must_==(Success(person))
  }

  "serializing and deserializing a Person with a nickname" in {
    personWithNickname.toPointedGraph.as[Person] must_==(Success(personWithNickname))
  }

}
