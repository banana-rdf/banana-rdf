package org.w3.banana.syntax

import org.w3.banana._

trait TripleSyntax[Rdf <: RDF] { self: RDFSyntax[Rdf] =>

  implicit def tripleSyntax(triple: Rdf#Triple) = new TripleW[Rdf](triple)

}

class TripleW[Rdf <: RDF](val triple: Rdf#Triple) extends AnyVal {

  def subject(implicit ops: RDFOps[Rdf]): Rdf#Node = {
    val (s, _, _) = ops.fromTriple(triple)
    s
  }

  def predicate(implicit ops: RDFOps[Rdf]): Rdf#URI = {
    val (_, p, _) = ops.fromTriple(triple)
    p
  }

  def objectt(implicit ops: RDFOps[Rdf]): Rdf#Node = {
    val (_, _, o) = ops.fromTriple(triple)
    o
  }


  def resolveAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Triple = {
    import ops._
    val (s, p, o) = ops.fromTriple(triple)
    ops.makeTriple(s.resolveAgainst(baseUri), p, o.resolveAgainst(baseUri))
  }

  def relativize(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Triple = {
    import ops._
    val (s, p, o) = ops.fromTriple(triple)
    ops.makeTriple(s.relativize(baseUri), p, o.relativize(baseUri))
  }

  def relativizeAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Triple = {
    import ops._
    val (s, p, o) = ops.fromTriple(triple)
    ops.makeTriple(s.relativizeAgainst(baseUri), p, o.relativizeAgainst(baseUri))
  }

}
