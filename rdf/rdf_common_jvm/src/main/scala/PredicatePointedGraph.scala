package org.w3.banana.diesel

import org.w3.banana._

case class PredicatePointedGraph[Rdf <: RDF](p: Rdf#URI, pointed: PointedGraph[Rdf]) {

  def --(s: Rdf#Node)(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    import pointed.{ graph => acc, pointer => o }
    val graph = acc union Graph(Triple(s, p, o))
    PointedGraph(s, graph)
  }

  def --(pointedSubject: PointedGraph[Rdf])(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    import pointed.{ graph => acc, pointer => o }
    import pointedSubject.{ graph => graphObject, pointer => s }
    val graph = Graph(Triple(s, p, o)) union acc union graphObject
    PointedGraph(s, graph)
  }

}
