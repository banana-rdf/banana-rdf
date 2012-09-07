package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import scalaz._
import scalaz.Scalaz._
import java.util.UUID
import org.w3.banana.LinkedDataStore._
import akka.actor.ActorSystem
import akka.util.Timeout
import org.w3.banana.util._

abstract class LinkedDataStoreTest[Rdf <: RDF](
  store: RDFStore[Rdf, BananaFuture])(
    implicit diesel: Diesel[Rdf])
    extends WordSpec with MustMatchers with TestHelper {

  import diesel._

  val objects = new ObjectExamples
  import objects._

  val address1 = VerifiedAddress("32 Vassar st", City("Cambridge"))
  val address2 = VerifiedAddress("rue des poissons", City("Paris"))
  val person = Person("betehess")

  val graphStore = GraphStore[Rdf, BananaFuture](store)

  class LinkedPerson(uri: Rdf#URI, graphStore: GraphStore[Rdf, BananaFuture])(implicit diesel: Diesel[Rdf]) {

    lazy val bananaResource: BananaFuture[LinkedDataResource[Rdf]] = graphStore.get(uri)

    def getAddresses(): BananaFuture[Set[Address]] = {
      for {
        ldr <- bananaResource
        uris <- (ldr.resource / Person.address).asSet[Rdf#URI].bf
        resources <- graphStore.get(uris)
        addresses <- resources.map(_.resource.as[Address]).sequence[BananaValidation, Address].bf
      } yield {
        addresses
      }
    }

    def linkAddress(addressURI: Rdf#URI): BananaFuture[Unit] =
      graphStore.append(uri, uri -- foaf("address") ->- addressURI)

  }

  def linkedPerson(ldr: LinkedDataResource[Rdf]): LinkedPerson =
    new LinkedPerson(ldr.uri, graphStore) {
      import org.w3.banana.util._
      // just a little optimization in the case we already have the resource at hand
      override lazy val bananaResource = ldr.bf
    }

  def linkedPerson(uri: Rdf#URI): LinkedPerson = new LinkedPerson(uri, graphStore)

  "saving a person, 2 addresses and then retrieve them" in {

    val r = for {
      personUri <- graphStore.post(Person.container, person.toPG)
      lp = linkedPerson(personUri)
      address1Uri <- graphStore.post(personUri, address1.toPG)
      address2Uri <- graphStore.post(personUri, address2.toPG)
      _ <- lp.linkAddress(address1Uri)
      _ <- lp.linkAddress(address2Uri)
      personResource <- graphStore.get(personUri)
      addresses <- linkedPerson(personResource).getAddresses()
    } yield {
      addresses
    }

    val addresses = r.await().map(_.toSet)
    addresses must be(Success(Set(address1, address2)))

  }

}
