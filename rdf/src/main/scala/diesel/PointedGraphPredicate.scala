package org.w3.banana.diesel

import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.syntax._
import scala.util._

class PointedGraphPredicate[Rdf <: RDF](pointed: PointedGraph[Rdf], p: Rdf#URI) {

  def ->-(os: Rdf#Node*)(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    import pointed.{ pointer => s, graph => acc }

    val triples = os map { o => Triple(s, p, o) }
    val graph = Graph(triples) union acc
    PointedGraph(s, graph)
  }

  /**
   * We need to use DummyImplicit here, to make overloaded methods distinguishable after type erasure.
   **/
  def ->-(pointedObjects: PointedGraph[Rdf]*)(implicit ops: RDFOps[Rdf], di: DummyImplicit): PointedGraph[Rdf] = {
    import ops._
    import pointed.{ pointer => s, graph => acc }

    val triples = pointedObjects map (pg => Triple(s, p, pg.pointer))
    val graph = pointedObjects.foldLeft(Graph(triples) union acc)(_ union _.graph)
    PointedGraph(s, graph)
  }

  def ->-[T](o: T)(implicit ops: RDFOps[Rdf], toPG: ToPG[Rdf, T]): PointedGraph[Rdf] = {
    import pointed.{ pointer => s, graph => acc }
    val pg = toPG.toPG(o)
    this.->-(pg)
  }

  def ->-[T](opt: Option[T])(implicit ops: RDFOps[Rdf], toPG: ToPG[Rdf, T]): PointedGraph[Rdf] = opt match {
    case None => pointed
    case Some(t) => this.->-(t)(ops, toPG)
  }

}
