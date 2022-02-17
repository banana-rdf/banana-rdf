/*
 *  Copyright (c) 2012 , 2021 W3C Members
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  This program and the accompanying materials are made available under
 *  the W3C Software Notice and Document License (2015-05-13) which is available at
 *  https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document.
 *
 *  SPDX-License-Identifier: W3C-20150513
 */

package org.w3.banana.isomorphism

import org.w3.banana.RDF
import org.w3.banana.RDF.*

import scala.collection.immutable
import scala.collection.immutable.ListMap
import scala.util.{Failure, Success, Try}

/** Trait for implementations of BNode Mapping Generators. A good implementation should never leave
  * out a correct mapping. Better implementations should return less mappings. (better
  * implementations may also be less easy to understand.)
  */
trait MappingGenerator[Rdf <: RDF]:
   /** generate a list of possible bnode mappings. A very stupid implementation would for each bnode
     * in g1 suggest every bnode in g2 as a mapping. Better implementations will filter the mappings
     * by applying classification algorithm on the nodes by relating them to other edges
     *
     * @param g1
     *   first graph
     * @param g2
     *   second graph
     * @return
     *   a ListMap mapping BNode from graph g1 to a smaller set of Bnodes from graph g2 which they
     *   should correspond to. The list map keeps an order of the nodes so as to put the nodes with
     *   the least options first.
     */
   def bnodeMappings(
       g1: Graph[Rdf],
       g2: Graph[Rdf]
   ): Try[immutable.ListMap[BNode[Rdf], immutable.Set[BNode[Rdf]]]]

object MappingGenerator:
   /** calculate the size of the tree of possibilities from the given mapping
     */
   def complexity[T](maps: Try[Map[T, Set[T]]]) =
     maps match
        case Failure(_) => 0
        case Success(m) => m.values.foldLeft(1)((n, v) => n * v.size)

   /** a function to take a list of layers, and turn it into a lazy stream of branches */
   // todo this used to return a scalaz EphemeralStream.
   def branches[T](layers: List[List[T]]): LazyList[List[T]] =
     layers.foldLeft(LazyList(List[T]())) {
       case (streamOfBranches, layer) =>
         for
            mapping <- LazyList(layer*)
            branch  <- streamOfBranches
         yield mapping :: branch
     }

   /** transform a ListMap - order is important - into a List of layers of maps
     *
     * @param nodeMapping
     *   an ordered Map
     * @tparam T
     * @return
     *   a list of layers of maps.
     */
   def treeLevels[T](nodeMapping: ListMap[T, Set[T]]): List[List[(T, T)]] =
     nodeMapping.toList.map { case (key, values) => values.toList.map(v => (key, v)) }
