package org.w3.banana.syntax

import org.w3.banana._

class NodeMatchSyntax[Rdf <: RDF](val nodeMatch: Rdf#NodeMatch) extends AnyVal {

  def fold[T](funANY: => T, funNode: Rdf#Node => T)(implicit ops: RDFOps[Rdf]): T =
    ops.foldNodeMatch(nodeMatch)(funANY, funNode)

  def resolveAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#NodeMatch = {
    import ops.{ toConcreteNodeMatch, nodeSyntax }
    ops.foldNodeMatch(nodeMatch)(ops.ANY, _.resolveAgainst(baseUri))
  }

  def relativize(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#NodeMatch = {
    import ops.{ toConcreteNodeMatch, nodeSyntax }
    ops.foldNodeMatch(nodeMatch)(ops.ANY, _.relativize(baseUri))
  }

}
