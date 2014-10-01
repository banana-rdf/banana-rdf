package org.w3.banana.syntax

import org.w3.banana._

trait GraphSyntax[Rdf <: RDF] { self: RDFSyntax[Rdf] =>

  implicit def graphW(graph: Rdf#Graph) = new GraphW[Rdf](graph)

}

class GraphW[Rdf <: RDF](val graph: Rdf#Graph) extends AnyVal {

  def triples(implicit ops: RDFOps[Rdf]): Iterable[Rdf#Triple] = ops.getTriples(graph)

  def union(otherGraph: Rdf#Graph)(implicit ops: RDFOps[Rdf]): Rdf#Graph = ops.union(graph :: otherGraph :: Nil)

  def +(triple: Rdf#Triple)(implicit ops: RDFOps[Rdf]) = ops.union(Seq(graph, ops.Graph(Set(triple))))

  def diff(other: Rdf#Graph)(implicit ops: RDFOps[Rdf]): Rdf#Graph = ops.diff(graph, other)

  def isIsomorphicWith(otherGraph: Rdf#Graph)(implicit ops: RDFOps[Rdf]): Boolean = ops.isomorphism(graph, otherGraph)

  def contains(triple: Rdf#Triple)(implicit ops: RDFOps[Rdf]): Boolean = {
    val (sub, rel, obj) = ops.fromTriple(triple)
    import ops.toConcreteNodeMatch
    ops.find(graph, sub, rel, obj).hasNext
  }

  /**
   * returns a copy of the graph where uri are transformed through urifunc
   */
  def copy(urifunc: Rdf#URI => Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#Graph = {
    def nodefunc(node: Rdf#Node) = ops.foldNode(node)(urifunc, bn => bn, lit => lit)
    @annotation.tailrec
    def loop(it: Iterator[Rdf#Triple], triples: Set[Rdf#Triple]): Set[Rdf#Triple] = {
      if (it.hasNext) {
        val ops.Triple(s, p, o) = it.next()
        loop(it, triples + ops.Triple(nodefunc(s), urifunc(p), nodefunc(o)))
      } else triples
    }
    val triples = loop(this.triples.iterator, Set.empty)
    ops.makeGraph(triples)
  }

  def copy(implicit ops: RDFOps[Rdf]): Rdf#Graph = copy { uri => uri }

  def resolveAgainst(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf], uriOps: URIOps[Rdf]): Rdf#Graph =
    copy { uri => uriOps.resolve(baseUri, uri) }

  def relativize(baseUri: Rdf#URI)(implicit ops: RDFOps[Rdf], uriOps: URIOps[Rdf]): Rdf#Graph =
    copy { uri => uriOps.relativize(baseUri, uri) }

  def getAllInstancesOf(clazz: Rdf#URI)(implicit ops: RDFOps[Rdf]): PointedGraphs[Rdf] = {
    val instances = ops.getSubjects(graph, ops.rdf("type"), clazz): Iterable[Rdf#Node]
    new PointedGraphs(instances, graph)
  }

  def size(implicit ops: RDFOps[Rdf]): Int = ops.graphSize(graph)

}
