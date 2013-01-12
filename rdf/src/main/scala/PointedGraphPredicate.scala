package org.w3.banana

import scala.util._

class PointedGraphPredicate[Rdf <: RDF](pointed: PointedGraph[Rdf], p: Rdf#URI) {

  def ->-(o: Rdf#Node, os: Rdf#Node*)(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    import pointed.{ pointer => s, graph => acc }
    val graph: Rdf#Graph =
      if (os.isEmpty) {
        val g = Graph(Triple(s, p, o))
        acc union g
      } else {
        val triples: Iterable[Rdf#Triple] = (o :: os.toList) map { o => Triple(s, p, o) }
        Graph(triples) union acc
      }
    PointedGraph(s, graph)
  }

  def ->-(pointedObject: PointedGraph[Rdf])(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    import pointed.{ pointer => s, graph => acc }
    import pointedObject.{ pointer => o, graph => graphObject }
    val graph = Graph(Triple(s, p, o)) union acc union graphObject
    PointedGraph(s, graph)
  }

  def ->-[T](o: T)(implicit ops: RDFOps[Rdf], binder: PointedGraphBinder[Rdf, T]): PointedGraph[Rdf] = {
    import pointed.{ pointer => s, graph => acc }
    val pg = binder.toPointedGraph(o)
    this.->-(pg)
  }

  def ->-[T](opt: Option[T])(implicit ops: RDFOps[Rdf], binder: PointedGraphBinder[Rdf, T]): PointedGraph[Rdf] = opt match {
    case None => pointed
    case Some(t) => this.->-(t)(ops, binder)
  }

}
