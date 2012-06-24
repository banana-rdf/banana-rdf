package org.w3.banana.syntax

import org.w3.banana._

trait TripleSyntax[Rdf <: RDF] {

  def ops: RDFOperations[Rdf]

  implicit def tripleWrapper(triple: Rdf#Triple): TripleW = new TripleW(triple)

  // TODO @deprecated("", "")
  implicit def tupleToTriple(tuple: (Rdf#Node, Rdf#URI, Rdf#Node)): Rdf#Triple =
    ops.makeTriple(tuple._1, tuple._2, tuple._3)

  class TripleW(triple: Rdf#Triple) {

    val (subject, predicate, objectt) = ops.fromTriple(triple)
    
  }

}
