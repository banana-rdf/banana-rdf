package org.w3.banana.syntax

import org.w3.banana._

trait TypedLiteralSyntax[Rdf <: RDF] {
this: RDFOperationsSyntax[Rdf] =>

  implicit def typedLiteralWrapper(tl: Rdf#TypedLiteral): TypedLiteralW = new TypedLiteralW(tl)

  class TypedLiteralW(tl: Rdf#TypedLiteral) {

    def datatype: Rdf#URI = ops.fromTypedLiteral(tl)._2

  }

}
