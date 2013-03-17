package org.w3.banana.diesel

import diesel.ObjectList
import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.syntax._

class PointedGraphPredicate[Rdf <: RDF](pointed: PointedGraph[Rdf], p: Rdf#URI) {

  def ->-(pointedObject: PointedGraph[Rdf])(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    import ops._
    import pointed.{ pointer => s, graph => acc }
    import pointedObject.{ pointer => o, graph => graphObject }
    val graph = Graph(Triple(s, p, o)) union acc union graphObject
    PointedGraph(s, graph)
  }

  def ->-[T](o: T, os: T*)(implicit ops: RDFOps[Rdf], toPG: ToPG[Rdf, T]): PointedGraph[Rdf] = os match {
    case Seq() => this.->-(toPG.toPG(o))
    case _ => pointed -- p ->- ObjectList(o :: os.toList)
  }

  def ->-[T](opt: Option[T])(implicit ops: RDFOps[Rdf], toPG: ToPG[Rdf, T]): PointedGraph[Rdf] = opt match {
    case None => pointed
    case Some(t) => this.->-(t)
  }

  def ->-[T](objList: ObjectList[T])(implicit ops: RDFOps[Rdf], toPG: ToPG[Rdf, T]): PointedGraph[Rdf] =
    objList.ts.foldLeft(pointed)(
      (acc, t) => acc -- p ->- t
    )
}
