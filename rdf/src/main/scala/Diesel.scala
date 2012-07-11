package org.w3.banana

import scalaz._
import scalaz.Scalaz._
import scalaz.Validation._
import NodeBinder._

object Diesel {
  def apply[Rdf <: RDF](implicit ops: RDFOperations[Rdf]): Diesel[Rdf] = new Diesel()(ops)
}

class Diesel[Rdf <: RDF]()(implicit val ops: RDFOperations[Rdf])
    extends syntax.RDFOperationsSyntax[Rdf]
    with syntax.GraphSyntax[Rdf]
    with syntax.NodeSyntax[Rdf]
    with syntax.URISyntax[Rdf]
    with syntax.LiteralSyntax[Rdf]
    with syntax.TypedLiteralSyntax[Rdf]
    with syntax.LangLiteralSyntax[Rdf]
    with syntax.StringSyntax[Rdf]
    with syntax.AnySyntax[Rdf]
    with CommonBinders[Rdf]
    with ListBinder[Rdf]
    with TupleBinder[Rdf]
    with MapBinder[Rdf]
    with EitherBinder[Rdf]
    with RecordBinder[Rdf] {

  import ops._

  val xsd = XSDPrefix(ops)
  val rdf = RDFPrefix(ops)
  val dc = DCPrefix(ops)
  val foaf = FOAFPrefix(ops)

  implicit def toPointedGraphW(node: Rdf#Node): PointedGraphW = new PointedGraphW(PointedGraph(node))

  class PointedGraphW(pointed: PointedGraph[Rdf]) {

    import pointed.graph

    def as[T](implicit binder: PointedGraphBinder[Rdf, T]): Validation[BananaException, T] =
      binder.fromPointedGraph(pointed)

    def a(clazz: Rdf#URI): PointedGraph[Rdf] = {
      val newGraph = graph union Graph(Triple(node, rdf("type"), clazz))
      PointedGraph(node, newGraph)
    }

    def --(p: Rdf#URI): PointedGraphPredicate = new PointedGraphPredicate(pointed, p)

    def -<-(p: Rdf#URI): PredicatePointedGraph = new PredicatePointedGraph(p, pointed)

    def /(p: Rdf#URI): PointedGraphs = {
      val nodes = getObjects(graph, node, p)
      new PointedGraphs(nodes, graph)
    }

    def node: Rdf#Node = pointed.node

    def predicates = getPredicates(graph, node)

    def isA(clazz: Rdf#URI): Boolean = {
      def isAIfNodeOrBNode = {
        val classes = getObjects(graph, node, rdf("type"))
        classes exists { _ == clazz }
      }
      node.fold(
        uri => isAIfNodeOrBNode,
        bnode => isAIfNodeOrBNode,
        literal => false
      )
    }

  }

  class PointedGraphs(val nodes: Iterable[Rdf#Node], val graph: Rdf#Graph) extends Iterable[PointedGraph[Rdf]] {

    def iterator = nodes.iterator map { PointedGraph(_, graph) }

    def /(p: Rdf#URI): PointedGraphs = {
      val ns = this flatMap { case PointedGraph(node, graph) => getObjects(graph, node, p) }
      new PointedGraphs(ns, graph)
    }

    def takeOnePointedGraph: Validation[BananaException, PointedGraph[Rdf]] = {
      val it = nodes.iterator
      if (!it.hasNext) {
        Failure(WrongExpectation("expected exactly one node but got 0"))
      } else {
        val first = it.next
        Success(PointedGraph(first, graph))
      }
    }

    def takeOneAs[T](implicit binder: PointedGraphBinder[Rdf, T]): Validation[BananaException, T] =
      takeOnePointedGraph flatMap (_.as[T])

    def exactlyOnePointedGraph: Validation[BananaException, PointedGraph[Rdf]] = {
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

    def as[T](implicit binder: PointedGraphBinder[Rdf, T]): Validation[BananaException, T] =
      exactlyOnePointedGraph flatMap (_.as[T])

    def asOption[T](implicit binder: PointedGraphBinder[Rdf, T]): Validation[BananaException, Option[T]] = headOption match {
      case None => Success(None)
      case Some(pointed) => pointed.as[T] map (Some(_))
    }

  }

  class PointedGraphPredicate(pointed: PointedGraph[Rdf], p: Rdf#URI) {

    def ->-(o: Rdf#Node, os: Rdf#Node*): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val graph: Rdf#Graph =
        if (os.isEmpty) {
          val g = Graph(Triple(s, p, o))
          graphWrapper(acc).union(g)
        } else {
          val triples: Iterable[Rdf#Triple] = (o :: os.toList) map { o => Triple(s, p, o) }
          Graph(triples) union acc
        }
      PointedGraph(s, graph)
    }

    def ->-(pointedObject: PointedGraph[Rdf]): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val PointedGraph(o, graphObject) = pointedObject
      val graph = Graph(Triple(s, p, o)) union acc union graphObject
      PointedGraph(s, graph)
    }

    def ->-[T](o: T)(implicit binder: PointedGraphBinder[Rdf, T]): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val pg = binder.toPointedGraph(o)
      this.->-(pg)
    }

    def ->-[T](opt: Option[T])(implicit binder: PointedGraphBinder[Rdf, T]): PointedGraph[Rdf] = opt match {
      case None => pointed
      case Some(t) => this.->-(t)(binder)
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

  class GraphW(graph: Rdf#Graph) {

    def getAllInstancesOf(clazz: Rdf#URI): PointedGraphs = {
      val instances = getSubjects(graph, rdf("type"), clazz): Iterable[Rdf#Node]
      new PointedGraphs(instances, graph)
    }

  }

  /* looks like implicit resolution does not work if Rdf is not fixed...  */

  implicit def UriToNodeBinder[T](implicit binder: URIBinder[Rdf, T]): NodeBinder[Rdf, T] = URIBinder.toNodeBinder[Rdf, T](ops, binder)

  implicit def TypedLiteralToLiteralBinder[T](implicit binder: TypedLiteralBinder[Rdf, T]): LiteralBinder[Rdf, T] = TypedLiteralBinder.toLiteralBinder[Rdf, T](ops, binder)

  implicit def LangLiteralToLiteralBinder[T](implicit binder: LangLiteralBinder[Rdf, T]): LiteralBinder[Rdf, T] = LangLiteralBinder.toLiteralBinder[Rdf, T](ops, binder)

  implicit def LiteralToNodeBinder[T](implicit binder: LiteralBinder[Rdf, T]): NodeBinder[Rdf, T] = LiteralBinder.toNodeBinder[Rdf, T](ops, binder)

  implicit def NodeToPointedGraphBinder[T](implicit binder: NodeBinder[Rdf, T]): PointedGraphBinder[Rdf, T] = NodeBinder.toPointedGraphBinder[Rdf, T](ops, binder)

  // the natural Binders

  implicit val PGBNode: PointedGraphBinder[Rdf, Rdf#Node] = NodeToPointedGraphBinder(NodeBinder.naturalBinder[Rdf])

  implicit val PGBUri: PointedGraphBinder[Rdf, Rdf#URI] = NodeToPointedGraphBinder(UriToNodeBinder(URIBinder.naturalBinder[Rdf]))

  implicit val PGBLiteral: PointedGraphBinder[Rdf, Rdf#Literal] = NodeToPointedGraphBinder(LiteralToNodeBinder(LiteralBinder.naturalBinder[Rdf]))

  implicit def pointedGraph2PointedGraphW(pointed: PointedGraph[Rdf]): PointedGraphW = new PointedGraphW(pointed)

  implicit def graph2GraphW(graph: Rdf#Graph): GraphW = new GraphW(graph)

}
