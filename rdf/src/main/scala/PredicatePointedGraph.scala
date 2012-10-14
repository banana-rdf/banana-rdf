package org.w3.banana

import scala.util._

case class PredicatePointedGraph[Rdf <: RDF](p: Rdf#URI, pointed: PointedGraph[Rdf]) {

  def --(s: Rdf#Node)(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    val PointedGraph(o, acc) = pointed
    val graph = acc union Graph(Triple(s, p, o))
    PointedGraph(s, graph)
  }

  def --(pointedSubject: PointedGraph[Rdf])(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    val PointedGraph(o, acc) = pointed
    val PointedGraph(s, graphObject) = pointedSubject
    val graph = Graph(Triple(s, p, o)) union acc union graphObject
    PointedGraph(s, graph)
  }

}
