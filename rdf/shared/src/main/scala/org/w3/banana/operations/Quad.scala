package org.w3.banana.operations

import org.w3.banana.RDF
import org.w3.banana.RDF.Quad
import org.w3.banana.RDF.Statement as St


/**
 * A Quad is a structural description of what is used as a statement.
 * A Statement is the expression of a relation r, where
 *    r ∈ Subject ✖️ Predicate ✖️ Object triple.
 * The same relation can appear in any number of statements which is just a
 * relation expressed somewhere (written down at some place).
 * Statement = Relation ✖️️️ Where ≅ (Subject ✖️ Predicate ✖️ Object) ✖️ Where
 *                               ≅  Subject ✖️ Predicate ✖️ Object ✖️ Where
 * I.e. a statement is encoded as a quad.
 * Interestingly enough a Statement can also be true or false. It is true if that
 * Relation was stated at that place.
 **/
trait Quad[Rdf<:RDF](ops: org.w3.banana.Ops[Rdf]):
	type QuadI = (St.Subject[Rdf], St.Relation[Rdf], St.Object[Rdf], St.Graph[Rdf])
	def defaultGraph: RDF.DefaultGraphNode[Rdf]
	def apply(s: St.Subject[Rdf], p: St.Relation[Rdf], o: St.Object[Rdf]): RDF.Quad[Rdf]
	def apply(
		s: St.Subject[Rdf], p: St.Relation[Rdf],
		o: St.Object[Rdf], where: St.Graph[Rdf]
	): RDF.Quad[Rdf]
	def unapply(t: RDF.Quad[Rdf]): Option[QuadI] = Some(untuple(t))
	def untuple(t: RDF.Quad[Rdf]): QuadI = (subjectOf(t),relationOf(t),objectOf(t),graphOf(t))
	protected def subjectOf(s: RDF.Quad[Rdf]): St.Subject[Rdf]
	protected def relationOf(s: RDF.Quad[Rdf]): St.Relation[Rdf]
	protected def objectOf(s: RDF.Quad[Rdf]): St.Object[Rdf]
	protected def graphOf(s: RDF.Quad[Rdf]): St.Graph[Rdf]
	extension (quad: RDF.Quad[Rdf])
		def triple: RDF.Triple[Rdf] = //we have to make a Triple so that equality works
			ops.Triple(quad.subj, quad.rel, quad.obj)
		def subj: St.Subject[Rdf] = subjectOf(quad)
		def rel: St.Relation[Rdf] = relationOf(quad)
		def obj: St.Object[Rdf] = objectOf(quad)
		def graph: St.Graph[Rdf] = graphOf(quad)
		def at(g: St.Graph[Rdf]) = apply(quad.subj,quad.rel,quad.obj, g)

	extension (graphNode: St.Graph[Rdf])
		def isDefault: Boolean = graphNode == defaultGraph
end Quad
