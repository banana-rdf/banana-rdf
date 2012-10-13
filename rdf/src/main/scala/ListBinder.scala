package org.w3.banana

import scala.util._

trait ListBinder[Rdf <: RDF] {
  this: Diesel[Rdf] =>

  import ops._

  implicit def ListBinder[T](implicit binder: PointedGraphBinder[Rdf, T]): PointedGraphBinder[Rdf, List[T]] = new PointedGraphBinder[Rdf, List[T]] {

    def fromPointedGraph(pointed: PointedGraph[Rdf]): Try[List[T]] = {
      import pointed.{ pointer, graph }
      var elems = List[T]()
      var current = pointer
      Try {
        while (current != rdf.nil) {
          (getObjects(graph, current, rdf.first).toList, getObjects(graph, current, rdf.rest).toList) match {
            case (List(first), List(rest)) => {
              val firstPointed = PointedGraph(first, pointed.graph)
              elems ::= binder.fromPointedGraph(firstPointed).get
              current = rest
            }
            case other => throw new FailedConversion("asList: couldn't decode a list")
          }
        }
        elems.reverse
      }
    }

    def toPointedGraph(t: List[T]): PointedGraph[Rdf] = {
      var current: Rdf#Node = rdf.nil
      val triples = scala.collection.mutable.Set[Rdf#Triple]()
      t.reverse foreach { a =>
        val newBNode = bnode()
        val pointed = binder.toPointedGraph(a)
        triples += Triple(newBNode, rdf.first, pointed.pointer)
        triples ++= pointed.graph.toIterable
        triples += Triple(newBNode, rdf.rest, current)
        current = newBNode
      }
      PointedGraph(current, Graph(triples))
    }

  }

}
