package org.w3.banana.operations

import org.w3.banana.RDF

import scala.util.Try

trait Solution[Rdf <: RDF]:
   extension (solution: RDF.Solution[Rdf])
      def apply(variable: String): Try[RDF.Node[Rdf]]
      def variableNames: Set[String]
end Solution
