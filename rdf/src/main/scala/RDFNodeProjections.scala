package org.w3.banana

import scalaz._
import scalaz.Scalaz._
import scalaz.Validation._

object RDFNodeProjections {
  def apply[Rdf <: RDF](ops: RDFOperations[Rdf]): RDFNodeProjections[Rdf] =
    new RDFNodeProjections[Rdf](ops)
}

class RDFNodeProjections[Rdf <: RDF](ops: RDFOperations[Rdf]) {

  import ops._

  private val commonLiteralBinders = CommonLiteralBinders(ops)
  import commonLiteralBinders._

  class NodeW(node: Rdf#Node) {

    def asLiteral: Validation[BananaException, Rdf#Literal] =
      Node.fold(node)(
        iri => Failure(FailedConversion(node.toString + " is not a literal")),
        bnode => Failure(FailedConversion(node.toString + " is not a literal")),
        literal => Success(literal)
      )

    def as[T](implicit binder: LiteralBinder[Rdf, T]): Validation[BananaException, T] =
      asLiteral flatMap binder.fromLiteral
      
    def asString: Validation[BananaException, String] = as[String]
    
    def asInt: Validation[BananaException, Int] = as[Int]
    
    def asDouble: Validation[BananaException, Double] = as[Double]

    def asUri: Validation[BananaException, Rdf#URI] = {
      Node.fold(node)(
        iri => Success(iri),
        bnode => Failure(FailedConversion("asUri: " + node.toString + " is not a URI")),
        literal => Failure(FailedConversion("asUri: " + node.toString + " is not a URI"))
      )
    }

  }

  implicit def node2NodeW(node: Rdf#Node): NodeW = new NodeW(node)

}
