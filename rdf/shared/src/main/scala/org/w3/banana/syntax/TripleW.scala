package org.w3.banana.syntax


import org.w3.banana.{RDF,Ops}
import RDF.*

extension [Rdf<:RDF](triple: Triple[Rdf])(using ops: Ops[Rdf])
	def subj: Node[Rdf] = ops.Triple.subjectOf(triple)
	def rel: URI[Rdf]   = ops.Triple.relationOf(triple)
	def obj: Node[Rdf]  = ops.Triple.objectOf(triple)

extension [Rdf<:RDF](rtriple: rTriple[Rdf])(using ops: Ops[Rdf])
	def rsubj: rNode[Rdf] = ops.rTriple.subjectOf(rtriple)
	def rrel: rURI[Rdf]   = ops.rTriple.relationOf(rtriple)
	def robj: rNode[Rdf]  = ops.rTriple.objectOf(rtriple)

