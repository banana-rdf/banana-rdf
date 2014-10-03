package org.w3.banana.diesel

import org.w3.banana._

trait Diesel {

  implicit def toPointedGraphW[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOps[Rdf]): PointedGraphW[Rdf] = new PointedGraphW[Rdf](PointedGraph(node)(ops))

  implicit def toPointedGraphW[Rdf <: RDF](pointed: PointedGraph[Rdf]): PointedGraphW[Rdf] = new PointedGraphW[Rdf](pointed)

}
