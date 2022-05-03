package org.w3.banana.operations

import org.w3.banana.{Prefix, RDF}

import scala.util.Try

trait Query[Rdf <: RDF]:
   extension (query: String)
      def asSelect(
          prefixes: Seq[Prefix[Rdf]]
      ): Try[RDF.SelectQuery[Rdf]]

      def asAsk(
          prefixes: Seq[Prefix[Rdf]]
      ): Try[RDF.AskQuery[Rdf]]

      def asConstruct(
          prefixes: Seq[Prefix[Rdf]]
      ): Try[RDF.ConstructQuery[Rdf]]

   protected def withPrefixes[Rdf <: RDF](query: String, prefixes: Seq[Prefix[Rdf]]): String =
     (prefixes.map(p => s"prefix ${p.prefixName}: <${p.prefixIri}>") :+ query).mkString("\n")
end Query
