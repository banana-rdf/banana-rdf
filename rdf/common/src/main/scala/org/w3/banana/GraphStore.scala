package org.w3.banana

/**
 * A typeclass for graph stores supporting manipulations of [RDF Datasets](http://www.w3.org/TR/rdf11-concepts/#h2_section-dataset).
 *
 * With this interface, one can only manipulate named graphs (no
 * default graph) through their names (only as URIs, no bnode).
 */
trait GraphStore[Rdf <: RDF, M[+_], A] {

  /**
   * To the graph at `uri`, appends the content of `graph`. If there was
   * no previous graph, this would create it.
   */
  def appendToGraph(a: A, uri: Rdf#URI, graph: Rdf#Graph): M[Unit]

  /**
   * To the graph at `uri`, removes the matching triples
   */
  def removeTriples(a: A, uri: Rdf#URI, triples: Iterable[TripleMatch[Rdf]]): M[Unit]

  /** Gets the graph at `uri`. */
  def getGraph(a: A, uri: Rdf#URI): M[Rdf#Graph]

  /** Removes the graph at `uri`. */
  def removeGraph(a: A, uri: Rdf#URI): M[Unit]

  val graphStoreSyntax = new syntax.GraphStoreSyntax[Rdf, M, A]

}
