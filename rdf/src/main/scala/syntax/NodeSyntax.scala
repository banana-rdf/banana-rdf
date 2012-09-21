package org.w3.banana.syntax

import org.w3.banana._

trait NodeSyntax[Rdf <: RDF] {

  def ops: RDFOps[Rdf]

  implicit def nodeWrapper(node: Rdf#Node): NodeW = new NodeW(node)

  class NodeW(node: Rdf#Node) {

    def fold[T](funURI: Rdf#URI => T, funBNode: Rdf#BNode => T, funLiteral: Rdf#Literal => T): T =
      ops.foldNode(node)(funURI, funBNode, funLiteral)

    def resolveAgainst(baseUri: Rdf#URI)(implicit diesel: Diesel[Rdf]): Rdf#Node = {
      import diesel._
      node.fold(_.resolveAgainst(baseUri), bn => bn, lit => lit)
    }

    def relativize(baseUri: Rdf#URI)(implicit diesel: Diesel[Rdf]): Rdf#Node = {
      import diesel._
      node.fold(_.relativize(baseUri), bn => bn, lit => lit)
    }

  }

}
