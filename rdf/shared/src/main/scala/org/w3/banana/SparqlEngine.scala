package org.w3.banana

trait SparqlEngine[Rdf <: RDF, M[_], A]:
   import RDF.*

   extension (a: A)
      def executeSelect(
          query: RDF.SelectQuery[Rdf],
          bindings: Map[String, Node[Rdf]]
      ): M[Solutions[Rdf]]

      def executeConstruct(
          query: ConstructQuery[Rdf],
          bindings: Map[String, Node[Rdf]]
      ): M[Graph[Rdf]]

      def executeAsk(
          query: AskQuery[Rdf],
          bindings: Map[String, Node[Rdf]]
      ): M[Boolean]
