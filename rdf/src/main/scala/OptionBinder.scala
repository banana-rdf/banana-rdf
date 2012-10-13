package org.w3.banana

import scala.util._

trait OptionBinder[Rdf <: RDF] {
  this: Diesel[Rdf] =>

  import ops._

  implicit def OptionBinder[T](implicit binder: PointedGraphBinder[Rdf, T]): PointedGraphBinder[Rdf, Option[T]] = new PointedGraphBinder[Rdf, Option[T]] {

    val listBinder = implicitly[PointedGraphBinder[Rdf, List[T]]]

    def fromPointedGraph(pointed: PointedGraph[Rdf]): Try[Option[T]] = {
      listBinder.fromPointedGraph(pointed) map { _.headOption }
    }

    def toPointedGraph(tOpt: Option[T]): PointedGraph[Rdf] = {
      listBinder.toPointedGraph(tOpt.toList)
    }

  }

}
