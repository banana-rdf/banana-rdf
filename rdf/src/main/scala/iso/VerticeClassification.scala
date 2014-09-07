package org.w3.banana.iso

import org.w3.banana.{ RDF, RDFOps }

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

/**
 * A Vertice Classification Builder for a simple classification algorithm.
 * More advanced classification builders may need different methods
 *
 * @tparam Rdf
 */
trait VerticeCBuilder[Rdf <: RDF] {

  def setForwardRel(rel: Rdf#URI, obj: Rdf#Node): Unit

  def setBackwardRel(rel: Rdf#URI, subj: Rdf#Node): Unit

  /**
   * @return a VerticeClassification when done
   */
  def result: VerticeClassification

}

/**
 * This is a much too simple Vertice classifier.
 * It is useful to test the MappingGenerator though.
 * It classifies each vertice by the number of certain types of relations it has.
 * WARNING: Do not use in production! Checking isomorphisms with this class on a list with a
 * little duplication can make your machine stall. eg: comparing two graphs each containing the same
 * two lists of length 6 and 5 can lead to a search tree of 65 million. This is better that the full
 * ~ 10 billion search space, but nowhere near small enough to be useful. In comparison the
 * SimpleHashVerticeType brings that down to 64.
 *
 * This is a case class as the case class, as that gives us the equals and hash methods.
 * One could use an immutable Map, but that may be more expensive.
 *
 * @tparam Rdf
 */
class CountingVCBuilder[Rdf <: RDF]
    extends VerticeCBuilder[Rdf] {
  private val forwardRels = mutable.HashMap[Rdf#URI, Long]().withDefaultValue(0)
  private val backwardRels = mutable.HashMap[Rdf#URI, Long]().withDefaultValue(0)

  def setForwardRel(rel: Rdf#URI, obj: Rdf#Node): Unit = {
    forwardRels.put(rel, forwardRels(rel) + 1)
  }

  def setBackwardRel(rel: Rdf#URI, subj: Rdf#Node): Unit = {
    backwardRels.put(rel, backwardRels(rel) + 1)
  }

  override def result: CountingVC = CountingVC(forwardRels.hashCode(), backwardRels.hashCode())

}

case class CountingVC(forwardRels: Int,
  backwardRels: Int) extends VerticeClassification

/**
 *
 * @param ops needed to calculate the hash of Nodes
 * @tparam Rdf
 */
case class SimpleHashVCBuilder[Rdf <: RDF](implicit ops: RDFOps[Rdf])
    extends VerticeCBuilder[Rdf] {

  val forwardRels = mutable.Map[Rdf#URI, Long]().withDefaultValue(0)
  val backwardRels = mutable.Map[Rdf#URI, Long]().withDefaultValue(0)
  val bnodeValue = 2017 // prime number

  import ops._

  def hashOf(node: Rdf#Node) = node.fold(_.hashCode, _ => bnodeValue, _.hashCode)

  def setForwardRel(rel: Rdf#URI, obj: Rdf#Node): Unit =
    forwardRels.put(rel, (forwardRels(rel) + hashOf(obj)) % Long.MaxValue)

  def setBackwardRel(rel: Rdf#URI, subj: Rdf#Node): Unit =
    backwardRels.put(rel, (backwardRels(rel) + hashOf(subj)) % Long.MaxValue)

  override def result: HashVC = HashVC(forwardRels.hashCode(), backwardRels.hashCode())

}

case class HashVC(forwardRels: Long,
  backwardRels: Long) extends VerticeClassification

object VerticeCBuilder {

  def simpleHash[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = () => new SimpleHashVCBuilder[Rdf]()

  def counting[Rdf <: RDF] = () => new CountingVCBuilder[Rdf]()

}
