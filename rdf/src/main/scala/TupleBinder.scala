package org.w3.banana

import org.w3.banana.scalaz._

trait TupleBinder[Rdf <: RDF] {
this: Diesel[Rdf] =>

  import ops._
  import graphTraversal._

  implicit def Tuple2Binder[T1, T2](implicit b1: PointedGraphBinder[Rdf, T1], b2: PointedGraphBinder[Rdf, T2]): PointedGraphBinder[Rdf, (T1, T2)] = new PointedGraphBinder[Rdf, (T1, T2)] {

    def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, (T1, T2)] =
      for {
        t1 <- (pointed / rdf("_1")).as[T1]
        t2 <- (pointed / rdf("_2")).as[T2]
      } yield (t1, t2)

    def toPointedGraph(t: (T1, T2)): PointedGraph[Rdf] = (
      bnode().a(rdf("Tuple2"))
        -- rdf("_1") ->- t._1
        -- rdf("_2") ->- t._2
    )

  }


  implicit def Tuple3Binder[T1, T2, T3](
    implicit b1: PointedGraphBinder[Rdf, T1],
    b2: PointedGraphBinder[Rdf, T2],
    b3: PointedGraphBinder[Rdf, T3]): PointedGraphBinder[Rdf, (T1, T2, T3)] = new PointedGraphBinder[Rdf, (T1, T2, T3)] {

    def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, (T1, T2, T3)] =
      for {
        t1 <- (pointed / rdf("_1")).as[T1]
        t2 <- (pointed / rdf("_2")).as[T2]
        t3 <- (pointed / rdf("_3")).as[T3]
      } yield (t1, t2, t3)

    def toPointedGraph(t: (T1, T2, T3)): PointedGraph[Rdf] = (
      bnode().a(rdf("Tuple3"))
        -- rdf("_1") ->- t._1
        -- rdf("_2") ->- t._2
        -- rdf("_3") ->- t._3
    )

  }


}
