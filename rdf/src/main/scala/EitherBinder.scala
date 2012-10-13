package org.w3.banana

import scala.util._

trait EitherBinder[Rdf <: RDF] {
  this: Diesel[Rdf] =>

  import ops._

  implicit def EitherBinder[T1, T2](implicit b1: PointedGraphBinder[Rdf, T1], b2: PointedGraphBinder[Rdf, T2]): PointedGraphBinder[Rdf, Either[T1, T2]] = new PointedGraphBinder[Rdf, Either[T1, T2]] {

    def fromPointedGraph(pointed: PointedGraph[Rdf]): Try[Either[T1, T2]] = {
      if (pointed isA rdf("Left"))
        (pointed / rdf("left")).as[T1] flatMap (v => Success(Left(v)))
      else if (pointed isA rdf("Right"))
        (pointed / rdf("right")).as[T2] flatMap (v => Success(Right(v)))
      else
        Failure(FailedConversion(pointed.toString + " is not an Either"))
    }

    def toPointedGraph(t: Either[T1, T2]): PointedGraph[Rdf] = t match {
      case Left(t1) => bnode().a(rdf("Either")).a(rdf("Left")) -- rdf("left") ->- b1.toPointedGraph(t1)
      case Right(t2) => bnode().a(rdf("Either")).a(rdf("Right")) -- rdf("right") ->- b2.toPointedGraph(t2)
    }

  }

}
