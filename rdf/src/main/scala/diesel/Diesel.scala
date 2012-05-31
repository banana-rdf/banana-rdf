package org.w3.banana.diesel

import org.w3.banana._
import scalaz._
import scalaz.Scalaz._
import scalaz.Validation._

object Diesel {
  def apply[Rdf <: RDF](ops: RDFOperations[Rdf], union: GraphUnion[Rdf], graphTraversal: RDFGraphTraversal[Rdf]): Diesel[Rdf] =
    new Diesel(ops, union, graphTraversal)
}

class Diesel[Rdf <: RDF](
  ops: RDFOperations[Rdf],
  union: GraphUnion[Rdf],
  graphTraversal: RDFGraphTraversal[Rdf]) extends RDFNodeProjections[Rdf](ops) {

  import ops._
  import union._
  import graphTraversal._

  val rdf = RDFPrefix(ops)

  private val commonLiteralBinders = CommonLiteralBinders(ops)
  implicit val stringBinder = commonLiteralBinders.StringBinder
  implicit val intBinder = commonLiteralBinders.IntBinder
  implicit val doubleBinder = commonLiteralBinders.DoubleBinder
  implicit val dateTimeBinder = commonLiteralBinders.DateTimeBinder

  class PointedGraphW(pointed: PointedGraph[Rdf]) {

    import pointed.{ node => _node , graph }

    def a(clazz: Rdf#URI): PointedGraph[Rdf] = {
      val newGraph = graph union Graph(Triple(node, rdf("type"), clazz))
      PointedGraph(node, newGraph)
    }

    def --(p: Rdf#URI): PointedGraphPredicate = new PointedGraphPredicate(pointed, p)

    def -<-(p: Rdf#URI): PredicatePointedGraph = new PredicatePointedGraph(p, pointed)

    def /(p: Rdf#URI): PointedGraphs[Rdf] = {
      val nodes = getObjects(graph, node, p)
      PointedGraphs(nodes, graph)
    }

    def node: Rdf#Node = _node

    def predicates = getPredicates(graph, node)

    def asList[T](implicit binder: LiteralBinder[Rdf, T]): Validation[BananaException, List[T]] =
      try {
        var elems = List[T]()
        var current = node
        while(current != rdf.nil) {
          (getObjects(graph, current, rdf.first).toList, getObjects(graph, current, rdf.rest).toList) match {
            case (List(first), List(rest)) => {
              elems ::= first.asLiteral.flatMap(binder.fromLiteral).fold(be => throw be, e => e)
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

  class PointedGraphsW(pointedGraphs: PointedGraphs[Rdf]) extends Iterable[PointedGraph[Rdf]] {

    import pointedGraphs.{ nodes, graph }

    def iterator = nodes.iterator map { PointedGraph(_, graph) }

    def /(p: Rdf#URI): PointedGraphs[Rdf] = {
      val ns = this flatMap { case PointedGraph(node, graph) => getObjects(graph, node, p) }
      PointedGraphs(ns, graph)
    }

    def takeOneNode: Validation[BananaException, Rdf#Node] = {
      val first = nodes.iterator.next
      if (first == null)
        Failure(WrongExpectation("not even one node"))
      else
        Success(first)
    }

    def takeOneUri: Validation[BananaException, Rdf#URI] =
      takeOneNode.flatMap(_.asUri)

    def takeOne[T](implicit binder: LiteralBinder[Rdf, T]): Validation[BananaException, T] =
      takeOneNode.flatMap(_.as[T])

    def exactlyOnePointedGraph: Validation[BananaException, PointedGraph[Rdf]] = {
      val it = nodes.iterator
      val first = it.next
      if (first == null)
        Failure(WrongExpectation("expected exactly one node but got 0"))
      else if (it.hasNext)
        Failure(WrongExpectation("expected exactly one node but got more than 1"))
      else
        Success(PointedGraph(first, graph))
    }

    def exactlyOneNode: Validation[BananaException, Rdf#Node] = {
      val it = nodes.iterator
      val first = it.next
      if (first == null)
        Failure(WrongExpectation("expected exactly one node but got 0"))
      else if (it.hasNext)
        Failure(WrongExpectation("expected exactly one node but got more than 1"))
      else
        Success(first)
    }

    def exactlyOneUri: Validation[BananaException, Rdf#URI] =
      exactlyOneNode.flatMap(_.asUri)

    def exactlyOne[T](implicit binder: LiteralBinder[Rdf, T]): Validation[BananaException, T] =
      exactlyOneNode.flatMap(_.as[T])

    def asOption[T](implicit binder: LiteralBinder[Rdf, T]): Validation[BananaException, Option[T]] = nodes.headOption match {
      case None => Success(None)
      case Some(node) => node.as[T] map (Some(_))
    }

    def asList[T](implicit binder: LiteralBinder[Rdf, T]): Validation[BananaException, List[T]] =
      exactlyOnePointedGraph flatMap (_.asList[T])
    
  }

  class PointedGraphPredicate(pointed: PointedGraph[Rdf], p: Rdf#URI) {

    def ->-(o: Rdf#Node, os: Rdf#Node*): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val graph =
        if (os.isEmpty) {
          acc union Graph(Triple(s, p, o))
        } else {
          val triples: Iterable[Rdf#Triple] = (o :: os.toList) map { o => Triple(s, p, o) }
          Graph(triples) union acc
        }
      PointedGraph(s, graph)
    }

    def ->-[T](o: T)(implicit binder: LiteralBinder[Rdf, T]): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val graph = acc union Graph(Triple(s, p, binder.toLiteral(o)))
      PointedGraph(s, graph)
    }

    def ->-[T1, T2](o1: T1, o2: T2)(implicit b1: LiteralBinder[Rdf, T1], b2: LiteralBinder[Rdf, T2]): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val graph = acc union Graph(Triple(s, p, b1.toLiteral(o1)), Triple(s, p, b2.toLiteral(o2)))
      PointedGraph(s, graph)
    }

    def ->-(pointedObject: PointedGraph[Rdf]): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val PointedGraph(o, graphObject) = pointedObject
      val graph = Graph(Triple(s, p, o)) union acc union graphObject
      PointedGraph(s, graph)
    }

    def ->-[T](opt: Option[T])(implicit binder: LiteralBinder[Rdf, T]): PointedGraph[Rdf] = opt match {
      case None => pointed
      case Some(t) => this.->-(t)
    }

    def ->-[T](collection: List[T])(implicit binder: LiteralBinder[Rdf, T]): PointedGraph[Rdf] = {
      var current: Rdf#Node = rdf.nil
      val triples = scala.collection.mutable.Set[Rdf#Triple]()
      collection.reverse foreach { a =>
        val newBNode = BNode()
        triples += Triple(newBNode, rdf.first, binder.toLiteral(a))
        triples += Triple(newBNode, rdf.rest, current)
        current = newBNode
      }
      val PointedGraph(s, acc) = pointed
      triples += Triple(s, p, current)
      val graph = acc union Graph(triples)
      PointedGraph(s, graph)
    }

  }


  case class PredicatePointedGraph(p: Rdf#URI, pointed: PointedGraph[Rdf]) {

    def --(s: Rdf#Node): PointedGraph[Rdf] = {
      val PointedGraph(o, acc) = pointed
      val graph = acc union Graph(Triple(s, p, o))
      PointedGraph(s, graph)
    }

    def --(pointedSubject: PointedGraph[Rdf]): PointedGraph[Rdf] = {
      val PointedGraph(o, acc) = pointed
      val PointedGraph(s, graphObject) = pointedSubject
      val graph = Graph(Triple(s, p, o)) union acc union graphObject
      PointedGraph(s, graph)
    }

  }

  implicit def node2PointedGraphW(node: Rdf#Node): PointedGraphW = new PointedGraphW(new PointedGraph[Rdf](node, Graph.empty))

  implicit def pointedGraph2PointedGraphW(pointed: PointedGraph[Rdf]): PointedGraphW = new PointedGraphW(pointed)

  implicit def multiplePointedGraph2PointedGraphsW(pointedGraphs: PointedGraphs[Rdf]): PointedGraphsW = new PointedGraphsW(pointedGraphs)

  def bnode(): Rdf#BNode = BNode()

  def bnode(label: String) = BNode(label)

  def uri(s: String): Rdf#URI = URI(s)

}
