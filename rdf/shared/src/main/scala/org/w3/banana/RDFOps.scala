package org.w3.banana


trait RDFOps[Rdf <: RDF & Singleton]:
	val rdf: Rdf

	//Graph
	def empty: RDF.Graph[Rdf]
	def mkGraph(triples: RDF.Triple[Rdf]*): RDF.Graph[Rdf]
	def iterate(graph: RDF.Graph[Rdf]): Iterable[RDF.Triple[Rdf]]
	def graphSize(graph: RDF.Graph[Rdf]): Int

	//Triple
	def makeTriple(s: RDF.Node[Rdf], p: RDF.URI[Rdf], o: RDF.Node[Rdf]): RDF.Triple[Rdf]
	//def fromTriple(triple: RDF.Triple[Rdf]): (RDF.Node[Rdf], RDF.Node[Rdf], RDF.Node[Rdf])