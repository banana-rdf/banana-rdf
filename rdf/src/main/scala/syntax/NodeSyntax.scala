package org.w3.banana.syntax

import org.w3.banana._

class NodeSyntax[Rdf <: RDF](val node: Rdf#Node) extends AnyVal {

  def fold[T](funURI: Rdf#URI => T, funBNode: Rdf#BNode => T, funLiteral: Rdf#Literal => T)(implicit ops: RDFOps[Rdf]): T =
    ops.foldNode(node)(funURI, funBNode, funLiteral)

  def resolveAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Node = {
    import ops._
    foldNode(node)(_.resolveAgainst(baseUri), bn => bn, lit => lit)
  }

  def relativize(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Node = {
    import ops._
    foldNode(node)(_.relativize(baseUri), bn => bn, lit => lit)
  }

}
