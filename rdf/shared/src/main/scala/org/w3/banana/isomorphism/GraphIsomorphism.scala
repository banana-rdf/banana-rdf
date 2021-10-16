package org.w3.banana.isomorphism

import org.w3.banana.{Ops, RDF}

import scala.collection.mutable
import scala.util.control.NoStackTrace
import scala.util.{Failure, Success, Try}

/**
 * Methods to establish Graph Equivalences
 *
 * Following Jeremy J. Carroll's "Matching RDF Graphs" article
 * http://www.hpl.hp.com/techreports/2001/HPL-2001-293.pdf
 *
 *
 * Also I found it useful to think in terms of the RDF category
 * as defined by Benjamin Braatz in "Formal Modelling and Application of Graph Transformations
 * in the Resource Description Framework" Proposition 2.1 ( p 16 )
 *
 * http://www.researchgate.net/profile/Benjamin_Braatz/publication/40635984_Formal_Modelling_and_Application_of_Graph_Transformations_in_the_Resource_Description_Framework/file/d912f50d3189b51ef1.pdf
 *
 * This code can be ameliorated in a number of ways:
 *
 * - by using lazy data structures
 * - by optimising memory
 *
 * @param mappingGen a mapping generator that knows to find for two graphs the possible mappings of bnodes between
 *                   them. Better mapping generators will find less mappings for the same graphs, without missing out
 *                   on correct ones.
 * @tparam Rdf RDF implementation to work with
 */
