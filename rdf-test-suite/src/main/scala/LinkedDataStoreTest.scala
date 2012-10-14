package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import scala.concurrent._
import org.w3.banana.LinkedDataStore._

abstract class LinkedDataStoreTest[Rdf <: RDF](
  store: RDFStore[Rdf, Future])(
    implicit diesel: Diesel[Rdf])
    extends WordSpec with MustMatchers with TestHelper {

  import diesel._

  val foaf = FOAFPrefix[Rdf]

  val objects = new ObjectExamples
  import objects._

  val address1 = VerifiedAddress("32 Vassar st", City("Cambridge"))
  val address2 = VerifiedAddress("rue des poissons", City("Paris"))
  val person = Person("betehess")

  class LinkedPerson(uri: Rdf#URI, store: RDFStore[Rdf, Future])(implicit diesel: Diesel[Rdf]) {

    import diesel._
    import ops._

    lazy val bananaResource: Future[LinkedDataResource[Rdf]] = store.GET(uri)

    def getAddresses(): Future[Set[Address]] = {
      for {
        ldr <- bananaResource
        uris <- (ldr.resource / Person.address).asSet[Rdf#URI].asFuture
        resources <- store.GET(uris)
        addresses <- resources.map(_.resource.as[Address].get).asFuture
      } yield {
        addresses
      }
    }

    def linkAddress(addressURI: Rdf#URI): Future[Unit] =
      store.POST(uri, uri -- foaf("address") ->- addressURI)

  }

  def linkedPerson(ldr: LinkedDataResource[Rdf]): LinkedPerson =
    new LinkedPerson(ldr.uri, store) {
      override lazy val bananaResource = ldr.asFuture
    }

  def linkedPerson(uri: Rdf#URI): LinkedPerson = new LinkedPerson(uri, store)

  "saving a person, 2 addresses and then retrieve them" in {

    val r = for {
      personUri <- store.POSTToCollection(Person.container, person.toPG)
      lp = linkedPerson(personUri)
      address1Uri <- store.POSTToCollection(personUri, address1.toPG)
      address2Uri <- store.POSTToCollection(personUri, address2.toPG)
      _ <- lp.linkAddress(address1Uri)
      _ <- lp.linkAddress(address2Uri)
      personResource <- store.GET(personUri)
      addresses <- linkedPerson(personResource).getAddresses()
    } yield {
      addresses
    }

    val addresses = r.getOrFail().toSet
    addresses must be(Set(address1, address2))

  }

}
