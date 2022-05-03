package org.w3.banana.operations

import org.w3.banana.RDF

import scala.util.Try

trait SparqlEngine[Rdf <: RDF, M[_], A]:
   import RDF.*

   extension (a: A)
      def executeSelect(
          query: SelectQuery[Rdf],
          bindings: Map[String, Node[Rdf]] = Map.empty
      ): M[Solutions[Rdf]]

      def executeConstruct(
          query: ConstructQuery[Rdf],
          bindings: Map[String, Node[Rdf]] = Map.empty
      ): M[Graph[Rdf]]

      def executeAsk(
          query: AskQuery[Rdf],
          bindings: Map[String, Node[Rdf]] = Map.empty
      ): M[Boolean]
end SparqlEngine
