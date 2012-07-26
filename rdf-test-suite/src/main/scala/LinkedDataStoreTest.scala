package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import scalaz.{ Validation, Failure, Success }
import java.util.UUID
import org.w3.banana.LinkedDataStore._
import akka.actor.ActorSystem
import akka.util.Timeout

abstract class LinkedDataStoreTest[Rdf <: RDF](
  syncStore: RDFStore[Rdf])(
    implicit diesel: Diesel[Rdf])
    extends WordSpec with MustMatchers {

  import diesel._

  val system = ActorSystem("jena-asynsparqlquery-test", util.AkkaDefaults.DEFAULT_CONFIG)
  implicit val timeout = Timeout(1000)
  val store = AsyncRDFStore(syncStore, system)

  val objects = new ObjectExamples
  import objects._

  val address1 = VerifiedAddress("32 Vassar st", City("Cambridge"))
  val address2 = VerifiedAddress("rue des poissons", City("Paris"))
  val person = Person("betehess")

  "foo" in {

    val u1 = Person.makeUri()
    val u2 = Person.makeUri()
    val coll = uri("http://example.com/persons")

    val pointed = person.toPG -- Person.address ->- address1.toPG
    println(pointed)

    for {
      _ <- store.append(u1, pointed)
      rPointed <- store.get(u1)
    } {
      println(rPointed)
    }

    for {
      docUri <- store.post(coll, pointed)
      rPointed <- store.get(docUri)

    } {
      println(rPointed)
    }

  }

}
