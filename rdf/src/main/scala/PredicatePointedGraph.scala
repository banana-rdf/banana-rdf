package org.w3.banana

import scala.util._

case class PredicatePointedGraph[Rdf <: RDF](p: Rdf#URI, pointed: PointedGraph[Rdf]) {

  def --(s: Rdf#Node)(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    import pointed.{ pointer => o, graph => acc }
    val graph = acc union Graph(Triple(s, p, o))
    PointedGraph(s, graph)
  }

  def --(pointedSubject: PointedGraph[Rdf])(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    import pointed.{ pointer => o, graph => acc }
    import pointedSubject.{ pointer => s, graph => graphObject }
    val graph = Graph(Triple(s, p, o)) union acc union graphObject
    PointedGraph(s, graph)
  }

}
