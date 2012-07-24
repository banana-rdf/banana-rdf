package org.w3.banana

import scalaz.{ Validation, Success, Failure }

object RecordBinder {

  private def po[Rdf <: RDF, T](t: T, property: Property[Rdf, T]): (Rdf#URI, PointedGraph[Rdf]) = {
    val Property(uri, binder) = property
    (uri, binder.toPointedGraph(t))
  }

  private def make[Rdf <: RDF](pos: (Rdf#URI, PointedGraph[Rdf])*)(implicit ops: RDFOperations[Rdf]): PointedGraph[Rdf] = {
    val subject = ops.makeUri("#" + java.util.UUID.randomUUID().toString)
    var triples: Set[Rdf#Triple] = Set.empty
    for (po <- pos.toIterable) {
      val (p, pg) = po
      triples += ops.makeTriple(subject, p, pg.pointer)
      triples ++= ops.graphToIterable(pg.graph)
    }
    PointedGraph(subject, ops.makeGraph(triples))
  }

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
trait RecordBinder[Rdf <: RDF] {
  self: Diesel[Rdf] =>

  import RecordBinder._
  import ops._

  def classUrisFor[T](uri: Rdf#URI, uris: Rdf#URI*): ClassUrisFor[Rdf, T] = new ClassUrisFor[Rdf, T] {
    val classes = uri :: uris.toList
  }

  /**
   * binds a type T to one unique URI
   *
   * consT is typically a singleton object and T is its singleton type
   */
  def constant[T](constT: T, constUri: Rdf#URI): PointedGraphBinder[Rdf, T] = {

    val uriBinder = new URIBinder[Rdf, T] {
      def fromUri(uri: Rdf#URI): Validation[BananaException, T] =
        if (constUri == uri)
          Success(constT)
        else
          Failure(WrongExpectation(constUri + " does not equal " + uri))

      def toUri(t: T): Rdf#URI = constUri
    }

    NodeToPointedGraphBinder(UriToNodeBinder(uriBinder))

  }

  /**
   * declares a Property/Object element where T is in the object position
   */
  def property[T](uri: Rdf#URI)(implicit objectBinder: PointedGraphBinder[Rdf, T]): Property[Rdf, T] = Property(uri, objectBinder)

  private def extract[T](pointed: PointedGraph[Rdf], property: Property[Rdf, T]): Validation[BananaException, T] = {
    val Property(predicate, oBinder) = property
    (pointed / predicate).as[T](oBinder)
  }

  def newUri(prefix: String): Rdf#URI = uri(prefix + java.util.UUID.randomUUID().toString)

  /**
   * combine PointedGraphBinder elements and apply/unapply functions to build binders
   *
   * TODO
   * - provide a trait to avoid reflection
   * - provide other apply methods with different arity
   * - use shapeless to generalize
   */

  def pgb[T] = new PGB[T]

  class PGB[T] {

    def apply[T1](p1: Property[Rdf, T1])(apply: (T1) => T, unapply: T => Option[T1]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some(t1) = unapply(t)
        make(po(t1, p1))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = extract(pointed, p1)
        for (t1 <- v1) yield apply(t1)
      }

    }

    def apply[T1, T2](p1: Property[Rdf, T1], p2: Property[Rdf, T2])(apply: (T1, T2) => T, unapply: T => Option[(T1, T2)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2)) = unapply(t)
        make(po(t1, p1), po(t2, p2))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = extract(pointed, p1)
        def v2 = extract(pointed, p2)
        for (t1 <- v1; t2 <- v2) yield apply(t1, t2)
      }

    }

    def apply[T1, T2, T3](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3])(apply: (T1, T2, T3) => T, unapply: T => Option[(T1, T2, T3)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3)) = unapply(t)
        make(po(t1, p1), po(t2, p2), po(t3, p3))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = extract(pointed, p1)
        def v2 = extract(pointed, p2)
        def v3 = extract(pointed, p3)
        for (t1 <- v1; t2 <- v2; t3 <- v3) yield apply(t1, t2, t3)
      }

    }

    def apply[T1, T2, T3, T4](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4])(apply: (T1, T2, T3, T4) => T, unapply: T => Option[(T1, T2, T3, T4)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4)) = unapply(t)
        make(po(t1, p1), po(t2, p2), po(t3, p3), po(t4, p4))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        val v1 = extract(pointed, p1)
        val v2 = extract(pointed, p2)
        val v3 = extract(pointed, p3)
        val v4 = extract(pointed, p4)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4) yield apply(t1, t2, t3, t4)
      }

    }

    def apply[T1, T2, T3, T4, T5](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5])(apply: (T1, T2, T3, T4, T5) => T, unapply: T => Option[(T1, T2, T3, T4, T5)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5)) = unapply(t)
        make(po(t1, p1), po(t2, p2), po(t3, p3), po(t4, p4), po(t5, p5))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        val v1 = extract(pointed, p1)
        val v2 = extract(pointed, p2)
        val v3 = extract(pointed, p3)
        val v4 = extract(pointed, p4)
        val v5 = extract(pointed, p5)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5) yield apply(t1, t2, t3, t4, t5)
      }

    }

  }

}
