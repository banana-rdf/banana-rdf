package org.w3.banana.syntax

import org.w3.banana._

final class GraphStoreSyntax[Rdf <: RDF, A] {

  implicit def graphStoreW(a: A) = new GraphStoreW[Rdf, A](a)

}

final class GraphStoreW[Rdf <: RDF, A](val a: A) extends AnyVal {

  def appendToGraph(uri: Rdf#URI, graph: Rdf#Graph)(implicit graphStore: GraphStore[Rdf, A]) =
    graphStore.appendToGraph(a, uri, graph)

  def removeTriples(uri: Rdf#URI, triples: Iterable[TripleMatch[Rdf]])(implicit graphStore: GraphStore[Rdf, A]) =
    graphStore.removeTriples(a, uri, triples)

  def getGraph(uri: Rdf#URI)(implicit graphStore: GraphStore[Rdf, A]) =
    graphStore.getGraph(a, uri)

  def removeGraph(uri: Rdf#URI)(implicit graphStore: GraphStore[Rdf, A]) =
    graphStore.removeGraph(a, uri)

}
