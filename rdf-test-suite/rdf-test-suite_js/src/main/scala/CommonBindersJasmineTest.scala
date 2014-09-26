package org.w3.banana.jasmine.test

import org.w3.banana._, syntax._, diesel._, binder._
import org.w3.banana.binder.JsDateBinders._
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
abstract class CommonBindersJasmineTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
    extends JasmineTest {

  import ops._
  import syntax._

  describe("common binders") {

    it("serializing and deserialiazing JS DateTime") {
      val dateTime = new js.Date()
      expect(dateTime.toPG.as[js.Date].get) toEqual dateTime
    }

    it("serializing and deserialiazing a Boolean") {
      val truePg = true.toPG
      expect(truePg.pointer == Literal("true", xsd.boolean)).toEqual(true)
      expect(truePg.graph == Graph.empty).toEqual(true)
      expect(true.toPG.as[Boolean] == Success(true)).toEqual(true)

      val falsePg = false.toPG
      expect(truePg.pointer == Literal("true", xsd.boolean)).toEqual(true)
      expect(truePg.graph == Graph.empty).toEqual(true)
      expect(false.toPG.as[Boolean] == Success(false)).toEqual(true)

    }

    it("serializing and deserializing an Integer") {
      val pg123 = 123.toPG
      expect(pg123.pointer == Literal("123", xsd.integer)).toEqual(true)
      expect(pg123.graph == Graph.empty).toEqual(true)
      expect(pg123.toPG.as[Int] == Success(123)).toEqual(true)
    }

    it("serializing and deserializing a List of simple nodes") {
      val bn1 = BNode()
      val bn2 = BNode()
      val bn3 = BNode()
      val constructedListGr = Graph(
        Triple(bn1, rdf.first, Literal("1", xsd.integer)),
        Triple(bn1, rdf.rest, bn2),
        Triple(bn2, rdf.first, Literal("2", xsd.integer)),
        Triple(bn2, rdf.rest, bn3),
        Triple(bn3, rdf.first, Literal("3", xsd.integer)),
        Triple(bn3, rdf.rest, rdf.nil)
      )
      val binder = PGBinder[Rdf, List[Int]]
      val list = List(1, 2, 3)
      val listPg = binder.toPG(list)
      expect(listPg.graph isIsomorphicWith (constructedListGr)).toEqual(true)
      expect(binder.fromPG(listPg) == Success(list)).toEqual(true)
    }

    it("serializing and deserializing a List of complex types") {
      val binder = implicitly[PGBinder[Rdf, List[List[Int]]]]
      val list = List(List(1, 2), List(3))
      expect(binder.fromPG(binder.toPG(list)) == Success(list)).toEqual(true)
    }

    it("serializing and deserializing a Tuple2") {
      val binder = PGBinder[Rdf, (Int, String)]
      val tuple = (42, "42")
      expect(binder.fromPG(binder.toPG(tuple)) == Success(tuple)).toEqual(true)
    }

    it("serializing and deserializing a Map") {
      val binder = PGBinder[Rdf, Map[String, List[Int]]]
      val map = Map("1" -> List(1, 2, 3), "2" -> List(4, 5))
      expect(binder.fromPG(binder.toPG(map)) == Success(map)).toEqual(true)
      expect(binder.fromPG(binder.toPG(Map.empty)) == Success(Map.empty)).toEqual(true)
    }

    it("serializing and deserializing an Either") {
      val binder = PGBinder[Rdf, Either[String, List[Int]]]
      val StringPGBinder = PGBinder[Rdf, String]
      val left = Left("foo")
      val right = Right(List(1, 2, 3))
      expect(binder.fromPG(binder.toPG(left)) == Success(left)).toEqual(true)
      expect(binder.fromPG(binder.toPG(right)) == Success(right)).toEqual(true)
      expect(binder.fromPG(StringPGBinder.toPG("foo")).isFailure).toEqual(true)
    }

    it("serializing and deserialiazing Option") {
      val opts: Option[String] = Some("foo")
      implicit val binder = PGBinder[Rdf, Option[String]]
      expect(opts.toPG.as[Option[String]] == Success(opts)).toEqual(true)
      expect((None: Option[String]).toPG.as[Option[String]] == Success(None)).toEqual(true)
    }

    it("the implicit chains must be complete") {
      implicitly[PGBinder[Rdf, Rdf#URI]]
      implicitly[NodeBinder[Rdf, Rdf#URI]]
      implicitly[PGBinder[Rdf, Rdf#Node]]
      implicitly[ToURI[Rdf, Rdf#URI]]
      implicitly[FromURI[Rdf, Rdf#URI]]
    }

  }

}
