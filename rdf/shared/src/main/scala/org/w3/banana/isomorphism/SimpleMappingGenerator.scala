package org.w3.banana.isomorphism

import org.w3.banana.{Ops, RDF}

import scala.collection.immutable.ListMap
import scala.collection.{immutable, mutable}
import scala.util.Try

/*
 * The SimpleMappingGenerator implements only the first stage of Jeremy
 * Carroll's optimisation strategy. It classifies nodes only by the arrows
 * going in and out, but does not follow those further.
 *
 * @param VT the classifier
 * @param maxComplexity the maximum number of solutions to look at, otherwise fails
 */
final class SimpleMappingGenerator[Rdf <: RDF](VT: () => VerticeCBuilder[Rdf])(implicit ops: RDFOps[Rdf])
	extends MappingGenerator[Rdf] {

	import ops.*

	/**
	 * generate a list of possible bnode mappings, filtered by applying classification algorithm
	 * on the nodes by relating them to other edges
	 *
	 * @return a ListMap mapping BNode from graph g1 to a smaller set of Bnodes from graph g2 which
	 *         they should corresond to
	 */
	def bnodeMappings(
		g1: Graph[Rdf],
		g2: Graph[Rdf]): Try[immutable.ListMap[BNode[Rdf], immutable.Set[BNode[Rdf]]]] = Try {
		val clz1 = bnodeClassify(g1)
		val clz2 = bnodeClassify(g2)
		if (clz1.size != clz2.size)
			throw ClassificationException("the two graphs don't have the same number of classes.", clz1, clz2)
		val mappingOpts: mutable.Map[BNode[Rdf], mutable.Set[BNode[Rdf]]] = mutable.HashMap[BNode[Rdf], mutable.Set[BNode[Rdf]]]()
		for {
			(vt, bnds1) <- clz1 // .sortBy { case (vt, bn) => bn.size }
			bnds2 <- clz2.get(vt)
		} {
			if (bnds2.size != bnds1.size)
				throw ClassificationException(s"the two graphs don't have the same number of bindings for type $vt", clz1, clz2)
			for (bnd <- bnds1) {
				mappingOpts.get(bnd).orElse(Some(mutable.Set.empty[BNode[Rdf]])).map { bnset =>
					mappingOpts.put(bnd, bnset ++= bnds2)
				}
			}
		}
		//todo: this transformation to immutable is expensive
		ListMap(mappingOpts.toSeq.map(l => (l._1, l._2.toSet)).sortBy(_._2.size): _*)
	}

	/**
	 * This classification can be improved, but it is easier to debug while it is not so effective.
	 *
	 * @return a classification of bnodes by type, where nodes can only be matched by other nodes of the same type
	 */
	def bnodeClassify(graph: Graph[Rdf]): Map[VerticeClassification, Set[BNode[Rdf]]] = {
		val bnodeClass = mutable.HashMap[BNode[Rdf], VerticeCBuilder[Rdf]]()
		for (triple <- graph.triples) {
			triple.subj.fold((_: URI[Rdf]) => (),
				(bn: BNode[Rdf]) => bnodeClass.get(bn).orElse {
					val vt = VT()
					bnodeClass.put(bn, vt)
					Some(vt)
				}.map { vt =>
					vt.setForwardRel(rel, obj)
				},
				(_: Literal[Rdf]) => ()
			)

			triple.obj.fold(
				(_: URI[Rdf]) => (),
				(bn: BNode[Rdf]) => bnodeClass.get(bn).orElse {
					val vt = VT()
					bnodeClass.put(bn, vt)
					Some(vt)
				}.map { vt =>
					vt.setBackwardRel(rel, subj)
				},
				(_: Literal[Rdf]) => ())
		}
		bnodeClass.view.mapValues(_.result).toMap
			.groupBy(_._2)
			.view.mapValues(_.keys.toSet).toMap
	}

	case class ClassificationException(
		msg: String,
		clz1: Map[VerticeClassification, Set[BNode[Rdf]]],
		clz2: Map[VerticeClassification, Set[BNode[Rdf]]]) extends MappingError {
		override def toString() = s"ClassificationException($msg,$clz1,$clz2)"
	}

}
