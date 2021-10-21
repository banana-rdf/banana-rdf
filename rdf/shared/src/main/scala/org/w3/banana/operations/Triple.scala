package org.w3.banana.operations

import org.w3.banana.RDF
import org.w3.banana.RDF.Statement as St

trait Triple[Rdf<:RDF]:
	type TripleI = (RDF.Node[Rdf], RDF.URI[Rdf], RDF.Node[Rdf])

	def apply(s: St.Subject[Rdf], p: St.Relation[Rdf], o: St.Object[Rdf]): RDF.Triple[Rdf]

	def unapply(t: RDF.Triple[Rdf]): Option[TripleI] = Some(untuple(t))

	def untuple(t: RDF.Triple[Rdf]): TripleI

	protected def subjectOf(s: RDF.Triple[Rdf]): St.Subject[Rdf]

	protected def relationOf(s: RDF.Triple[Rdf]): St.Relation[Rdf]

	protected def objectOf(s: RDF.Triple[Rdf]): St.Object[Rdf]

	extension (triple: RDF.Triple[Rdf])
		def subj: St.Subject[Rdf] = subjectOf(triple)
		def rel: St.Relation[Rdf] = relationOf(triple)
		def obj: St.Object[Rdf] = objectOf(triple)

