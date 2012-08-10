package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode, _ }
import scala.collection.JavaConverters._

sealed trait ImmutableJenaGraph {

  def jenaGraph: JenaGraph

  def toIterable: Iterable[Jena#Triple]

}

case object EmptyGraph extends ImmutableJenaGraph {

  val jenaGraph: JenaGraph = Factory.createDefaultGraph

  def toIterable: Iterable[Jena#Triple] = List.empty

}

/**
 * a simple wrapper for a plain-old Jena graph
 */
case class BareJenaGraph(graph: JenaGraph) extends ImmutableJenaGraph {

  val jenaGraph: JenaGraph = graph

  def toIterable: Iterable[Jena#Triple] = graph.find(JenaNode.ANY, JenaNode.ANY, JenaNode.ANY).asScala.toIterable

}

/**
 * used as the result of a union between graphs
 *
 * the idea is that we don't compute the result of the union until we really need it
 *
 * note: this must verify the following invariant: the list of graph must be flat.
 * ie. the union function must maintain this invariant, and that's the *only* place
 * where this class should be used
 */
case class UnionGraphs(graphs: List[ImmutableJenaGraph]) extends ImmutableJenaGraph {

  lazy val jenaGraph: JenaGraph = {
    val graph: JenaGraph = Factory.createDefaultGraph
    graphs foreach {
      case EmptyGraph => ()
      case BareJenaGraph(graph) => {
        val it = graph.find(JenaNode.ANY, JenaNode.ANY, JenaNode.ANY)
        while (it.hasNext) { graph.add(it.next()) }
      }
      case GraphAsIterable(iterable) => {
        val it = iterable.iterator
        while (it.hasNext) { graph.add(it.next()) }
      }
      case UnionGraphs(graphs) => sys.error("you should not construct a union of unions...")
    }
    graph
  }

  def toIterable: Iterable[Jena#Triple] = jenaGraph.find(JenaNode.ANY, JenaNode.ANY, JenaNode.ANY).asScala.toIterable

}

case class GraphAsIterable(triples: Iterable[Jena#Triple]) extends ImmutableJenaGraph {

  lazy val jenaGraph: JenaGraph = {
    val graph: JenaGraph = Factory.createDefaultGraph
    val it = triples.iterator
    while (it.hasNext) { graph.add(it.next()) }
    graph
  }

  def toIterable: Iterable[Jena#Triple] = jenaGraph.find(JenaNode.ANY, JenaNode.ANY, JenaNode.ANY).asScala.toIterable

}
