package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import scalaz.{ Validation, Failure, Success }
import java.util.UUID

abstract class RecordBinderTest[Rdf <: RDF]()(implicit diesel: Diesel[Rdf]) extends WordSpec with MustMatchers {

  import diesel._
  import ops._

  case class Person(id: UUID, name: String, address: Address)

  object Person {

    val clazz = uri("http://example.com/Person#class")

    val uriT = uriTemplate[UUID]("http://example.com/person/{id}") { uuid =>
      try { Success(UUID.fromString(uuid)) } catch { case _: IllegalArgumentException => Failure(WrongExpectation(uuid + " cannot be made a UUID")) }
    }
    val name = property[String](foaf.name)
    val address = property[Address](foaf("address"))

    implicit val binder = pgb[Person](uriT, name, address)(Person.apply, Person.unapply)

  }

  sealed trait Address

  object Address {

    val clazz = uri("http://example.com/Address#class")
    implicit val classUris = classUrisFor[Address](clazz)

    // not sure if this could be made more general, nor if we actually want to do that
    implicit val binder: PointedGraphBinder[Rdf, Address] = new PointedGraphBinder[Rdf, Address] {
      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, Address] =
        Unknown.binder.fromPointedGraph(pointed) orElse VerifiedAddress.binder.fromPointedGraph(pointed)

      def toPointedGraph(address: Address): PointedGraph[Rdf] = address match {
        case va: VerifiedAddress => VerifiedAddress.binder.toPointedGraph(va)
        case Unknown => Unknown.binder.toPointedGraph(Unknown)
      }
    }

  }

  case object Unknown extends Address {

    val clazz = uri("http://example.com/Unknown#class")
    implicit val classUris = classUrisFor[Unknown.type](clazz, Address.clazz)

    // there is a question about constants and the classes they live in
    implicit val binder: PointedGraphBinder[Rdf, Unknown.type] = constant(this, uri("http://example.com/Unknown#thing")) withClasses classUris

  }

  case class VerifiedAddress(id: String, label: String, city: City) extends Address

  object VerifiedAddress {

    val clazz = uri("http://example.com/VerifiedAddress#class")
    implicit val classUris = classUrisFor[VerifiedAddress](clazz, Address.clazz)

    val id = uriTemplate[String]("#{id}")(Success(_))
    val label = property[String](foaf("label"))
    val city = property[City](foaf("city"))

    implicit val ci = classUrisFor[VerifiedAddress](clazz)

    implicit val binder = pgb[VerifiedAddress](id, label, city)(VerifiedAddress.apply, VerifiedAddress.unapply)

  }

  case class City(cityName: String)

  object City {

    val clazz = uri("http://example.com/City#class")
    implicit val classUris = classUrisFor[City](clazz)

    val cityName = property[String](foaf("cityName"))

    implicit val binder: PointedGraphBinder[Rdf, City] =
      pgb[City](cityName)(City.apply, City.unapply) withClasses classUris

  }

  val city = City("Cambridge")
  val verifiedAddress = VerifiedAddress("qwerty", "32 Vassar st", city)
  val person = Person(UUID.randomUUID(), "betehess", verifiedAddress)

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

}
