package org.w3.banana.syntax

import org.w3.banana._

trait GraphSyntax[Rdf <: RDF] {
  this: RDFOpsSyntax[Rdf] =>

  implicit def graphWrapper(graph: Rdf#Graph): GraphW = new GraphW(graph)

  class GraphW(graph: Rdf#Graph) {

    def toIterable: Iterable[Rdf#Triple] = ops.graphToIterable(graph)

    def union(otherGraph: Rdf#Graph): Rdf#Graph = ops.union(graph :: otherGraph :: Nil)

    def isIsomorphicWith(otherGraph: Rdf#Graph): Boolean = ops.isomorphism(graph, otherGraph)

    /**
     * returns a copy of the graph where uri are transformed through urifunc
     */
    def copy(urifunc: Rdf#URI => Rdf#URI): Rdf#Graph = {
      def nodefunc(node: Rdf#Node) = ops.foldNode(node)(urifunc, bn => bn, lit => lit)
      var triples = Set[Rdf#Triple]()
      val it = this.toIterable.iterator
      while (it.hasNext) {
        val Triple(s, p, o) = it.next()
        // question: what about predicates?
        triples += Triple(nodefunc(s), urifunc(p), nodefunc(o))
      }
      ops.makeGraph(triples)
    }

    def copy: Rdf#Graph = copy { uri => uri }

    def resolveAgainst(baseUri: Rdf#URI): Rdf#Graph =
      copy { uri => URIHelper.resolve(baseUri, uri.toString)(ops) }

    def relativize(baseUri: Rdf#URI): Rdf#Graph =
      copy { uri => URIHelper.relativize(baseUri, uri)(ops) }

  }

}
