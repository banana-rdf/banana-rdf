package org.w3.banana.syntax

import org.w3.banana._

trait GraphSyntax[Rdf <: RDF] {
this: RDFOperationsSyntax[Rdf] =>

  implicit def graphWrapper(graph: Rdf#Graph): GraphW = new GraphW(graph)

  class GraphW(graph: Rdf#Graph) {

    def toIterable: Iterable[Rdf#Triple] = ops.graphToIterable(graph)

    def union(otherGraph: Rdf#Graph): Rdf#Graph = ops.union(graph, otherGraph)

    def isIsomorphicWith(otherGraph: Rdf#Graph): Boolean = ops.isomorphism(graph, otherGraph)

  }

}
