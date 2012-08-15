package org.w3.banana

import scalaz._
import scalaz.Scalaz._
import scalaz.Validation._
import NodeBinder._
import org.w3.banana.util._

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
    with OptionBinder[Rdf]
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

    def as2[T1, T2](implicit b1: PointedGraphBinder[Rdf, T1], b2: PointedGraphBinder[Rdf, T2]): Validation[BananaException, (T1, T2)] =
      (b1.fromPointedGraph(pointed) |@| b2.fromPointedGraph(pointed))(Tuple2.apply)

    def a(clazz: Rdf#URI): PointedGraph[Rdf] = {
      val newGraph = graph union Graph(Triple(pointer, rdf("type"), clazz))
      PointedGraph(pointer, newGraph)
    }

    def --(p: Rdf#URI): PointedGraphPredicate = new PointedGraphPredicate(pointed, p)

    def -<-(p: Rdf#URI): PredicatePointedGraph = new PredicatePointedGraph(p, pointed)

    def /(p: Rdf#URI): PointedGraphs = {
      val nodes = getObjects(graph, pointer, p)
      new PointedGraphs(nodes, graph)
    }

    def pointer: Rdf#Node = pointed.pointer

    def predicates = getPredicates(graph, pointer)

    def isA(clazz: Rdf#URI): Boolean = {
      def isAIfNodeOrBNode = {
        val classes = getObjects(graph, pointer, rdf("type"))
        classes exists { _ == clazz }
      }
      pointer.fold(
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

    def exactlyOneAs[T](implicit binder: PointedGraphBinder[Rdf, T]): Validation[BananaException, T] =
      exactlyOnePointedGraph flatMap (_.as[T])

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
      takeOnePointedGraph flatMap (_.as[T])

    def as2[T1, T2](implicit b1: PointedGraphBinder[Rdf, T1], b2: PointedGraphBinder[Rdf, T2]): Validation[BananaException, (T1, T2)] =
      takeOnePointedGraph flatMap { _.as2[T1, T2] }

    /**
     * returns optionally a T (though the implicit binder) if it is available.
     * that's a good way to know if a particular rdf object was there
     *
     * note: this is very different from as[Option[T]], which is an encoding of an Option in RDF
     */
    def asOption[T](implicit binder: PointedGraphBinder[Rdf, T]): Validation[BananaException, Option[T]] = headOption match {
      case None => Success(None)
      case Some(pointed) => pointed.as[T] map (Some(_))
    }

    def asOption2[T1, T2](implicit b1: PointedGraphBinder[Rdf, T1], b2: PointedGraphBinder[Rdf, T2]): Validation[BananaException, Option[(T1, T2)]] = headOption match {
      case None => Success(None)
      case Some(pointed) => (pointed.as[T1] |@| pointed.as[T2])(Tuple2.apply) map { Some(_) }
    }

    /**
     * sees the nodes for this PointedGraphs as an iterator, after they were bound successfully to
     * a T thought an implicit PointedGraphBinder.
     * it is a success only if all the bindings were successful themselves
     *
     * note: this is very different from as[List[T]], which is an encoding of a List in RDF
     */
    def asSet[T](implicit binder: PointedGraphBinder[Rdf, T]): Validation[BananaException, Set[T]] =
      this.iterator.toList.map(_.as[T]).sequence[BananaValidation, T].map(_.toSet)

    def asSet2[T1, T2](implicit b1: PointedGraphBinder[Rdf, T1], b2: PointedGraphBinder[Rdf, T2]): Validation[BananaException, Set[(T1, T2)]] =
      this.iterator.toList.map { pg => (pg.as[T1] |@| pg.as[T2])(Tuple2.apply) }.sequence[BananaValidation, (T1, T2)].map(_.toSet)

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

  implicit val PGBPointedGraphBinder: PointedGraphBinder[Rdf, PointedGraph[Rdf]] =
    new PointedGraphBinder[Rdf, PointedGraph[Rdf]] {
      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, PointedGraph[Rdf]] = Success(pointed)
      def toPointedGraph(t: PointedGraph[Rdf]): PointedGraph[Rdf] = t
    }

  implicit val PGBNode: PointedGraphBinder[Rdf, Rdf#Node] = NodeToPointedGraphBinder(NodeBinder.naturalBinder[Rdf])

  implicit val PGBUri: PointedGraphBinder[Rdf, Rdf#URI] = NodeToPointedGraphBinder(UriToNodeBinder(URIBinder.naturalBinder[Rdf]))

  implicit val PGBLiteral: PointedGraphBinder[Rdf, Rdf#Literal] = NodeToPointedGraphBinder(LiteralToNodeBinder(LiteralBinder.naturalBinder[Rdf]))

  implicit def pointedGraph2PointedGraphW(pointed: PointedGraph[Rdf]): PointedGraphW = new PointedGraphW(pointed)

  implicit def graph2GraphW(graph: Rdf#Graph): GraphW = new GraphW(graph)

}
