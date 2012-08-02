package org.w3.banana

import scalaz.{ Validation, Failure, Success }
import java.util.UUID

class ObjectExamples[Rdf <: RDF]()(implicit diesel: Diesel[Rdf]) {

  import diesel._
  import ops._

  case class Person(name: String, nickname: Option[String] = None)

  object Person {

    val clazz = uri("http://example.com/Person#class")
    implicit val classUris = classUrisFor[Person](clazz)

    val name = property[String](foaf.name)
    val nickname = optional[String](foaf("nickname"))
    val address = property[Address](foaf("address"))

    implicit val container = uri("http://example.com/persons/")
    implicit val binder = pgb[Person](name, nickname)(Person.apply, Person.unapply)

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

  case class VerifiedAddress(label: String, city: City) extends Address

  object VerifiedAddress {

    val clazz = uri("http://example.com/VerifiedAddress#class")
    implicit val classUris = classUrisFor[VerifiedAddress](clazz, Address.clazz)

    val label = property[String](foaf("label"))
    val city = property[City](foaf("city"))

    implicit val ci = classUrisFor[VerifiedAddress](clazz)

    implicit val binder = pgb[VerifiedAddress](label, city)(VerifiedAddress.apply, VerifiedAddress.unapply) withClasses classUris

  }

  case class City(cityName: String, otherNames: Set[String] = Set.empty)

  object City {

    val clazz = uri("http://example.com/City#class")
    implicit val classUris = classUrisFor[City](clazz)

    val cityName = property[String](foaf("cityName"))
    val otherNames = set[String](foaf("otherNames"))

    implicit val binder: PointedGraphBinder[Rdf, City] =
      pgb[City](cityName, otherNames)(City.apply, City.unapply) withClasses classUris

  }

}
