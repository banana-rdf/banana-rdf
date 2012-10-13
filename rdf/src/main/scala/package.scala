package org.w3

import scalaz._
import scalaz.Id._

package object banana
extends BananaRDFWriterSelector
with BananaSparqlSolutionWriterSelector {

  type TripleMatch[Rdf <: RDF] = (Rdf#NodeMatch, Rdf#NodeMatch, Rdf#NodeMatch)

  type SparqlSolutionsWriter[Rdf <: RDF, +T] = Writer[Rdf#Solutions, T]

  type RDFWriterSelector[Rdf <: RDF] = WriterSelector[Rdf#Graph]

  type SparqlSolutionsWriterSelector[Rdf <: RDF] = WriterSelector[Rdf#Solutions]

}
