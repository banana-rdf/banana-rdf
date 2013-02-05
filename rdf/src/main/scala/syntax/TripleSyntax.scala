package org.w3.banana.syntax

import org.w3.banana._

trait TripleSyntax {

  def tripleSyntax[Rdf <: RDF](triple: Rdf#Triple) = new TripleW(triple)

}

object TripleSyntax extends TripleSyntax

class TripleW[Rdf <: RDF](val triple: Rdf#Triple) extends AnyVal {

  def resolveAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Triple = {
    val (s, p, o) = ops.fromTriple(triple)
    ops.makeTriple(s.resolveAgainst(baseUri), p, o.resolveAgainst(baseUri))
  }

  def relativize(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Triple = {
    val (s, p, o) = ops.fromTriple(triple)
    ops.makeTriple(s.relativize(baseUri), p, o.relativize(baseUri))
  }

  def relativizeAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Triple = {
    val (s, p, o) = ops.fromTriple(triple)
    ops.makeTriple(s.relativizeAgainst(baseUri), p, o.relativizeAgainst(baseUri))
  }

}
