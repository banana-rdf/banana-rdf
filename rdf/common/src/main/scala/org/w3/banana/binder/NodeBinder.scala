package org.w3.banana.binder

import org.w3.banana._

import scala.util._

trait NodeBinder[Rdf <: RDF, T] extends FromNode[Rdf, T] with ToNode[Rdf, T]

object NodeBinder {

  implicit def FromNodeToNode2NodeBinder[Rdf <: RDF, T](implicit from: FromNode[Rdf, T], to: ToNode[Rdf, T]): NodeBinder[Rdf, T] =
    new NodeBinder[Rdf, T] {
      def fromNode(node: Rdf#Node): Try[T] = from.fromNode(node)
      def toNode(t: T): Rdf#Node = to.toNode(t)
    }

}

