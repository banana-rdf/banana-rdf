package org.w3.banana

import scalaz._
import scalaz.Validation._
import org.joda.time.DateTime
import NodeBinder._

trait CommonBinders[Rdf <: RDF] {
this: Diesel[Rdf] =>

  import ops._
  import graphTraversal._

  implicit val StringBinder: NodeBinder[Rdf, String] = new NodeBinder[Rdf, String] {

    def fromNode(node: Rdf#Node): Validation[BananaException, String] =
      asTypedLiteral(node) flatMap {
        case TypedLiteral(lexicalForm, datatype) =>
          if (datatype == xsd.string)
            Success(lexicalForm)
          else
            Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
      }

    def toNode(t: String): Rdf#Node = TypedLiteral(t, xsd.string)

  }


  implicit val IntBinder: NodeBinder[Rdf, Int] = new NodeBinder[Rdf, Int] {

    def fromNode(node: Rdf#Node): Validation[BananaException, Int] = {
      asTypedLiteral(node) flatMap {
        case TypedLiteral(lexicalForm, datatype) =>
          if (datatype == xsd.int)
            Success(lexicalForm.toInt)
          else
            Failure(FailedConversion(lexicalForm + " may be convertible to an Integer but has following datatype: " + datatype))
      }
    }

    def toNode(t: Int): Rdf#Node = TypedLiteral(t.toString, xsd.int)

  }

  implicit val DoubleBinder: NodeBinder[Rdf, Double] = new NodeBinder[Rdf, Double] {

    def fromNode(node: Rdf#Node): Validation[BananaException, Double] = {
      asTypedLiteral(node) flatMap {
        case TypedLiteral(lexicalForm, datatype) =>
          if (datatype == xsd.double)
            Success(lexicalForm.toDouble)
          else
            Failure(FailedConversion(lexicalForm + " may be convertible to an Double but has following datatype: " + datatype))
      }
    }

    def toNode(t: Double): Rdf#Node = TypedLiteral(t.toString, xsd.double)

  }

  implicit val DateTimeBinder: NodeBinder[Rdf, DateTime] = new NodeBinder[Rdf, DateTime] {

    def fromNode(node: Rdf#Node): Validation[BananaException, DateTime] = {
      asTypedLiteral(node) flatMap {
        case TypedLiteral(lexicalForm, datatype) =>
          if (datatype == xsd.dateTime)
            try {
              Success(DateTime.parse(lexicalForm))
            } catch {
              case t => Failure(FailedConversion(node.toString + " is of type xsd:dateTime but its lexicalForm could not be parsed: " + lexicalForm))
            }
          else
            Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
      }
    }

    def toNode(t: DateTime): Rdf#Node = TypedLiteral(t.toString, xsd.dateTime)

  }

  // if you have a binder for T, you get automatically a binder for List[T]
  implicit def ListPointedGraphBinder[T](implicit binder: NodeBinder[Rdf, T]): PointedGraphBinder[Rdf, List[T]] = new PointedGraphBinder[Rdf, List[T]] {

    def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, List[T]] = {
      import pointed.{ node , graph }
      var elems = List[T]()
      var current = node
      try {
        while(current != rdf.nil) {
          (getObjects(graph, current, rdf.first).toList, getObjects(graph, current, rdf.rest).toList) match {
            case (List(first), List(rest)) => {
              elems ::= binder.fromNode(first).fold(be => throw be, e => e)
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
        triples += Triple(newBNode, rdf.first, binder.toNode(a))
        triples += Triple(newBNode, rdf.rest, current)
        current = newBNode
      }
      PointedGraph(current, Graph(triples))
    }

  }


  implicit def Tuple2PointedGraphBinder[T1, T2](implicit b1: PointedGraphBinder[Rdf, T1], b2: PointedGraphBinder[Rdf, T2]): PointedGraphBinder[Rdf, (T1, T2)] = new PointedGraphBinder[Rdf, (T1, T2)] {

    def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, (T1, T2)] =
      for {
        t1 <- (pointed / rdf("_1")).as[T1]
        t2 <- (pointed / rdf("_2")).as[T2]
      } yield (t1, t2)

    def toPointedGraph(t: (T1, T2)): PointedGraph[Rdf] = (
      bnode().a(rdf("Tuple2"))
        -- rdf("_1") -->- t._1
        -- rdf("_2") -->- t._2
    )

  }


  implicit val UriBinder: NodeBinder[Rdf, Rdf#URI] = new NodeBinder[Rdf, Rdf#URI] {

    def fromNode(node: Rdf#Node): Validation[BananaException, Rdf#URI] =
      Node.fold(node)(
        uri => Success(uri),
        bnode => Failure(FailedConversion(node + " is a BNode while I was expecting a URI")),
        literal => Failure(FailedConversion(node + " is a Literal while I was expecting a URI"))
      )


    def toNode(t: Rdf#URI): Rdf#Node = t

  }




}
