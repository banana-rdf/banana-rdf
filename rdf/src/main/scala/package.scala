package org.w3

import scalaz._
import scalaz.Id._

package object banana
extends BananaRDFWriterSelector
with BananaSPARQLSolutionWriterSelector {

  type TripleMatch[Rdf <: RDF] = (Rdf#NodeMatch, Rdf#NodeMatch, Rdf#NodeMatch)

  type BananaValidation[T] = Validation[BananaException, T]

  type BananaFuture[T] = util.FutureValidation[BananaException, T]

  type SyncRDFStore[Rdf <: RDF] = RDFStore[Rdf, Id]

  type AsyncRDFStore[Rdf <: RDF] = RDFStore[Rdf, BananaFuture]

  type SyncGraphStore[Rdf <: RDF] = GraphStore[Rdf, Id]

  type AsyncGraphStore[Rdf <: RDF] = GraphStore[Rdf, BananaFuture]

  type SyncSPARQLEngine[Rdf <: RDF] = SPARQLEngine[Rdf, Id]

  type AsyncSPARQLEngine[Rdf <: RDF] = SPARQLEngine[Rdf, BananaFuture]


  type SparqlSolutionsWriter[Rdf <: RDF, +T] = Writer[Rdf#Solutions, T]


  type RDFWriterSelector[Rdf <: RDF] = WriterSelector[Rdf#Graph]

  type SparqlSolutionsWriterSelector[Rdf <: RDF] = WriterSelector[Rdf#Solutions]

}
