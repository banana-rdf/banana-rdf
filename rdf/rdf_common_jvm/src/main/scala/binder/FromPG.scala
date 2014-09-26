package org.w3.banana.binder

import org.w3.banana._
import org.w3.banana.diesel._

import scala.util._

trait FromPG[Rdf <: RDF, +T] {

  def fromPG(pointed: PointedGraph[Rdf]): Try[T]

}

object FromPG {

  implicit def PointedGraphFromPG[Rdf <: RDF] =
    new FromPG[Rdf, PointedGraph[Rdf]] {
      def fromPG(pointed: PointedGraph[Rdf]): Try[PointedGraph[Rdf]] = Success(pointed)
    }

  implicit def FromNodeFromPG[Rdf <: RDF, T](implicit from: FromNode[Rdf, T]) =
    new FromPG[Rdf, T] {
      def fromPG(pointed: PointedGraph[Rdf]): Try[T] = from.fromNode(pointed.pointer)
    }

  implicit def ListFromPG[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], from: FromPG[Rdf, T]): FromPG[Rdf, List[T]] = new FromPG[Rdf, List[T]] {
    import ops._
    def fromPG(pointed: PointedGraph[Rdf]): Try[List[T]] = {
      import pointed.{ graph, pointer }
      var elems = List[T]()
      var current = pointer
      Try {
        while (current != rdf.nil) {
          (getObjects(graph, current, rdf.first).toList, getObjects(graph, current, rdf.rest).toList) match {
            case (List(first), List(rest)) => {
              val firstPointed = PointedGraph(first, pointed.graph)
              elems ::= from.fromPG(firstPointed).get
              current = rest
            }
            case other => throw new FailedConversion("asList: couldn't decode a list")
          }
        }
        elems.reverse
      }
    }
  }

  implicit def EitherFromPG[Rdf <: RDF, T1, T2](implicit ops: RDFOps[Rdf], fromPG1: FromPG[Rdf, T1], fromPG2: FromPG[Rdf, T2]): FromPG[Rdf, Either[T1, T2]] = new FromPG[Rdf, Either[T1, T2]] {
    import ops._
    def fromPG(pointed: PointedGraph[Rdf]): Try[Either[T1, T2]] = {
      if (pointed isA rdf("Left"))
        (pointed / rdf("left")).as[T1] flatMap (v => Success(Left(v)))
      else if (pointed isA rdf("Right"))
        (pointed / rdf("right")).as[T2] flatMap (v => Success(Right(v)))
      else
        Failure(FailedConversion(pointed.toString + " is not an Either"))
    }
  }

  implicit def Tuple2FromPG[Rdf <: RDF, T1, T2](implicit ops: RDFOps[Rdf], fromPG1: FromPG[Rdf, T1], fromPG2: FromPG[Rdf, T2]): FromPG[Rdf, (T1, T2)] = new FromPG[Rdf, (T1, T2)] {
    import ops._
    def fromPG(pointed: PointedGraph[Rdf]): Try[(T1, T2)] = {
      for {
        t1 <- (pointed / rdf("_1")).as[T1]
        t2 <- (pointed / rdf("_2")).as[T2]
      } yield (t1, t2)
    }
  }

  implicit def MapFromPG[Rdf <: RDF, K, V](implicit ops: RDFOps[Rdf], kFromPG: FromPG[Rdf, K], vFromPG: FromPG[Rdf, V]): FromPG[Rdf, Map[K, V]] = new FromPG[Rdf, Map[K, V]] {
    val ListKVFromPG = implicitly[FromPG[Rdf, List[(K, V)]]]
    def fromPG(pointed: PointedGraph[Rdf]): Try[Map[K, V]] =
      ListKVFromPG.fromPG(pointed) map { l => Map(l: _*) }
  }

  implicit def OptionFromPG[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], from: FromPG[Rdf, T]): FromPG[Rdf, Option[T]] = new FromPG[Rdf, Option[T]] {
    val ListFromPG = implicitly[FromPG[Rdf, List[T]]]
    def fromPG(pointed: PointedGraph[Rdf]): Try[Option[T]] =
      ListFromPG.fromPG(pointed) map { _.headOption }
  }

}
