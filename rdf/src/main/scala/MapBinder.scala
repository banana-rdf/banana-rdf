package org.w3.banana

import scala.util._

trait MapBinder[Rdf <: RDF] {
  this: Diesel[Rdf] =>

  import ops._

  implicit def MapBinder[K, V](implicit kbinder: PointedGraphBinder[Rdf, K], vbinder: PointedGraphBinder[Rdf, V]): PointedGraphBinder[Rdf, Map[K, V]] = new PointedGraphBinder[Rdf, Map[K, V]] {

    val binder = implicitly[PointedGraphBinder[Rdf, List[(K, V)]]]

    def fromPointedGraph(pointed: PointedGraph[Rdf]): Try[Map[K, V]] = binder.fromPointedGraph(pointed) map { l => Map() ++ l }

    def toPointedGraph(t: Map[K, V]): PointedGraph[Rdf] =
      binder.toPointedGraph(t.toList)

  }

}
