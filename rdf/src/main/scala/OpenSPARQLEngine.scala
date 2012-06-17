package org.w3.banana

import scalaz.{ Left3, Right3, Middle3, Either3 }

object OpenSPARQLEngine {

  def apply[Rdf <: RDF, Sparql <: SPARQL](implicit ops: SPARQLOperations[Rdf,Sparql], sparqlEngine: SPARQLEngine[Rdf,Sparql]): OpenSPARQLEngine[Rdf, Sparql] =
    new OpenSPARQLEngine[Rdf, Sparql](sparqlEngine)(ops)

}

class OpenSPARQLEngine[Rdf <: RDF, Sparql <: SPARQL](sparqlEngine: SPARQLEngine[Rdf,Sparql])(implicit ops: SPARQLOperations[Rdf,Sparql]) {

  import sparqlEngine._

  /**
   * This takes a generic query, and returns whatever type of object that query returns
   * @param query
   * @return an Iterable[Sparql#Row] if the query was a select query,
   *         an Rdf#Graph if the query was a Construct query
   *         a boolean if the query was an ASK query
   */
   def executeQuery(query: Sparql#Query) = ops.fold(query)(
    select => Left3(executeSelect(select)),
    construct => Middle3(executeConstruct(construct)),
    ask => Right3(executeAsk(ask))
  )

}
