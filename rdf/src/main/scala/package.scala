package org.w3

package object banana {

  type TripleMatch[Rdf <: RDF] = (Rdf#NodeMatch, Rdf#NodeMatch, Rdf#NodeMatch)

}
