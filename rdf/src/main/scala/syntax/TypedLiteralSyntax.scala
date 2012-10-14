package org.w3.banana.syntax

import org.w3.banana._

class TypedLiteralSyntax[Rdf <: RDF](val tl: Rdf#TypedLiteral) extends AnyVal {

  def datatype(implicit ops: RDFOps[Rdf]): Rdf#URI = ops.fromTypedLiteral(tl)._2

}
