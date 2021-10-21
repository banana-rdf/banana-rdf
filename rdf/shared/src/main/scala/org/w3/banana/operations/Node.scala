package org.w3.banana.operations

import org.w3.banana.RDF

trait Node[Rdf<:RDF]:
	extension (node: RDF.Node[Rdf])
		def fold[A](
			uriF: RDF.URI[Rdf] => A,
			bnF:  RDF.BNode[Rdf] => A,
			litF: RDF.Literal[Rdf] => A
		): A =
			if node.isURI then uriF(node.asInstanceOf[RDF.URI[Rdf]])
			else if node.isBNode then bnF(node.asInstanceOf[RDF.BNode[Rdf]])
			else if node.isLiteral then litF(node.asInstanceOf[RDF.Literal[Rdf]])
			else //we should never get here, but refactorings could happen...
				throw IllegalArgumentException(
					s"node.fold() received `$node` which is neither a BNode, URI or Literal. Please report."
				)

		def isURI: Boolean
		def isBNode: Boolean
		def isLiteral: Boolean

	extension (node: RDF.Statement.Object[Rdf])
	//todo: find a way to remove this asInstanceOf
		def asNode: RDF.Node[Rdf] = node.asInstanceOf[RDF.Node[Rdf]]
end Node