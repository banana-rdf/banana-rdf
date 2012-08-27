package org.w3.banana

trait TripleUtil[Rdf <: RDF] {

  def ops: RDFOperations[Rdf]

  implicit def tripleAsTripleMatch(triple: Rdf#Triple): TripleMatch[Rdf] =
    ops.fromTriple(triple).asInstanceOf[TripleMatch[Rdf]]

  implicit def triplesAsTripleMatches(triples: Iterable[Rdf#Triple]): Iterable[TripleMatch[Rdf]] =
    triples map { triple => tripleAsTripleMatch(triple) }

}
