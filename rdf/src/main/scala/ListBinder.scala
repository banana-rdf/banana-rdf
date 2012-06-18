package org.w3.banana

import scalaz._
import scalaz.Validation._

trait ListBinder[Rdf <: RDF] {
this: Diesel[Rdf] =>

  import ops._
  import graphTraversal._

  implicit def ListBinder[T](implicit binder: PointedGraphBinder[Rdf, T]): PointedGraphBinder[Rdf, List[T]] = new PointedGraphBinder[Rdf, List[T]] {

    def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, List[T]] = {
      import pointed.{ node , graph }
      var elems = List[T]()
      var current = node
      try {
        while(current != rdf.nil) {
          (getObjects(graph, current, rdf.first).toList, getObjects(graph, current, rdf.rest).toList) match {
            case (List(first), List(rest)) => {
              val firstPointed = PointedGraph(first, pointed.graph)
              elems ::= binder.fromPointedGraph(firstPointed).fold(be => throw be, e => e)
              current = rest
            }
            case _ => throw new FailedConversion("asList: couldn't decode a list")
          }
        }
        Success(elems.reverse)
      } catch {
        case be: BananaException => Failure(be)
      }
    }

    def toPointedGraph(t: List[T]): PointedGraph[Rdf] = {
      var current: Rdf#Node = rdf.nil
      val triples = scala.collection.mutable.Set[Rdf#Triple]()
      t.reverse foreach { a =>
        val newBNode = BNode()
        val pointed = binder.toPointedGraph(a)
        triples += Triple(newBNode, rdf.first, pointed.node)
        triples ++= pointed.graph
        triples += Triple(newBNode, rdf.rest, current)
        current = newBNode
      }
      PointedGraph(current, Graph(triples))
    }

  }

}
