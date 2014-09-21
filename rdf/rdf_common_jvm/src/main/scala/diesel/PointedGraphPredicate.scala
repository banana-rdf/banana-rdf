package org.w3.banana.diesel

import org.w3.banana._
import org.w3.banana.binder._

class PointedGraphPredicate[Rdf <: RDF](pointed: PointedGraph[Rdf], p: Rdf#URI) {

  def ->-(pointedObject: PointedGraph[Rdf])(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    import pointed.{ graph => acc, pointer => s }
    import pointedObject.{ graph => graphObject, pointer => o }
    val graph = Graph(Triple(s, p, o)) union acc union graphObject
    PointedGraph(s, graph)
  }

  def ->-[T](o: T, os: T*)(implicit ops: RDFOps[Rdf], toPG: ToPG[Rdf, T]): PointedGraph[Rdf] = {
    if (os.isEmpty)
      this.->-(toPG.toPG(o))
    else
      pointed -- p ->- ObjectList(o +: os)
  }

  def ->-[T](opt: Option[T])(implicit ops: RDFOps[Rdf], toPG: ToPG[Rdf, T]): PointedGraph[Rdf] = opt match {
    case None => pointed
    case Some(t) => this.->-(t)
  }

  def ->-[T](objList: ObjectList[T])(implicit ops: RDFOps[Rdf], toPG: ToPG[Rdf, T]): PointedGraph[Rdf] =
    objList.ts.foldLeft(pointed)(
      (acc, t) => acc -- p ->- t
    )

  def ->-[T](objects: Set[T])(implicit ops: RDFOps[Rdf], toPG: ToPG[Rdf, T]): PointedGraph[Rdf] = {
    import ops._
    val graph = objects.foldLeft(pointed.graph) {
      case (acc, obj) =>
        val pg = toPG.toPG(obj)
        acc union Graph(Set(Triple(pointed.pointer, p, pg.pointer))) union pg.graph
    }
    PointedGraph(pointed.pointer, graph)
  }

}
