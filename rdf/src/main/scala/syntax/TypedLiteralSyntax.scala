package org.w3.banana.syntax

import org.w3.banana._

trait TypedLiteralSyntax {

  implicit def typedLiteralW[Rdf <: RDF](tl: Rdf#TypedLiteral) =
    new TypedLiteralW[Rdf](tl)

}

object TypedLiteralSyntax extends TypedLiteralSyntax

class TypedLiteralW[Rdf <: RDF](val tl: Rdf#TypedLiteral) extends AnyVal {

  def datatype(implicit ops: RDFOps[Rdf]): Rdf#URI = ops.fromTypedLiteral(tl)._2

}
