package org.w3.banana

import scalaz._
import scalaz.Validation._
import org.joda.time.DateTime
import NodeBinder._

object CommonBinders {

  def apply[Rdf <: RDF]()(implicit ops: RDFOperations[Rdf], graphTraversal: RDFGraphTraversal[Rdf]): CommonBinders[Rdf] =
    new CommonBinders[Rdf]()

}

class CommonBinders[Rdf <: RDF]()(implicit ops: RDFOperations[Rdf], graphTraversal: RDFGraphTraversal[Rdf]) {

  import ops._
  import graphTraversal._

  // prefixed by "__" just to avoid the silly name clashes despite the private modifier...
  private val __xsd = XSDPrefix(ops)
  private val __rdf = RDFPrefix(ops)

  implicit val StringBinder: NodeBinder[Rdf, String] = new NodeBinder[Rdf, String] {

    def fromNode(node: Rdf#Node): Validation[BananaException, String] =
      asTypedLiteral(node) flatMap {
        case TypedLiteral(lexicalForm, datatype) =>
          if (datatype == __xsd.string)
            Success(lexicalForm)
          else
            Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
      }

    def toNode(t: String): Rdf#Node = TypedLiteral(t, __xsd.string)

  }


  implicit val IntBinder: NodeBinder[Rdf, Int] = new NodeBinder[Rdf, Int] {

    def fromNode(node: Rdf#Node): Validation[BananaException, Int] = {
      asTypedLiteral(node) flatMap {
        case TypedLiteral(lexicalForm, datatype) =>
          if (datatype == __xsd.int)
            Success(lexicalForm.toInt)
          else
            Failure(FailedConversion(lexicalForm + " may be convertible to an Integer but has following datatype: " + datatype))
      }
    }

    def toNode(t: Int): Rdf#Node = TypedLiteral(t.toString, __xsd.int)

  }

  implicit val DoubleBinder: NodeBinder[Rdf, Double] = new NodeBinder[Rdf, Double] {

    def fromNode(node: Rdf#Node): Validation[BananaException, Double] = {
      asTypedLiteral(node) flatMap {
        case TypedLiteral(lexicalForm, datatype) =>
          if (datatype == __xsd.double)
            Success(lexicalForm.toDouble)
          else
            Failure(FailedConversion(lexicalForm + " may be convertible to an Double but has following datatype: " + datatype))
      }
    }

    def toNode(t: Double): Rdf#Node = TypedLiteral(t.toString, __xsd.double)

  }

  implicit val DateTimeBinder: NodeBinder[Rdf, DateTime] = new NodeBinder[Rdf, DateTime] {

    def fromNode(node: Rdf#Node): Validation[BananaException, DateTime] = {
      asTypedLiteral(node) flatMap {
        case TypedLiteral(lexicalForm, datatype) =>
          if (datatype == __xsd.dateTime)
            try {
              Success(DateTime.parse(lexicalForm))
            } catch {
              case t => Failure(FailedConversion(node.toString + " is of type xsd:dateTime but its lexicalForm could not be parsed: " + lexicalForm))
            }
          else
            Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
      }
    }

    def toNode(t: DateTime): Rdf#Node = TypedLiteral(t.toString, __xsd.dateTime)

  }

  // if you have a binder for T, you get automatically a binder for List[T]
  implicit def ListPointedGraphBinder[T](binder: NodeBinder[Rdf, T]): PointedGraphBinder[Rdf, List[T]] = new PointedGraphBinder[Rdf, List[T]] {

    def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, List[T]] = {
      import pointed.{ node , graph }
      var elems = List[T]()
      var current = node
      try {
        while(current != __rdf.nil) {
          (getObjects(graph, current, __rdf.first).toList, getObjects(graph, current, __rdf.rest).toList) match {
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
      var current: Rdf#Node = __rdf.nil
      val triples = scala.collection.mutable.Set[Rdf#Triple]()
      t.reverse foreach { a =>
        val newBNode = BNode()
        triples += Triple(newBNode, __rdf.first, binder.toNode(a))
        triples += Triple(newBNode, __rdf.rest, current)
        current = newBNode
      }
      PointedGraph(current, Graph(triples))
    }

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
