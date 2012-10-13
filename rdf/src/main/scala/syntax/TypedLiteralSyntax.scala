package org.w3.banana.syntax

import org.w3.banana._

class TypedLiteralSyntax[Rdf <: RDF](tl: Rdf#TypedLiteral)(implicit ops: RDFOps[Rdf]) {

  def datatype: Rdf#URI = ops.fromTypedLiteral(tl)._2

}
