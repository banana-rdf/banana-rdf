package org.w3.banana

import scala.util.Try

trait SparqlEngine[Rdf <: RDF, M[_], A]:
   import RDF.*

   extension (solutions: RDF.Solutions[Rdf])
     def iterator: Iterator[RDF.Solution[Rdf]]

   extension (solution: RDF.Solution[Rdf])
     def apply(variable: String): Try[RDF.Node[Rdf]]

   extension (a: A)
      def executeSelect(
          query: RDF.SelectQuery[Rdf],
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
      ): Try[RDF.SelectQuery[Rdf]]

      def asAsk(
          prefixes: Seq[Prefix[Rdf]]
      ): Try[RDF.SelectQuery[Rdf]]

      def asConstruct(
          prefixes: Seq[Prefix[Rdf]]
      ): Try[RDF.SelectQuery[Rdf]]

   protected def withPrefixes[Rdf <: RDF](query: String, prefixes: Seq[Prefix[Rdf]]): String =
     (prefixes.map(p => s"prefix ${p.prefixName}: <${p.prefixIri}>") :+ query).mkString("\n")
