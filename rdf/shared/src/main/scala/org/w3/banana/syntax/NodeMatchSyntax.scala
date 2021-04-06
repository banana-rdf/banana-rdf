package org.w3.banana.syntax

import org.w3.banana._

trait NodeMatchSyntax[Rdf <: RDF] { self: RDFSyntax[Rdf] =>

  implicit def nodeMatchW(nodeMatch: Rdf#NodeMatch): NodeMatchW[Rdf] =
    new NodeMatchW[Rdf](nodeMatch)

}

class NodeMatchW[Rdf <: RDF](val nodeMatch: Rdf#NodeMatch) extends AnyVal {

  def fold[T](funANY: => T, funNode: Rdf#Node => T)(implicit ops: RDFOps[Rdf]): T =
    ops.foldNodeMatch(nodeMatch)(funANY, funNode)

  def resolveAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#NodeMatch = {
    import ops._
    ops.foldNodeMatch(nodeMatch)(ops.ANY, node => node.resolveAgainst(baseUri))
  }

  def relativize(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#NodeMatch = {
    import ops._
    ops.foldNodeMatch(nodeMatch)(ops.ANY, node => node.relativize(baseUri))
  }

}
