package org.w3.banana

import org.w3.banana.binder._
import org.w3.banana.diesel._

import scala.util._

class PointedGraphs[Rdf <: RDF](val nodes: Iterable[Rdf#Node], val graph: Rdf#Graph) extends Iterable[PointedGraph[Rdf]] {

  override def toString: String = {
    nodes.mkString("[", " ; ", "]")
  }

  def iterator = nodes.iterator map { PointedGraph(_, graph) }

  def /(p: Rdf#URI)(implicit ops: RDFOps[Rdf]): PointedGraphs[Rdf] = {
    val ns: Iterable[Rdf#Node] = this flatMap { pointed: PointedGraph[Rdf] =>
      import pointed.pointer
      ops.getObjects(graph, pointer, p)
    }
    new PointedGraphs[Rdf](ns, graph)
  }

  def takeOnePointedGraph: Try[PointedGraph[Rdf]] = {
    val it = nodes.iterator
    if (!it.hasNext) {
      Failure(WrongExpectation("expected exactly one node but got 0"))
    } else {
      val first = it.next
      Success(PointedGraph(first, graph))
    }
  }

  def exactlyOneAs[T](implicit fromPG: FromPG[Rdf, T]): Try[T] =
    exactlyOnePointedGraph flatMap (_.as[T])

  def exactlyOnePointedGraph: Try[PointedGraph[Rdf]] = {
    val it = nodes.iterator
    if (!it.hasNext) {
      Failure(WrongExpectation("expected exactly one node but got 0"))
    } else {
      val first = it.next
      if (it.hasNext)
        Failure(WrongExpectation("expected exactly one node but got more than 1"))
      else
        Success(PointedGraph(first, graph))
    }
  }

  def as[T](implicit fromPG: FromPG[Rdf, T]): Try[T] =
    takeOnePointedGraph flatMap (_.as[T])

  def as2[T1, T2](implicit fromPG1: FromPG[Rdf, T1], fromPG2: FromPG[Rdf, T2]): Try[(T1, T2)] =
    takeOnePointedGraph flatMap { _.as2[T1, T2] }

  /**
   * returns optionally a T (though the implicit binder) if it is available.
   * that's a good way to know if a particular rdf object was there
   *
   * note: this is very different from as[Option[T]], which is an encoding of an Option in RDF
   */
  def asOption[T](implicit fromPG: FromPG[Rdf, T]): Try[Option[T]] = headOption match {
    case None => Success(None)
    case Some(pointed) => pointed.as[T] map (Some(_))
  }

  def asOption2[T1, T2](implicit fromPG1: FromPG[Rdf, T1], fromPG2: FromPG[Rdf, T2]): Try[Option[(T1, T2)]] = headOption match {
    case None => Success(None)
    case Some(pointed) =>
      for {
        t1 <- pointed.as[T1]
        t2 <- pointed.as[T2]
      } yield Some((t1, t2))
  }

  /**
   * sees the nodes for this PointedGraphs as an iterator, after they were bound successfully to
   * a T thought an implicit PGBinder.
   * it is a success only if all the bindings were successful themselves
   *
   * note: this is very different from as[List[T]], which is an encoding of a List in RDF
   */
  def asSet[T](implicit fromPG: FromPG[Rdf, T]): Try[Set[T]] = Try {
    this.iterator.foldLeft[Set[T]](Set.empty[T]) { case (acc, pg) => acc + fromPG.fromPG(pg).get }
  }

  def asSet2[T1, T2](implicit fromPG1: FromPG[Rdf, T1], fromPG2: FromPG[Rdf, T2]): Try[Set[(T1, T2)]] =
    this.iterator.foldLeft[Try[Set[(T1, T2)]]](Success(Set.empty)) {
      case (accT, g) =>
        for {
          acc <- accT
          t1 <- g.as[T1]
          t2 <- g.as[T2]
        } yield acc + ((t1, t2))
    }
}
