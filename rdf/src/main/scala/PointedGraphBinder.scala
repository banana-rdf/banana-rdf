package org.w3.banana

import scala.util._

trait ToPointedGraph[Rdf <: RDF, T] {
  def toPointedGraph(t: T): PointedGraph[Rdf]
}

object ToPointedGraph {
  implicit def ToPointedGraphForPGB[Rdf <: RDF, T](implicit binder: PointedGraphBinder[Rdf, T]): ToPointedGraph[Rdf, T] = binder
}

trait FromPointedGraph[Rdf <: RDF, T] {
  def fromPointedGraph(pointed: PointedGraph[Rdf]): Try[T]
}

object FromPointedGraph {
  implicit def FromPointedGraphForPGB[Rdf <: RDF, T](implicit binder: PointedGraphBinder[Rdf, T]): FromPointedGraph[Rdf, T] = binder
}

trait PointedGraphBinder[Rdf <: RDF, T] extends FromPointedGraph[Rdf, T] with ToPointedGraph[Rdf, T]

object PointedGraphBinder {

  implicit def PGBForPointedGraphBinder[Rdf <: RDF]: PointedGraphBinder[Rdf, PointedGraph[Rdf]] =
    new PointedGraphBinder[Rdf, PointedGraph[Rdf]] {
      def fromPointedGraph(pointed: PointedGraph[Rdf]): Try[PointedGraph[Rdf]] = Success(pointed)
      def toPointedGraph(t: PointedGraph[Rdf]): PointedGraph[Rdf] = t
    }

  def apply[Rdf <: RDF, T](implicit binder: PointedGraphBinder[Rdf, T]): PointedGraphBinder[Rdf, T] = binder

  implicit def toPointedGraphBinderCombinator[Rdf <: RDF, T](binder: PointedGraphBinder[Rdf, T])(implicit diesel: Diesel[Rdf]): PointedGraphBinderCombinator[Rdf, T] = new PointedGraphBinderCombinator[Rdf, T](binder)(diesel)

  implicit def PGBForNode[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], binder: NodeBinder[Rdf, T]): PointedGraphBinder[Rdf, T] =
    NodeBinder.toPointedGraphBinder[Rdf, T](ops, binder)

}

trait ClassUrisFor[Rdf <: RDF, T] {

  def classes: Iterable[Rdf#URI]

}

class PointedGraphBinderCombinator[Rdf <: RDF, T](binder: PointedGraphBinder[Rdf, T])(implicit val diesel: Diesel[Rdf]) {

  import diesel._

  def withClasses(classUris: ClassUrisFor[Rdf, T]): PointedGraphBinder[Rdf, T] =
    new PointedGraphBinder[Rdf, T] {
      // Q: do we want to check the classes here?
      def fromPointedGraph(pointed: PointedGraph[Rdf]): Try[T] =
        binder.fromPointedGraph(pointed)

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        var pointed = binder.toPointedGraph(t)
        classUris.classes foreach { clazz =>
          pointed = pointed.a(clazz)
        }
        pointed
      }
    }

}
