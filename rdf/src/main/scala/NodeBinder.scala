package org.w3.banana

import scalaz._

trait NodeBinder[Rdf <: RDF, T] {
  def fromNode(node: Rdf#Node): Validation[BananaException, T]
  def toNode(t: T): Rdf#Node
}

object NodeBinder {

  def toPointedGraphBinder[Rdf <: RDF, T](implicit ops: RDFOperations[Rdf], binder: NodeBinder[Rdf, T]): PointedGraphBinder[Rdf, T] =
    new PointedGraphBinder[Rdf, T] {

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] =
        binder.fromNode(pointed.node)

      def toPointedGraph(t: T): PointedGraph[Rdf] = PointedGraph(binder.toNode(t))
    }

  def asLiteral[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOperations[Rdf]): Validation[BananaException, Rdf#Literal] =
    ops.Node.fold(node)(
      iri => Failure(FailedConversion(node + " is an IRI while I was expecting a Literal")),
      bnode => Failure(FailedConversion(node + " is a BNode while I was expecting a Literal")),
      literal => Success(literal)
    )
 
  def asTypedLiteral[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOperations[Rdf]): Validation[BananaException, Rdf#TypedLiteral] =
    asLiteral(node) flatMap { literal =>
      ops.Literal.fold(literal)(
        typedLiteral => Success(typedLiteral),
        langLiteral => Failure(FailedConversion(langLiteral + " is a LangLiteral while I was expecting a TypedLiteral"))
    )
  }

  def asLangLiteral[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOperations[Rdf]): Validation[BananaException, Rdf#LangLiteral] =
    asLiteral(node) flatMap { literal =>
      ops.Literal.fold(literal)(
        typedLiteral => Failure(FailedConversion(typedLiteral + " is a TypedLiteral while I was expecting a langLiteral")),
        langLiteral => Success(langLiteral)
    )
  }
 

}