//noinspection ScalaStyle
final case class GraphIsomorphism[Rdf <: RDF](
	val mappingGen: MappingGenerator[Rdf], maxComplexity: Int = 65536
)(implicit ops: Ops[Rdf]) {

	import ops.*

	/**
	 * filter a graphs into two subgraphs the first one containing no blank nodes
	 * the other one containing only statements with blank nodes
	 *
	 * @return a pair of Ground graph and non ground graph
	 */
	def groundTripleFilter(graph: Graph[Rdf]): (Graph[Rdf], Graph[Rdf]) = {
		var ground: Graph[Rdf] = Graph.empty
		var nonGround: Graph[Rdf] = Graph.empty
		for (triple <- graph.triples) {
			triple match {
				case Triple(s, r, o) if s.isBNode || o.isBNode => nonGround = nonGround + triple
				case _ => ground = ground + triple
			}
		}
		(ground, nonGround)

	}

	/**
	 * Find possible bnode mappings
	 *
	 * @return A list of possible bnode mappings or a reason for the error
	 */
	def possibleMappings(g1: Graph[Rdf], g2: Graph[Rdf]): Try[EphemeralStream[List[(BNode[Rdf], BNode[Rdf])]]] = {
		if (g1.size != g2.size)
			return Failure(MappingException(s"graphs don't have the same number of triples: g1.size=${g1.size} g2.size=${g2.size}"))

		val (grnd1, nongrnd1) = groundTripleFilter(g1)
		val (grnd2, nongrnd2) = groundTripleFilter(g2)
		if (grnd1.size != grnd2.size)
			return Failure(MappingException("the two graphs don't have the same number of ground triples. " +
				s"ground(g1).size=${grnd1.size} ground(g2).size=${grnd2.size}"))

		val bnodeMaps = mappingGen.bnodeMappings(nongrnd1, nongrnd2)
		val complexity = MappingGenerator.complexity(bnodeMaps)
		if (complexity > maxComplexity)
			return Failure(MappingException(s"Search space too big. maxComplexity is set to $maxComplexity but the search space is of size $complexity"))

		bnodeMaps map { nodeMapping =>
			import org.w3.banana.isomorphism.MappingGenerator.*
			/**
			 * We want to go from a Map(1->Set(1,2),2->Set(21,22),3->Set(33,34))
			 * to a Tree of potential answers looking like this
			 * <pre>
			 * (1,1)--->(2,21)
			 * ..|        |-------> (3,33)
			 * ..|        |-------> (3,34)
			 * ..|----->(2,22)
			 * ..|        |-------> (3,33)
			 * ..|        |-------> (3,34)
			 * (1,2)--->(2,21)
			 * ..|        |-------> (3,33)
			 * ..|        |-------> (3,34)
			 * ..|----->(2,22)
			 * ..         |-------> (3,33)
			 * ..         |-------> (3,34)
			 * </pre>
			 * each of the paths from the root to the leaves constitutes one potential solution,
			 * eg: List((1,2),(2,21),(3,34))
			 *
			 * So the original Map constitutes the layers of the result, and we want to go from that to
			 * a lazy stream of paths. ( so we can stop as soon as we found one result )
			 *
			 */
			branches[(BNode[Rdf], BNode[Rdf])](treeLevels[BNode[Rdf]](nodeMapping))
		}
	}

	/*
	 * @return A Stream of valid bnode mappings or a reason for the error
	 */
	def possibleAnswers(g1: Graph[Rdf], g2: Graph[Rdf]): Try[EphemeralStream[List[(BNode[Rdf], BNode[Rdf])]]] =
		possibleMappings(g1, g2).map {
			_.filter(answer => mapVerify(g1, g2, answer).isEmpty)
		}

	/** @return the first answer or the failure that there is no mapping */
	def findAnswer(g1: Graph[Rdf], g2: Graph[Rdf]): Try[List[(BNode[Rdf], BNode[Rdf])]] =
		possibleAnswers(g1, g2).flatMap(_.headOption match {
			case Some(nodeList) => Success(nodeList)
			case None => Failure[Nothing](NoMappingException(EphemeralStream()))
		})


	/**
	 * Verify that the bnode bijection allows one to map graph1 to graph2
	 *
	 * @param mapping the mappings to verify.
	 * @return a list of exceptions in case of failure or an empty list in case of success
	 *
	 */
	def mapVerify(
		graph1: Graph[Rdf],
		graph2: Graph[Rdf],
		mapping: List[(BNode[Rdf], BNode[Rdf])]): List[MappingException] = {
		// verify that both graphs are the same size
		if (graph1.size != graph2.size)
			return List(MappingException(s"graphs not of same size. graph1.size=${graph1.size} graph2.size=${graph2.size}"))

		// 1. verify that bnodeBijection is a bijection, fail early
		val bnodeBijection: mutable.HashMap[BNode[Rdf], BNode[Rdf]] = {
			val back = new mutable.HashMap[BNode[Rdf], BNode[Rdf]]()
			val map = new mutable.HashMap[BNode[Rdf], BNode[Rdf]]()
			for ((from, to) <- mapping) {
				if (back.put(to, from).fold[Boolean](true)(_ == from)
					&& map.put(from, to).fold(true)(_ == to)) { //all good
				} else return List[MappingException](
					MappingException(s"bnodeBijection is not a bijection: $from already mapped"))
			}
			map
		}

		def bnmap(node: Node[Rdf]): Node[Rdf] = node.fold(
			(uri: URI[Rdf]) => uri,
			(bnode: BNode[Rdf]) => bnodeBijection(bnode),
			(lit: Literal[Rdf]) => lit
		)

		//2. use the bijection to verify that it maps the triples correctly
		val mappTriples = for (Triple(sub, rel, obj) <- graph1.triples) yield {
			makeTriple(bnmap(sub), rel, bnmap(obj))
		}
		mappTriples.find(!graph2.contains(_)).toList.map(t => MappingException(s"could not find map for $t"))

		//do I have to test that the mapping goes the other way too? Or is it sufficient if the bnodesmap is a bijection?
	}

	case class NoMappingException(val reasons: EphemeralStream[(List[(BNode[Rdf], BNode[Rdf])], List[MappingException])]) extends MappingError {
		def msg: String = "No mapping found"

		override def toString(): String = s"NoMappingException($reasons)"
	}

}

trait MappingError extends NoStackTrace {
	def msg: String
}

case class MappingException(msg: String) extends MappingError {
	override def toString(): String = s"MappingException($msg)"
}

