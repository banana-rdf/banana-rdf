package org.w3.banana.ldpatch

import org.w3.banana._

object LDPatch {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]): LDPatch[Rdf] = new LDPatch[Rdf]
}

class LDPatch[Rdf <: RDF](implicit val ops: RDFOps[Rdf]) extends Grammar[Rdf] with Semantics[Rdf]
