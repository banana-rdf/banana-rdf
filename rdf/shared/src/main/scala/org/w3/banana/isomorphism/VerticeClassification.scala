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

import org.w3.banana.{Ops, RDF}
import org.w3.banana.RDF.*

import scala.collection.mutable

/*
 * A VerticeClassification classifies vertices ( BNodes in subject or object position )
 * into more or less specialised groups, such that two nodes in two graphs that are isomorphic belong
 * to the same VerticeClassification. Or equivalently it should never be the case that a node n1 in
 * graph g1 and a node n2 in graph g2, where g1 and g2 are isomorphic, and one of the isomorphic mappings maps
 * n1 to n2, that n1 and n2 belong to to two non equal VerticeClassification.
 *
 * Better implementations will classify Vertices more finely. This can then be used to reduce the search
 * space for finding isomorphisms between them, since nodes will only need to other nodes that are part
 * of the same VerticeClassification.
 *
 *  The only methods of interest here are hash and equals
 *
 */
trait VerticeClassification

/** A Vertice Classification Builder for a simple classification algorithm. More advanced
  * classification builders may need different methods
  */
trait VerticeCBuilder[Rdf <: RDF]:

   def setForwardRel(rel: URI[Rdf], obj: Node[Rdf]): Unit

   def setBackwardRel(rel: URI[Rdf], subj: Node[Rdf]): Unit

   /** @return
     *   a VerticeClassification when done
     */
   def result: VerticeClassification

/** This is a much too simple Vertice classifier. It is useful to test the MappingGenerator though.
  * It classifies each vertice by the number of certain types of relations it has. WARNING: Do not
  * use in production! Checking isomorphisms with this class on a list with a little duplication can
  * make your machine stall. eg: comparing two graphs each containing the same two lists of length 6
  * and 5 can lead to a search tree of 65 million. This is better that the full ~ 10 billion search
  * space, but nowhere near small enough to be useful. In comparison the SimpleHashVerticeType
  * brings that down to 64.
  *
  * This is a case class as the case class, as that gives us the equals and hash methods. One could
  * use an immutable Map, but that may be more expensive.
  */
class CountingVCBuilder[Rdf <: RDF]
    extends VerticeCBuilder[Rdf]:
   private val forwardRels = mutable.HashMap[URI[Rdf], Long]().withDefaultValue(0)
   private val backwardRels = mutable.HashMap[URI[Rdf], Long]().withDefaultValue(0)

   def setForwardRel(rel: URI[Rdf], obj: Node[Rdf]): Unit =
     forwardRels.put(rel, forwardRels(rel) + 1)

   def setBackwardRel(rel: URI[Rdf], subj: Node[Rdf]): Unit =
     backwardRels.put(rel, backwardRels(rel) + 1)

   override def result: CountingVC = CountingVC(forwardRels.hashCode(), backwardRels.hashCode())

case class CountingVC(forwardRels: Int, backwardRels: Int) extends VerticeClassification

/** @param ops
  *   needed to calculate the hash of Nodes
  */
case class SimpleHashVCBuilder[Rdf <: RDF]()(using ops: Ops[Rdf])
    extends VerticeCBuilder[Rdf]:

   val forwardRels = mutable.Map[URI[Rdf], Long]().withDefaultValue(0)
   val backwardRels = mutable.Map[URI[Rdf], Long]().withDefaultValue(0)
   val bnodeValue = 2017 // prime number

   import ops.{given, *}

   def hashOf(node: RDF.Node[Rdf]) = node.fold(_.hashCode, _ => bnodeValue, _.hashCode)

   def setForwardRel(rel: RDF.URI[Rdf], obj: RDF.Node[Rdf]): Unit =
     forwardRels.put(rel, (forwardRels(rel) + hashOf(obj)) % Long.MaxValue)

   def setBackwardRel(rel: RDF.URI[Rdf], subj: RDF.Node[Rdf]): Unit =
     backwardRels.put(rel, (backwardRels(rel) + hashOf(subj)) % Long.MaxValue)

   override def result: HashVC = HashVC(forwardRels.hashCode(), backwardRels.hashCode())

case class HashVC(forwardRels: Long, backwardRels: Long) extends VerticeClassification

object VerticeCBuilder:

   def simpleHash[Rdf <: RDF](using ops: Ops[Rdf]) = () => new SimpleHashVCBuilder[Rdf]()

   def counting[Rdf <: RDF] = () => new CountingVCBuilder[Rdf]()
