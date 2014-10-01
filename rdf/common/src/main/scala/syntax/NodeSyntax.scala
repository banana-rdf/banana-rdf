package org.w3.banana.syntax

import org.w3.banana._

trait NodeSyntax[Rdf <: RDF] { self: RDFSyntax[Rdf] =>

  implicit def nodeW(node: Rdf#Node) = new NodeW[Rdf](node)

}

class NodeW[Rdf <: RDF](val node: Rdf#Node) extends AnyVal {

  def fold[T](funURI: Rdf#URI => T, funBNode: Rdf#BNode => T, funLiteral: Rdf#Literal => T)(implicit ops: RDFOps[Rdf]): T =
    ops.foldNode(node)(funURI, funBNode, funLiteral)

  def resolveAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Node = {
    import ops._
    foldNode(node)(ops.resolve(baseUri, _), bn => bn, lit => lit)
  }

  def isBNode(implicit ops: RDFOps[Rdf]): Boolean = ops.foldNode(node)(url => false, bn => true, lit => false)
  def isLiteral(implicit ops: RDFOps[Rdf]): Boolean = ops.foldNode(node)(url => false, bn => false, lit => true)
  def isURI(implicit ops: RDFOps[Rdf]): Boolean = ops.foldNode(node)(url => true, bn => false, lit => false)

  def relativize(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Node = {
    import ops._
    foldNode(node)(_.relativize(baseUri), bn => bn, lit => lit)
  }

  def relativizeAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Node = {
    import ops._
    ops.foldNode(node)(_.relativizeAgainst(baseUri), bn => bn, lit => lit)
  }

}
