package org.w3.banana

/**
 * to execute Sparql queries
 */
trait SparqlEngine[Rdf <: RDF, M[_]] extends Any {

  def executeSelect(query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node]): M[Rdf#Solutions]

  def executeConstruct(query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node]): M[Rdf#Graph]

  def executeAsk(query: Rdf#AskQuery, bindings: Map[String, Rdf#Node]): M[Boolean]

  def executeSelect(query: Rdf#SelectQuery): M[Rdf#Solutions] = executeSelect(query, Map.empty)

  def executeConstruct(query: Rdf#ConstructQuery): M[Rdf#Graph] = executeConstruct(query, Map.empty)

  def executeAsk(query: Rdf#AskQuery): M[Boolean] = executeAsk(query, Map.empty)

}

object SparqlEngine {

  def apply[Rdf <: RDF, M[_]](store: RDFStore[Rdf, M]): SparqlEngine[Rdf, M] = new SparqlEngine[Rdf, M] {

    def executeSelect(query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node]): M[Rdf#Solutions] =
      store.execute(Command.select(query, bindings))

    def executeConstruct(query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node]): M[Rdf#Graph] =
      store.execute(Command.construct(query, bindings))

    def executeAsk(query: Rdf#AskQuery, bindings: Map[String, Rdf#Node]): M[Boolean] =
      store.execute(Command.ask(query, bindings))

  }

}

trait SparqlUpdateEngine[Rdf <: RDF, M[_]] //todo: implement a version for updates

