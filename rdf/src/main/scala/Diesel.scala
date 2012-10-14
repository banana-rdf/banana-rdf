package org.w3.banana

import scala.util._

object Diesel {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]): Diesel[Rdf] = new Diesel
}

class Diesel[Rdf <: RDF](implicit val ops: RDFOps[Rdf])
    extends CommonBinders[Rdf]
    with ListBinder[Rdf]
    with OptionBinder[Rdf]
    with TupleBinder[Rdf]
    with MapBinder[Rdf]
    with EitherBinder[Rdf]
    with RecordBinder[Rdf] {

  import ops._

  /* looks like implicit resolution does not work if Rdf is not fixed...  */

  implicit def UriToNodeBinder[T](implicit binder: URIBinder[Rdf, T]): NodeBinder[Rdf, T] = URIBinder.toNodeBinder[Rdf, T](ops, binder)

  implicit def TypedLiteralToLiteralBinder[T](implicit binder: TypedLiteralBinder[Rdf, T]): LiteralBinder[Rdf, T] = TypedLiteralBinder.toLiteralBinder[Rdf, T](ops, binder)

  implicit def LangLiteralToLiteralBinder[T](implicit binder: LangLiteralBinder[Rdf, T]): LiteralBinder[Rdf, T] = LangLiteralBinder.toLiteralBinder[Rdf, T](ops, binder)

  implicit def LiteralToNodeBinder[T](implicit binder: LiteralBinder[Rdf, T]): NodeBinder[Rdf, T] = LiteralBinder.toNodeBinder[Rdf, T](ops, binder)

  implicit def NodeToPointedGraphBinder[T](implicit binder: NodeBinder[Rdf, T]): PointedGraphBinder[Rdf, T] = NodeBinder.toPointedGraphBinder[Rdf, T](ops, binder)

  // the natural Binders

  implicit val PGBPointedGraphBinder: PointedGraphBinder[Rdf, PointedGraph[Rdf]] =
    new PointedGraphBinder[Rdf, PointedGraph[Rdf]] {
      def fromPointedGraph(pointed: PointedGraph[Rdf]): Try[PointedGraph[Rdf]] = Success(pointed)
      def toPointedGraph(t: PointedGraph[Rdf]): PointedGraph[Rdf] = t
    }

  implicit val PGBNode: PointedGraphBinder[Rdf, Rdf#Node] = NodeToPointedGraphBinder(NodeBinder.naturalBinder[Rdf])

  implicit val PGBLiteral: PointedGraphBinder[Rdf, Rdf#Literal] = NodeToPointedGraphBinder(LiteralToNodeBinder(LiteralBinder.naturalBinder[Rdf]))

}
