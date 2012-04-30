package org.w3.rdf

object Main {

  // val sparql = SPARQL("blah")

  // val foo: Iterable[Row] = Select("....................... SELECT ...").execute()

  // val bar: Rdf#Graph = Construct("CONSTRUCT ...").execute()

  // SPARQL("CONSTRUCT").executeCONSTRUCT()


}




trait Sparql[Rdf <: RDF] {

  type SelectQuery

  type ConstructQuery

  type AskQuery

  type Row

  def SelectQuery(query: String): SelectQuery

  def executeSelectQuery(graph: Rdf#Graph, query: SelectQuery): Iterable[Row]

  def getNode(row: Row, v: String): Rdf#Node

  def ConstructQuery(query: String): ConstructQuery

  def executeConstructQuery(graph: Rdf#Graph, query: ConstructQuery): Rdf#Graph

  def AskQuery(query: String): AskQuery

  def executeAskQuery(graph: Rdf#Graph, query: AskQuery): Boolean

}
