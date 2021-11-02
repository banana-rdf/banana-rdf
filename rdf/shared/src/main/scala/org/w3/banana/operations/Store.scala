package org.w3.banana.operations

import org.w3.banana.{RDF,Ops}
import org.w3.banana.RDF.Statement as St

/**
 * trait for Factories to create store
 *
 * There is too much diversity in preferences for store creation to hope to
 * be able to create a uniform interface for all of them at this time.
 * Perhaps experience working with them will reveal some commonality.
 * So for the moment it is better to have a very simple trait that programmers
 * can pass as givens in their context.
 */
trait StoreFactory[Rdf <: RDF]:
	def makeStore(): RDF.Store[Rdf]
end StoreFactory

trait Store[Rdf<:RDF](using ops: Ops[Rdf]):
	import ops.{given, *}
	def apply()(using sf: StoreFactory[Rdf]): RDF.Store[Rdf] = sf.makeStore()

	extension (store: RDF.Store[Rdf])
		def add(qs: RDF.Quad[Rdf]*): store.type

		def add(gr: RDF.Graph[Rdf], at: St.Graph[Rdf] = store.default): store.type = 
			for t <-  gr.triples do store.add(Quad(t.subj, t.rel, t.obj, at)) 
			store

      //todo: in multithreaded environments needs transactions
		def set(gr: RDF.Graph[Rdf], at: St.Graph[Rdf] = store.default): store.type =
			store.remove(`*`,`*`,`*`,at)
			store.add(gr,at)
			store

		def remove(qs: RDF.Quad[Rdf]*): store.type

		def remove(
			s: St.Subject[Rdf] | RDF.NodeAny[Rdf],
			p: St.Relation[Rdf] | RDF.NodeAny[Rdf],
			o: St.Object[Rdf] | RDF.NodeAny[Rdf],
			g: St.Graph[Rdf] | RDF.NodeAny[Rdf]
		): store.type

		def find(
			s: St.Subject[Rdf] | RDF.NodeAny[Rdf],
			p: St.Relation[Rdf] | RDF.NodeAny[Rdf],
			o: St.Object[Rdf] | RDF.NodeAny[Rdf],
		): Iterator[RDF.Quad[Rdf]] = find(s,p,o,default)

		def find(
			s: St.Subject[Rdf] | RDF.NodeAny[Rdf],
			p: St.Relation[Rdf] | RDF.NodeAny[Rdf],
			o: St.Object[Rdf] | RDF.NodeAny[Rdf],
			g: St.Graph[Rdf] | RDF.NodeAny[Rdf]
		): Iterator[RDF.Quad[Rdf]]

		def default: St.Graph[Rdf]
end Store