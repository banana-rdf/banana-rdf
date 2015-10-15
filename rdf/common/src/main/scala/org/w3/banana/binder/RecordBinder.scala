package org.w3.banana.binder

import org.w3.banana._
import org.w3.banana.diesel._

import scala.util._

object RecordBinder {

  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]): RecordBinder[Rdf] =
    new RecordBinder[Rdf]()

}

/**
 * helper functions for binding Scala records (typically case classes)
 *
 * here is the recipe, given one type T:
 * - start by declaring the various elements that make T
 *   this is done through [PGBElem]s, and RecordBinder provide some helpers (see constant, property and uriTemplate)
 * - say how to combine the elements with a contructor (apply-like function) and an extractor (unapply-like function)
 *   there is done with the pgb helper function
 */
class RecordBinder[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) {

  import ops._

  def classUrisFor[T](uri: Rdf#URI, uris: Rdf#URI*): ClassUrisFor[Rdf, T] = new ClassUrisFor[Rdf, T] {
    val classes = uri :: uris.toList
  }

  /**
   * binds a type T to one unique URI
   *
   * consT is typically a singleton object and T is its singleton type
   */
  def constant[T](constT: T, constUri: Rdf#URI): PGBinder[Rdf, T] = {
    implicit val uriBinder: URIBinder[Rdf, T] = new URIBinder[Rdf, T] {
      def fromURI(uri: Rdf#URI): Try[T] =
        if (constUri == uri)
          Success(constT)
        else
          Failure(WrongExpectation(constUri + " does not equal " + uri))

      def toURI(t: T): Rdf#URI = constUri
    }
    val binder = PGBinder[Rdf, T]
    binder
  }

  /**
   * declares a Property/Object element where T is in the object position
   */
  def property[T](predicate: Rdf#URI)(implicit objectBinder: PGBinder[Rdf, T]): Property[Rdf, T] = new Property[Rdf, T] {
    val uri = predicate
    def pos(t: T): Iterable[(Rdf#URI, PointedGraph[Rdf])] = Set((predicate, t.toPG))
    def extract(pointed: PointedGraph[Rdf]): Try[T] =
      (pointed / predicate).as[T]
  }

  def optional[T](predicate: Rdf#URI)(implicit objectBinder: PGBinder[Rdf, T]): Property[Rdf, Option[T]] = new Property[Rdf, Option[T]] {
    val uri = predicate
    def pos(tOpt: Option[T]): Iterable[(Rdf#URI, PointedGraph[Rdf])] = tOpt match {
      case None => Set()
      case Some(t) => Set((predicate, t.toPG))
    }
    def extract(pointed: PointedGraph[Rdf]): Try[Option[T]] =
      (pointed / predicate).asOption[T]
  }

  def set[T](predicate: Rdf#URI)(implicit objectBinder: PGBinder[Rdf, T]): Property[Rdf, Set[T]] = new Property[Rdf, Set[T]] {
    val uri = predicate
    def pos(ts: Set[T]): Iterable[(Rdf#URI, PointedGraph[Rdf])] =
      ts map { t => (predicate, t.toPG) }
    def extract(pointed: PointedGraph[Rdf]): Try[Set[T]] =
      (pointed / predicate).asSet[T]
  }

  def newUri(prefix: String): Rdf#URI = {
    def s4(): String = Math.floor((1 + Math.random()) * 0x10000).toString().substring(1)
    URI(s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4())
  }

  /**
   * combine PGBinder elements and apply/unapply functions to build binders
   *
   * TODO
   * - provide other apply methods with different arity
   * - use shapeless to generalize
   */

  /**
   * Create PGB with pointer based on record fields.
   */
  def pgbWithId[T](id: T => Rdf#Node) = new PGB[T] {
    def makeSubject(t: T): Rdf#Node = id(t)
  }

  /**
   * Create PGB with constant pointer.
   * Typically: #, #thing, #me, or even empty string.
   */
  def pgbWithConstId[T](constantPointer: String) =
    pgbWithId[T](_ => ops.makeUri(constantPointer))

  /**
   * Create PGB with random UUID pointer.
   */
  def pgb[T] = pgbWithId[T](_ => BNode())


  abstract class PGB[T] {

    def makeSubject(t: T): Rdf#Node

    def make(t: T, pos: Iterable[(Rdf#URI, PointedGraph[Rdf])]*)(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
      val subject = makeSubject(t)
      var triples: Set[Rdf#Triple] = Set.empty
      for (po <- pos.toIterable.flatten) {
        val (p, pg) = po
        triples += ops.makeTriple(subject, p, pg.pointer)
        triples ++= ops.getTriples(pg.graph)
      }
      PointedGraph(subject, ops.makeGraph(triples))
    }

    def apply[T1](p1: Property[Rdf, T1])(apply: (T1) => T, unapply: T => Option[T1]): PGBinder[Rdf, T] = new PGBinder[Rdf, T] {

      def toPG(t: T): PointedGraph[Rdf] = {
        val Some(t1) = unapply(t)
        make(t, p1.pos(t1))
      }

      def fromPG(pointed: PointedGraph[Rdf]): Try[T] = {
        def v1 = p1.extract(pointed)
        for (t1 <- v1) yield apply(t1)
      }

    }

    def apply[T1, T2](p1: Property[Rdf, T1], p2: Property[Rdf, T2])(apply: (T1, T2) => T, unapply: T => Option[(T1, T2)]): PGBinder[Rdf, T] = new PGBinder[Rdf, T] {

      def toPG(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2)) = unapply(t)
        make(t, p1.pos(t1), p2.pos(t2))
      }

      def fromPG(pointed: PointedGraph[Rdf]): Try[T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        for (t1 <- v1; t2 <- v2) yield apply(t1, t2)
      }

    }

    def apply[T1, T2, T3](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3])(apply: (T1, T2, T3) => T, unapply: T => Option[(T1, T2, T3)]): PGBinder[Rdf, T] = new PGBinder[Rdf, T] {

      def toPG(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3)) = unapply(t)
        make(t, p1.pos(t1), p2.pos(t2), p3.pos(t3))
      }

      def fromPG(pointed: PointedGraph[Rdf]): Try[T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3) yield apply(t1, t2, t3)
      }

    }

    def apply[T1, T2, T3, T4](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4])(apply: (T1, T2, T3, T4) => T, unapply: T => Option[(T1, T2, T3, T4)]): PGBinder[Rdf, T] = new PGBinder[Rdf, T] {

      def toPG(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4)) = unapply(t)
        make(t, p1.pos(t1), p2.pos(t2), p3.pos(t3), p4.pos(t4))
      }

      def fromPG(pointed: PointedGraph[Rdf]): Try[T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4) yield apply(t1, t2, t3, t4)
      }

    }

    def apply[T1, T2, T3, T4, T5](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5])(apply: (T1, T2, T3, T4, T5) => T, unapply: T => Option[(T1, T2, T3, T4, T5)]): PGBinder[Rdf, T] = new PGBinder[Rdf, T] {

      def toPG(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5)) = unapply(t)
        make(t, p1.pos(t1), p2.pos(t2), p3.pos(t3), p4.pos(t4), p5.pos(t5))
      }

      def fromPG(pointed: PointedGraph[Rdf]): Try[T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        def v5 = p5.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5) yield apply(t1, t2, t3, t4, t5)
      }

    }

    def apply[T1, T2, T3, T4, T5, T6](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5], p6: Property[Rdf, T6])(apply: (T1, T2, T3, T4, T5, T6) => T, unapply: T => Option[(T1, T2, T3, T4, T5, T6)]): PGBinder[Rdf, T] = new PGBinder[Rdf, T] {

      def toPG(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5, t6)) = unapply(t)
        make(t, p1.pos(t1), p2.pos(t2), p3.pos(t3), p4.pos(t4), p5.pos(t5), p6.pos(t6))
      }

      def fromPG(pointed: PointedGraph[Rdf]): Try[T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        def v5 = p5.extract(pointed)
        def v6 = p6.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5; t6 <- v6) yield apply(t1, t2, t3, t4, t5, t6)
      }

    }

    def apply[T1, T2, T3, T4, T5, T6, T7](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5], p6: Property[Rdf, T6], p7: Property[Rdf, T7])(apply: (T1, T2, T3, T4, T5, T6, T7) => T, unapply: T => Option[(T1, T2, T3, T4, T5, T6, T7)]): PGBinder[Rdf, T] = new PGBinder[Rdf, T] {

      def toPG(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5, t6, t7)) = unapply(t)
        make(t, p1.pos(t1), p2.pos(t2), p3.pos(t3), p4.pos(t4), p5.pos(t5), p6.pos(t6), p7.pos(t7))
      }

      def fromPG(pointed: PointedGraph[Rdf]): Try[T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        def v5 = p5.extract(pointed)
        def v6 = p6.extract(pointed)
        def v7 = p7.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5; t6 <- v6; t7 <- v7) yield apply(t1, t2, t3, t4, t5, t6, t7)
      }

    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5], p6: Property[Rdf, T6], p7: Property[Rdf, T7], p8: Property[Rdf, T8])(apply: (T1, T2, T3, T4, T5, T6, T7, T8) => T, unapply: T => Option[(T1, T2, T3, T4, T5, T6, T7, T8)]): PGBinder[Rdf, T] = new PGBinder[Rdf, T] {

      def toPG(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5, t6, t7, t8)) = unapply(t)
        make(t, p1.pos(t1), p2.pos(t2), p3.pos(t3), p4.pos(t4), p5.pos(t5), p6.pos(t6), p7.pos(t7), p8.pos(t8))
      }

      def fromPG(pointed: PointedGraph[Rdf]): Try[T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        def v5 = p5.extract(pointed)
        def v6 = p6.extract(pointed)
        def v7 = p7.extract(pointed)
        def v8 = p8.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5; t6 <- v6; t7 <- v7; t8 <- v8) yield apply(t1, t2, t3, t4, t5, t6, t7, t8)
      }

    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5], p6: Property[Rdf, T6], p7: Property[Rdf, T7], p8: Property[Rdf, T8], p9: Property[Rdf, T9])(apply: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => T, unapply: T => Option[(T1, T2, T3, T4, T5, T6, T7, T8, T9)]): PGBinder[Rdf, T] = new PGBinder[Rdf, T] {

      def toPG(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5, t6, t7, t8, t9)) = unapply(t)
        make(t, p1.pos(t1), p2.pos(t2), p3.pos(t3), p4.pos(t4), p5.pos(t5), p6.pos(t6), p7.pos(t7), p8.pos(t8), p9.pos(t9))
      }

      def fromPG(pointed: PointedGraph[Rdf]): Try[T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        def v5 = p5.extract(pointed)
        def v6 = p6.extract(pointed)
        def v7 = p7.extract(pointed)
        def v8 = p8.extract(pointed)
        def v9 = p9.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5; t6 <- v6; t7 <- v7; t8 <- v8; t9 <- v9) yield apply(t1, t2, t3, t4, t5, t6, t7, t8, t9)
      }

    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5], p6: Property[Rdf, T6], p7: Property[Rdf, T7], p8: Property[Rdf, T8], p9: Property[Rdf, T9], p10: Property[Rdf, T10])(apply: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => T, unapply: T => Option[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)]): PGBinder[Rdf, T] = new PGBinder[Rdf, T] {

      def toPG(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)) = unapply(t)
        make(t, p1.pos(t1), p2.pos(t2), p3.pos(t3), p4.pos(t4), p5.pos(t5), p6.pos(t6), p7.pos(t7), p8.pos(t8), p9.pos(t9), p10.pos(t10))
      }

      def fromPG(pointed: PointedGraph[Rdf]): Try[T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        def v5 = p5.extract(pointed)
        def v6 = p6.extract(pointed)
        def v7 = p7.extract(pointed)
        def v8 = p8.extract(pointed)
        def v9 = p9.extract(pointed)
        def v10 = p10.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5; t6 <- v6; t7 <- v7; t8 <- v8; t9 <- v9; t10 <- v10) yield apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)
      }

    }

  }

}
