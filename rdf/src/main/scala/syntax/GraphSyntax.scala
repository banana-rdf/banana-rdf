package org.w3.banana.syntax

import org.w3.banana._

class GraphSyntax[Rdf <: RDF](val graph: Rdf#Graph) extends AnyVal {

  def toIterable(implicit ops: RDFOps[Rdf]): Iterable[Rdf#Triple] = ops.graphToIterable(graph)

  def union(otherGraph: Rdf#Graph)(implicit ops: RDFOps[Rdf]): Rdf#Graph = ops.union(graph :: otherGraph :: Nil)

  def isIsomorphicWith(otherGraph: Rdf#Graph)(implicit ops: RDFOps[Rdf]): Boolean = ops.isomorphism(graph, otherGraph)

  /**
   * returns a copy of the graph where uri are transformed through urifunc
   */
  def copy(urifunc: Rdf#URI => Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Graph = {
    def nodefunc(node: Rdf#Node) = ops.foldNode(node)(urifunc, bn => bn, lit => lit)
    var triples = Set[Rdf#Triple]()
    val it = this.toIterable.iterator
    while (it.hasNext) {
      val ops.Triple(s, p, o) = it.next()
      // question: what about predicates?
      triples += ops.Triple(nodefunc(s), urifunc(p), nodefunc(o))
    }
    ops.makeGraph(triples)
  }

  def copy(implicit ops: RDFOps[Rdf]): Rdf#Graph = copy { uri => uri }

  def resolveAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Graph =
    copy { uri => URIHelper.resolve(baseUri, uri.toString)(ops) }

  def relativize(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Graph =
    copy { uri => URIHelper.relativize(baseUri, uri)(ops) }

  def getAllInstancesOf(clazz: Rdf#URI)(implicit ops: RDFOps[Rdf]): PointedGraphs[Rdf] = {
    val instances = ops.getSubjects(graph, ops.rdf("type"), clazz): Iterable[Rdf#Node]
    new PointedGraphs(instances, graph)
  }

}
