package org.w3.banana

import scalaz.Validation

trait ToPointedGraph[Rdf <: RDF, -T] {
  def toPointedGraph(t: T): PointedGraph[Rdf]
}

trait FromPointedGraph[Rdf <: RDF, +T] {
  def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T]
}

trait PointedGraphBinder[Rdf <: RDF, T] extends FromPointedGraph[Rdf, T] with ToPointedGraph[Rdf, T]

object PointedGraphBinder {

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
      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] =
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
