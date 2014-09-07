package org.w3.banana.iso

import org.w3.banana.RDF

import scala.collection.immutable
import scala.collection.immutable.ListMap
import scala.util.{ Success, Failure, Try }
import scalaz.EphemeralStream

/**
 * Trait for implementations of BNode Mapping Generators.
 * A good implementation should never leave out a correct mapping.
 * Better implementations should return less mappings.
 * (better implementations may also be less easy to understand.)
 */
trait MappingGenerator[Rdf <: RDF] {
  /**
   * generate a list of possible bnode mappings.
   * A very stupid implementation would for each bnode in g1 suggest every bnode in g2 as a mapping.
   * Better implementations will filter the mappings by applying classification algorithm
   * on the nodes by relating them to other edges
   * @param g1 first graph
   * @param g2 second graph
   * @return a ListMap mapping BNode from graph g1 to a smaller set of Bnodes from graph g2 which
   *         they should correspond to. The list map keeps an order of the nodes so as to put the nodes
   *         with the least options first.
   */
  def bnodeMappings(g1: Rdf#Graph, g2: Rdf#Graph): Try[immutable.ListMap[Rdf#BNode, immutable.Set[Rdf#BNode]]]

}

object MappingGenerator {
  /**
   * calculate the  size of the  tree of possibilities from the given mapping
   * @param maps
   * @tparam T
   */
  def complexity[T](maps: Try[Map[T, Set[T]]]) = {
    maps match {
      case x: Failure[T] => 0
      case Success(m) => m.values.foldLeft(1)((n, v) => n * v.size)
    }
  }

  /** a function to take a list of layers, and turn it into a lazy stream of branches */
  def branches[T](layers: List[List[T]]): EphemeralStream[List[T]] = {
    layers.foldLeft(EphemeralStream(List[T]())) {
      case (streamOfBranches, layer) =>
        for (
          mapping <- EphemeralStream(layer: _*);
          branch <- streamOfBranches
        ) yield {
          mapping :: branch
        }
    }

  }

  /**
   * transform a ListMap - order is important - into a List of layers of maps
   * @param nodeMapping an ordered Map
   * @tparam T
   * @return a list of layers of maps.
   */
  def treeLevels[T](nodeMapping: ListMap[T, Set[T]]): List[List[(T, T)]] =
    nodeMapping.toList.map { case (key, values) => values.toList.map(v => (key, v)) }

}