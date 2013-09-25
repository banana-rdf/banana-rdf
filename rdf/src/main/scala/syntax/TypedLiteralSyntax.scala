package org.w3.banana.syntax

import org.w3.banana._

trait TypedLiteralSyntax[Rdf <: RDF] { self: Syntax[Rdf] =>

  implicit def typedLiteralW(tl: Rdf#TypedLiteral) =
    new TypedLiteralW[Rdf](tl)

}

class TypedLiteralW[Rdf <: RDF](val tl: Rdf#TypedLiteral) extends AnyVal {

  def datatype(implicit ops: RDFOps[Rdf]): Rdf#URI = ops.fromTypedLiteral(tl)._2

}
