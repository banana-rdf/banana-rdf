package org.w3.banana.jasmine.test

import org.w3.banana._
import org.w3.banana.syntax._
import org.w3.banana.diesel._
import org.w3.banana.binder._
import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scalaz.Scalaz._
import scala.util._
import scala.collection.immutable.ListMap
import java.io._
import scala.concurrent.Future
import org.w3.banana.{ RDFStore => RDFStoreInterface }

import scala.scalajs.js
import scala.scalajs.test.JasmineTest

/**
 * Ported by Antonio Garrotte from rdf-test-suite in scala.tests to Jasmine Tests
 */
abstract class RecordBinderJasmineTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf],
  recordBinder: RecordBinder[Rdf])
    extends JasmineTest {

  import ops._
  import syntax._
  import recordBinder._

  val objects = new ObjectExamplesJasmine

  import objects._

  val city = City("Paris", Set("Panam", "Lutetia"))
  val verifiedAddress = VerifiedAddress("32 Vassar st", city)
  val person = Person("Alexandre Bertails")
  val personWithNickname = person.copy(nickname = Some("betehess"))
  val me = Me("Name")

  describe("record binders") {

    it("serializing and deserializing a City") {
      expect(city.toPG.as[City] == Success(city)).toEqual(true)

      val expectedGraph = (
        URI("http://example.com/Paris").a(City.clazz)
        -- foaf("cityName") ->- "Paris"
        -- foaf("otherNames") ->- "Panam"
        -- foaf("otherNames") ->- "Lutetia"
      ).graph
      expect(city.toPG.graph.isIsomorphicWith(expectedGraph)).toEqual(true)
    }

    /*
    "serializing and deserializing a public key" in {
      import Cert._
      val rsaPg = rsa.toPG
      //todo: there is a bug below. The isomorphism does not work, even though it should.
      //    System.out.println(s"rsag=${rsaPg.graph}")
      //    val expectedGraph = (
      //      URI("#k") -- cert.modulus ->- rsa.getModulus.toByteArray
      //              -- cert.exponent ->- rsa.getPublicExponent
      //      ).graph
      //    System.out.println(s"expectedGraph=${expectedGraph}")
      //    rsaPg.graph.isIsomorphicWith(expectedGraph) must be(true)
      rsaPg.as[RSAPublicKey] should be(Success(rsa))
    }
    */

    it("graph constant pointer") {
      expect(me.toPG.pointer == URI("http://example.com#me")).toEqual(true)
    }

    it("graph pointer based on record fields") {
      expect(city.toPG.pointer == URI("http://example.com/Paris")).toEqual(true)
    }

    it("serializing and deserializing a VerifiedAddress") {
      expect(verifiedAddress.toPG.as[VerifiedAddress] == Success(verifiedAddress)).toEqual(true)
    }

    it("serializing and deserializing a VerifiedAddress as an Address") {
      expect(verifiedAddress.toPG.as[Address] == Success(verifiedAddress)).toEqual(true)
    }

    it("serializing and deserializing an Unknown address") {
      expect(Unknown.toPointedGraph.as[Unknown.type] == Success(Unknown)).toEqual(true)
    }

    it("serializing and deserializing an Unknown address as an Address") {
      expect(Unknown.toPointedGraph.as[Address] == Success(Unknown)).toEqual(true)
    }

    it("serializing and deserializing a Person") {
      expect(person.toPointedGraph.as[Person] == Success(person)).toEqual(true)
    }

    it("serializing and deserializing a Person with a nickname") {
      expect(personWithNickname.toPointedGraph.as[Person] == Success(personWithNickname)).toEqual(true)
    }

  }

}
