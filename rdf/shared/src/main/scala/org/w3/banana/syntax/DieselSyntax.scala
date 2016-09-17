package org.w3.banana.syntax

import org.w3.banana._, diesel._

object DieselSyntax {

  implicit def toPointedGraphW[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOps[Rdf]): PointedGraphW[Rdf] =
    new PointedGraphW[Rdf](PointedGraph(node)(ops))

  implicit def toPointedGraphW[Rdf <: RDF](pointed: PointedGraph[Rdf]): PointedGraphW[Rdf]
  = new PointedGraphW[Rdf](pointed)

}

trait DieselSyntax[Rdf <: RDF] {

  implicit def toPointedGraphW(node: Rdf#Node)(implicit ops: RDFOps[Rdf]): PointedGraphW[Rdf] =
    DieselSyntax.toPointedGraphW(node)

  implicit def toPointedGraphW[Rdf <: RDF](pointed: PointedGraph[Rdf]): PointedGraphW[Rdf] =
    DieselSyntax.toPointedGraphW(pointed)

}
