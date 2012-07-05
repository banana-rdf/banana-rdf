package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import scalaz.{ Validation, Failure, Success }
import java.util.UUID

abstract class ObjectBinderTest[Rdf <: RDF]()(implicit diesel: Diesel[Rdf]) extends WordSpec with MustMatchers {

  import diesel._
  import ops._

  case class Person(id: UUID, name: String, address: Address)

  object Person {

    val uri = uriTemplate[UUID]("http://example.com/person/{id}"){ uuid =>
      try { Success(UUID.fromString(uuid)) } catch { case _: IllegalArgumentException => Failure(WrongExpectation(uuid + " cannot be made a UUID")) }
    }
    val name = property[String](foaf.name)
    val address = property[Address](foaf.address)


//    implicit val binder = pgb[Person](uri, name)(Person.apply, Person.unapp


//    implicit val binder = pgb[Person](uri, name, address)(Person.apply, Person.unapply)

  }
//
//  sealed trait Address
//
//  object Address {
//
//    implicit val binder = new PBG[Rdf, Address] {
//      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, Address] =
//        Unknown.binder.fromPointedGraph(pointed) orElse VerifiedAddress.binder.fromPointedGraph(pointed)
//
//      def toPointedGraph(address: Address): PointedGraph[Rdf] = address match {
//        case va: VerifiedAddress => VerifiedAddress.binder.toPointedGraph(va)
//        case Unknown => Unknown.binder.toPointedGraph(Unknown)
//      }
//    }
//
//  }
//
//  case object Unknown extends Address {
//
//    implicit val binder = constant(Unknown, "http://example.com/Unknown#thing")
//
//  }
//
//  case class VerifiedAddress(id: String, label: String, city: City) extends Address
//
//  object VerifiedAddress {
//
//    val id = absoluteUri[String]("http://example.com/person/{personId}#{id}", "id")
//    val label = property[String](foaf("label"))
//    val city = property[City](foaf("city"))
//
//    implicit val binder = pgb[VerifiedAddress]((id, label, city))(VerifiedAddress.apply, VerifiedAddress.unapply)
//
//  }
//  
//  case class City(cityName: String)
//
//  object City {
//
//    val cityName = property[String](foaf("cityName"))
//
//    implicit val binder = pgb[City](cityName)(City.apply, City.unapply)
//
//  }

}
