package org.w3.banana.binder

import org.w3.banana._

trait ToNode[Rdf <: RDF, -T] {
  def toNode(t: T): Rdf#Node
}

object ToNode {

  implicit def PointedGraphToNode[Rdf <: RDF]: ToNode[Rdf,PointedGraph[Rdf]] = new ToNode[Rdf, PointedGraph[Rdf]] {
    def toNode(t: PointedGraph[Rdf]): Rdf#Node = t.pointer
  }

  implicit def NodeToNode[Rdf <: RDF]: ToNode[Rdf,Rdf#Node] = new ToNode[Rdf, Rdf#Node] {
    def toNode(t: Rdf#Node): Rdf#Node = t
  }

  implicit def ToLiteralToNode[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], to: ToLiteral[Rdf, T]): ToNode[Rdf,T] = new ToNode[Rdf, T] {
    def toNode(t: T): Rdf#Node = to.toLiteral(t)
  }

  implicit def ToURIToNode[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], to: ToURI[Rdf, T]): ToNode[Rdf,T] = new ToNode[Rdf, T] {
    def toNode(t: T): Rdf#Node = to.toURI(t)
  }

}
