package org.w3.banana

import scalaz.{ Left3, Right3, Middle3, Either3 }

object OpenSPARQLEngine {

  def apply[Rdf <: RDF](implicit ops: SPARQLOperations[Rdf], sparqlEngine: SPARQLEngine[Rdf]): OpenSPARQLEngine[Rdf] =
    new OpenSPARQLEngine[Rdf](sparqlEngine)(ops)

}

class OpenSPARQLEngine[Rdf <: RDF](sparqlEngine: SPARQLEngine[Rdf])(implicit ops: SPARQLOperations[Rdf]) {

  import sparqlEngine._

  /**
   * This takes a generic query, and returns whatever type of object that query returns
   * @param query
   * @return an Iterable[Rdf#Row] if the query was a select query,
   *         an Rdf#Graph if the query was a Construct query
   *         a boolean if the query was an ASK query
   */
  def executeQuery(query: Rdf#Query) = ops.fold(query)(
    select => Left3(executeSelect(select)),
    construct => Middle3(executeConstruct(construct)),
    ask => Right3(executeAsk(ask))
  )

}
