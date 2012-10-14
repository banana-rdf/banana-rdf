package org.w3.banana

import scala.util._

trait ToPointedGraph[Rdf <: RDF, T] {
  def toPointedGraph(t: T): PointedGraph[Rdf]
}

trait FromPointedGraph[Rdf <: RDF, T] {
  def fromPointedGraph(pointed: PointedGraph[Rdf]): Try[T]
}

trait PointedGraphBinder[Rdf <: RDF, T] extends FromPointedGraph[Rdf, T] with ToPointedGraph[Rdf, T]

object PointedGraphBinder {

//  implicit def PGBNode[Rdf <: RDF](implicit ops: ): PointedGraphBinder[Rdf, Rdf#Node] = NodeToPointedGraphBinder(NodeBinder.naturalBinder[Rdf])

  implicit def PGBUri[Rdf <: RDF](implicit ops: RDFOps[Rdf]): PointedGraphBinder[Rdf, Rdf#URI] = URIBinder.naturalBinder[Rdf].toNodeBinder.toPGB

//  implicit val PGBLiteral: PointedGraphBinder[Rdf, Rdf#Literal] = NodeToPointedGraphBinder(LiteralToNodeBinder(LiteralBinder.naturalBinder[Rdf]))

  def apply[Rdf <: RDF, T](implicit binder: PointedGraphBinder[Rdf, T]): PointedGraphBinder[Rdf, T] = binder

  implicit def toPointedGraphBinderCombinator[Rdf <: RDF, T](binder: PointedGraphBinder[Rdf, T])(implicit diesel: Diesel[Rdf]): PointedGraphBinderCombinator[Rdf, T] = new PointedGraphBinderCombinator[Rdf, T](binder)(diesel)

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
