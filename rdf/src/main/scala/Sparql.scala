package org.w3.rdf

object Main {

  // val sparql = SPARQL("blah")

  // val foo: Iterable[Row] = Select("....................... SELECT ...").execute()

  // val bar: Rdf#Graph = Construct("CONSTRUCT ...").execute()

  // SPARQL("CONSTRUCT").executeCONSTRUCT()


}




trait Sparql[Rdf <: RDF] {

  type Select

  type Row

  def Select(query: String): Select

  def executeSelect(graph: Rdf#Graph, query: Select): Iterable[Row]

  def getNode(row: Row, v: String): Rdf#Node

}
