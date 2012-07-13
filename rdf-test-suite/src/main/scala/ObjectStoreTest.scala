package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import scalaz.{ Validation, Failure, Success }
import java.util.UUID
import org.w3.banana.ObjectStore._
import akka.actor.ActorSystem
import akka.util.Timeout

abstract class ObjectStoreTest[Rdf <: RDF](
  syncStore: RDFStore[Rdf])(
  implicit diesel: Diesel[Rdf])
extends WordSpec with MustMatchers {

  import diesel._

  val system = ActorSystem("jena-asynsparqlquery-test", util.AkkaDefaults.DEFAULT_CONFIG)
  implicit val timeout = Timeout(1000)
  val store = AsyncRDFStore(syncStore, system)

  val objects = new ObjectExamples
  import objects._

  // look ma', it's really easy to define new binders!
  // type Person2 = (String, String)
  // implicit val classUris = classUrisFor[Person2](Person.clazz)
  // implicit val person2Binder = pgb[Person2](Person.name, Person.nickname)(Tuple2.apply, Tuple2.unapply) withClasses classUris

  val address1 = VerifiedAddress("qwerty", "32 Vassar st", City("Cambridge"))
  val address2 = VerifiedAddress("azerty", "rue des poissons", City("Paris"))
  val person = Person(UUID.randomUUID(), "betehess")

  "foo" in {

    for {
      _ <- store.save(person.toPG -- Person.address ->- address2)
      _ <- store.save(address1.toPG, person.toUri)
      addresses <- store.getAll[Address](person.toUri)
    } {
      println(addresses)
    }

  }

}
