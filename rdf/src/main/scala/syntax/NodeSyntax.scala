package org.w3.banana.syntax

import org.w3.banana._

class NodeSyntax[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOps[Rdf]) {

  def fold[T](funURI: Rdf#URI => T, funBNode: Rdf#BNode => T, funLiteral: Rdf#Literal => T): T =
    ops.foldNode(node)(funURI, funBNode, funLiteral)

  def resolveAgainst(baseUri: Rdf#URI)(implicit diesel: Diesel[Rdf]): Rdf#Node = {
    import diesel.{ ops => _, _ }
    ops.foldNode(node)(_.resolveAgainst(baseUri), bn => bn, lit => lit)
  }

  def relativize(baseUri: Rdf#URI)(implicit diesel: Diesel[Rdf]): Rdf#Node = {
    import diesel.{ ops => _, _ }
    ops.foldNode(node)(_.relativize(baseUri), bn => bn, lit => lit)
  }

}
