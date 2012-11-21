package org.w3.banana.syntax

import org.w3.banana._

class TripleMatchSyntax[Rdf <: RDF](val tripleMatch: TripleMatch[Rdf]) extends AnyVal {

  import tripleMatch.{ _1 => sMatch, _2 => pMatch, _3 => oMatch }

  def resolveAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): TripleMatch[Rdf] = {
    import ops.nodeMatchSyntax
    val s = sMatch.resolveAgainst(baseUri)
    val o = oMatch.resolveAgainst(baseUri)
    (s, pMatch, o)
  }

  def relativize(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): TripleMatch[Rdf] = {
    import ops.nodeMatchSyntax
    val s = sMatch.relativize(baseUri)
    val o = oMatch.relativize(baseUri)
    (s, pMatch, o)
  }

}
