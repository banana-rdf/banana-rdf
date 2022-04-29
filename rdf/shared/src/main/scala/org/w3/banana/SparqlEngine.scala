package org.w3.banana

import scala.util.Try

trait SparqlEngine[Rdf <: RDF, M[_], A]:
   import RDF.*

   extension (solutions: Solutions[Rdf])
     def iterator: Iterator[Solution[Rdf]]

   extension (solution: Solution[Rdf])
      def apply(variable: String): Try[Node[Rdf]]
      def variableNames: Set[String]

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

   extension (query: String)
      def asSelect(
          prefixes: Seq[Prefix[Rdf]]
      ): Try[SelectQuery[Rdf]]

      def asAsk(
          prefixes: Seq[Prefix[Rdf]]
      ): Try[AskQuery[Rdf]]

      def asConstruct(
          prefixes: Seq[Prefix[Rdf]]
      ): Try[ConstructQuery[Rdf]]

   protected def withPrefixes[Rdf <: RDF](query: String, prefixes: Seq[Prefix[Rdf]]): String =
     (prefixes.map(p => s"prefix ${p.prefixName}: <${p.prefixIri}>") :+ query).mkString("\n")
