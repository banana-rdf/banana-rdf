package org.w3.banana.syntax

import org.w3.banana._

trait LiteralSyntax[Rdf <: RDF] { self: RDFSyntax[Rdf] =>

  implicit def literalW(literal: Rdf#Literal) =
    new LiteralW[Rdf](literal)

}

class LiteralW[Rdf <: RDF](val literal: Rdf#Literal) extends AnyVal {

  def lexicalForm(implicit ops: RDFOps[Rdf]): String = ops.fromLiteral(literal)._1

  def datatype(implicit ops: RDFOps[Rdf]): Rdf#URI = ops.fromLiteral(literal)._2

  def lang(implicit ops: RDFOps[Rdf]): Option[Rdf#Lang] = ops.fromLiteral(literal)._3

}
