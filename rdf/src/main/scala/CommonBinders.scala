package org.w3.banana

import scalaz._
import scalaz.Validation._
import org.joda.time.DateTime
import NodeBinder._

object CommonBinders {

}

trait CommonBinders[Rdf <: RDF] {
this: Diesel[Rdf] =>

  import ops._
  import graphTraversal._

  implicit val StringBinder: TypedLiteralBinder[Rdf, String] = new TypedLiteralBinder[Rdf, String] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Validation[BananaException, String] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.string)
        Success(lexicalForm)
      else
        Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
    }

    def toTypedLiteral(t: String): Rdf#TypedLiteral = TypedLiteral(t, xsd.string)

  }


  implicit val IntBinder: TypedLiteralBinder[Rdf, Int] = new TypedLiteralBinder[Rdf, Int] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Validation[BananaException, Int] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.int)
        Success(lexicalForm.toInt)
      else
        Failure(FailedConversion(lexicalForm + " may be convertible to an Integer but has following datatype: " + datatype))
    }

    def toTypedLiteral(t: Int): Rdf#TypedLiteral = TypedLiteral(t.toString, xsd.int)

  }

  implicit val DoubleBinder: TypedLiteralBinder[Rdf, Double] = new TypedLiteralBinder[Rdf, Double] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Validation[BananaException, Double] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.double)
        Success(lexicalForm.toDouble)
      else
        Failure(FailedConversion(lexicalForm + " may be convertible to an Double but has following datatype: " + datatype))
    }

    def toTypedLiteral(t: Double): Rdf#TypedLiteral = TypedLiteral(t.toString, xsd.double)

  }

  implicit val DateTimeBinder: TypedLiteralBinder[Rdf, DateTime] = new TypedLiteralBinder[Rdf, DateTime] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Validation[BananaException, DateTime] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.dateTime)
        try {
          Success(DateTime.parse(lexicalForm))
        } catch {
          case t => Failure(FailedConversion(literal.toString + " is of type xsd:dateTime but its lexicalForm could not be parsed: " + lexicalForm))
        }
      else
        Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
    }

    def toTypedLiteral(t: DateTime): Rdf#TypedLiteral = TypedLiteral(t.toString, xsd.dateTime)

  }

  // if you have a binder for T, you get automatically a binder for List[T]
  implicit def ListBinder[T](implicit binder: PointedGraphBinder[Rdf, T]): PointedGraphBinder[Rdf, List[T]] = new PointedGraphBinder[Rdf, List[T]] {

    def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, List[T]] = {
      import pointed.{ node , graph }
      var elems = List[T]()
      var current = node
      try {
        while(current != rdf.nil) {
          (getObjects(graph, current, rdf.first).toList, getObjects(graph, current, rdf.rest).toList) match {
            case (List(first), List(rest)) => {
              val firstPointed = PointedGraph(first, pointed.graph)
              elems ::= binder.fromPointedGraph(firstPointed).fold(be => throw be, e => e)
              current = rest
            }
            case _ => throw new FailedConversion("asList: couldn't decode a list")
          }
        }
        Success(elems.reverse)
      } catch {
        case be: BananaException => Failure(be)
      }
    }

    def toPointedGraph(t: List[T]): PointedGraph[Rdf] = {
      var current: Rdf#Node = rdf.nil
      val triples = scala.collection.mutable.Set[Rdf#Triple]()
      t.reverse foreach { a =>
        val newBNode = BNode()
        val pointed = binder.toPointedGraph(a)
        triples += Triple(newBNode, rdf.first, pointed.node)
        triples ++= pointed.graph
        triples += Triple(newBNode, rdf.rest, current)
        current = newBNode
      }
      PointedGraph(current, Graph(triples))
    }

  }


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





  implicit def MapBinder[K, V](implicit kbinder: PointedGraphBinder[Rdf, K], vbinder: PointedGraphBinder[Rdf, V]): PointedGraphBinder[Rdf, Map[K, V]] = new PointedGraphBinder[Rdf, Map[K, V]] {

    val binder = implicitly[PointedGraphBinder[Rdf, List[(K, V)]]]

    def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, Map[K, V]] = binder.fromPointedGraph(pointed) map { l => Map() ++ l }

    def toPointedGraph(t: Map[K, V]): PointedGraph[Rdf] =
      binder.toPointedGraph(t.toList)

  }



}
