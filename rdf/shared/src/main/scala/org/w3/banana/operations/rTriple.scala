package org.w3.banana.operations

import org.w3.banana.RDF
import org.w3.banana.RDF.{rNode, rStatement, rTriple, rURI}


trait rTriple[Rdf<:RDF]:
	type rTripleI = (rNode[Rdf], rURI[Rdf], rNode[Rdf])
	import RDF.rStatement as rSt

	def apply(s: rSt.Subject[Rdf], p: rSt.Relation[Rdf], o: rSt.Object[Rdf]): RDF.rTriple[Rdf]
	def unapply(t: RDF.Triple[Rdf]): Option[rTripleI] = Some(untuple(t))
	def untuple(t: RDF.Triple[Rdf]): rTripleI
	protected
	def subjectOf(s: RDF.rTriple[Rdf]): rSt.Subject[Rdf]
	protected
	def relationOf(s: RDF.rTriple[Rdf]): rSt.Relation[Rdf]
	protected
	def objectOf(s: RDF.rTriple[Rdf]): rSt.Object[Rdf]
	//todo? should we only have the extension functions?
	extension (rtriple: RDF.rTriple[Rdf])
		def rsubj: rSt.Subject[Rdf] = subjectOf(rtriple)
		def rrel: rSt.Relation[Rdf]   = relationOf(rtriple)
		def robj: rSt.Object[Rdf]  = objectOf(rtriple)
end rTriple