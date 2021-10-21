package org.w3.banana.operations

import org.w3.banana.RDF
import org.w3.banana.RDF.Statement as St

trait Statement[Rdf<:RDF]:
	extension (subj: St.Subject[Rdf])
		def fold[A](uriFnct: RDF.URI[Rdf] => A, bnFcnt: RDF.BNode[Rdf] => A): A
