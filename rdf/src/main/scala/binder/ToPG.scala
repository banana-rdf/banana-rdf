package org.w3.banana.binder

import org.w3.banana._
import org.w3.banana.diesel._

import scala.util._

trait ToPG[Rdf <: RDF, -T] {
  def toPG(t: T): PointedGraph[Rdf]
}

object ToPG {

  implicit def PointedGraphToPG[Rdf <: RDF] = new ToPG[Rdf, PointedGraph[Rdf]] {
    def toPG(t: PointedGraph[Rdf]): PointedGraph[Rdf] = t
  }

  implicit def ToNodeToPG[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], to: ToNode[Rdf, T]) = new ToPG[Rdf, T] {
    def toPG(t: T): PointedGraph[Rdf] = PointedGraph(to.toNode(t))
  }

  implicit def ListToPG[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], to: ToPG[Rdf, T]): ToPG[Rdf, List[T]] = new ToPG[Rdf, List[T]] {
    import ops._
    def toPG(t: List[T]): PointedGraph[Rdf] = {
      var current: Rdf#Node = rdf.nil
      val triples = scala.collection.mutable.Set[Rdf#Triple]()
      t.reverse foreach { a =>
        val newBNode = bnode()
        val pointed = to.toPG(a)
        triples += Triple(newBNode, rdf.first, pointed.pointer)
        triples ++= pointed.graph.triples
        triples += Triple(newBNode, rdf.rest, current)
        current = newBNode
      }
      PointedGraph(current, Graph(triples))
    }
  }

  implicit def EitherToPG[Rdf <: RDF, T1, T2](implicit ops: RDFOps[Rdf], toPG1: ToPG[Rdf, T1], toPG2: ToPG[Rdf, T2]): ToPG[Rdf, Either[T1, T2]] = new ToPG[Rdf, Either[T1, T2]] {
    import ops._
    def toPG(t: Either[T1, T2]): PointedGraph[Rdf] = t match {
      case Left(t1) => bnode().a(rdf("Either")).a(rdf("Left")) -- rdf("left") ->- toPG1.toPG(t1)
      case Right(t2) => bnode().a(rdf("Either")).a(rdf("Right")) -- rdf("right") ->- toPG2.toPG(t2)
    }
  }

  implicit def Tuple2ToPG[Rdf <: RDF, T1, T2](implicit ops: RDFOps[Rdf], toPG1: ToPG[Rdf, T1], toPG2: ToPG[Rdf, T2]): ToPG[Rdf, (T1, T2)] = new ToPG[Rdf, (T1, T2)] {
    import ops._
    def toPG(t: (T1, T2)): PointedGraph[Rdf] = (
      bnode().a(rdf("Tuple2"))
      -- rdf("_1") ->- t._1
      -- rdf("_2") ->- t._2
    )
  }

  implicit def MapToPG[Rdf <: RDF, K, V](implicit ops: RDFOps[Rdf], kToPG: ToPG[Rdf, K], vToPG2: ToPG[Rdf, V]): ToPG[Rdf, Map[K, V]] = new ToPG[Rdf, Map[K, V]] {
    val ListKVToPG = implicitly[ToPG[Rdf, List[(K, V)]]]
    def toPG(t: Map[K, V]): PointedGraph[Rdf] =
      ListKVToPG.toPG(t.toList)
  }

  implicit def OptionToPG[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], to: ToPG[Rdf, T]): ToPG[Rdf, Option[T]] = new ToPG[Rdf, Option[T]] {
    val ListToPG = implicitly[ToPG[Rdf, List[T]]]
    def toPG(t: Option[T]): PointedGraph[Rdf] =
      ListToPG.toPG(t.toList)
  }

}
