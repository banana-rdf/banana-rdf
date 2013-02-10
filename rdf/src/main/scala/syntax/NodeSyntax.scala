package org.w3.banana.syntax

import org.w3.banana._
import org.w3.banana.binder._
import scala.util.Try

trait NodeSyntax {

  implicit def nodeW[Rdf <: RDF](node: Rdf#Node) = new NodeW[Rdf](node)

}

object NodeSyntax extends NodeSyntax

class NodeW[Rdf <: RDF](val node: Rdf#Node) extends AnyVal {

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

  def relativizeAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Node = {
    ops.foldNode(node)(_.relativizeAgainst(baseUri), bn => bn, lit => lit)
  }

  def as[T](implicit from: FromNode[Rdf, T]): Try[T] =
    from.fromNode(node)

}
