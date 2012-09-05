package org.w3.banana

/**
 * to execute SPARQL queries
 */
trait SPARQLEngine[Rdf <: RDF, M[_]] {

  def executeSelect(query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node]): M[Rdf#Solutions]

  def executeConstruct(query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node]): M[Rdf#Graph]

  def executeAsk(query: Rdf#AskQuery, bindings: Map[String, Rdf#Node]): M[Boolean]

  def executeSelect(query: Rdf#SelectQuery): M[Rdf#Solutions] = executeSelect(query, Map.empty)

  def executeConstruct(query: Rdf#ConstructQuery): M[Rdf#Graph] = executeConstruct(query, Map.empty)

  def executeAsk(query: Rdf#AskQuery): M[Boolean] = executeAsk(query, Map.empty)

}

object SPARQLEngine {

  def apply[Rdf <: RDF, M[_]](store: RDFStore[Rdf, M]): SPARQLEngine[Rdf, M] = new SPARQLEngine[Rdf, M] {

    def executeSelect(query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node]): M[Rdf#Solutions] =
      store.execute(Command.select(query, bindings))

    def executeConstruct(query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node]): M[Rdf#Graph] =
      store.execute(Command.construct(query, bindings))

    def executeAsk(query: Rdf#AskQuery, bindings: Map[String, Rdf#Node]): M[Boolean] =
      store.execute(Command.ask(query, bindings))

  }

}
