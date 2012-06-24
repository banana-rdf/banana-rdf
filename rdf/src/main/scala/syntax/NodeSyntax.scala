package org.w3.banana.syntax

import org.w3.banana._

trait NodeSyntax[Rdf <: RDF] {
this: RDFOperationsSyntax[Rdf] =>

  implicit def nodeWrapper(node: Rdf#Node): NodeW = new NodeW(node)

  class NodeW(node: Rdf#Node) {

    def fold[T](funURI: Rdf#URI => T, funBNode: Rdf#BNode => T, funLiteral: Rdf#Literal => T): T =
      ops.foldNode(node)(funURI, funBNode, funLiteral)

  }

}
