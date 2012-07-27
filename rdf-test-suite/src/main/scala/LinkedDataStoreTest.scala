package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import scalaz.{ Validation, Failure, Success }
import java.util.UUID
import org.w3.banana.LinkedDataStore._
import akka.actor.ActorSystem
import akka.util.Timeout
import org.w3.banana.util._

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

    for {
      personNode <- store.post(Person.container, person.toPG)
      personUri <- personNode.as[Rdf#URI].bf
      address1Uri <- store.post(personUri, address1.toPG)
      address2Uri <- store.post(personUri, address2.toPG)
      someData = (
        personUri
        -- foaf("address") ->- address1Uri
        -- foaf("address") ->- address2Uri
      )
      _ <- store.append(personUri, someData)
      ldr <- store.get(personUri)
    } {
      println("!!!!!!!!!! "+ldr)
    }

  }

}
