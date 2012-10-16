package org.w3.banana

case class PointedGraph[Rdf <: RDF](pointer: Rdf#Node, graph: Rdf#Graph) {

  def resolveAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    PointedGraph[Rdf](
      pointer.resolveAgainst(baseUri),
      graph.resolveAgainst(baseUri))
  }

  def relativize(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    PointedGraph[Rdf](
      pointer.relativize(baseUri),
      graph.relativize(baseUri))
  }

  // carefull: this is potentially unsafe!!!
  def toLDR()(implicit ops: RDFOps[Rdf]): LinkedDataResource[Rdf] = {
    import ops._
    ops.foldNode(pointer)(
      uri => LinkedDataResource(uri.fragmentLess, this),
      bn => sys.error("expected a uri, got BNode: " + bn),
      lit => sys.error("expected a uri, got Literal: " + lit)
    )
  }

}

object PointedGraph {

  def apply[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] =
    new PointedGraph[Rdf](node, ops.emptyGraph)

  implicit def toPointedGraphW[Rdf <: RDF](pointed: PointedGraph[Rdf]): PointedGraphW[Rdf] = new PointedGraphW[Rdf](pointed)

}
