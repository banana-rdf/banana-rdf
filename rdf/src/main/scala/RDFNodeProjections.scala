package org.w3.banana

import scalaz._
import scalaz.Scalaz._
import scalaz.Validation._
import NodeBinder._

object RDFNodeProjections {
  def apply[Rdf <: RDF]()(implicit ops: RDFOperations[Rdf]): RDFNodeProjections[Rdf] =
    new RDFNodeProjections[Rdf]()(ops)
}

class RDFNodeProjections[Rdf <: RDF]()(implicit ops: RDFOperations[Rdf]) {

  import ops._

  private val commonBinders = CommonBinders()(ops)
  import commonBinders._

  class NodeW(node: Rdf#Node) {

    def as[T](implicit binder: NodeBinder[Rdf, T]): Validation[BananaException, T] =
      asLiteral(node) flatMap binder.fromNode
      
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
