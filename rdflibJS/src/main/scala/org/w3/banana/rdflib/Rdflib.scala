package org.w3.banana.rdflib

import org.w3.banana.rdflib.Rdflib.rlNode
import types.rdflib.{mod, namedNodeMod}
import org.w3.banana.{Ops, RDF}
import types.rdflib.tfTypesMod.{QuadGraph, QuadObject, QuadPredicate, QuadSubject}
import types.rdflib.typesMod.SubjectType

import scala.annotation.targetName
import scala.util.{Success, Try}
import scala.reflect.TypeTest
import scala.scalajs.js.undefined


object Rdflib extends RDF {
	import types.rdflib.tfTypesMod as rdfTp
	import types.rdflib.storeMod
	type rlNode = types.rdflib.nodeInternalMod.Node
	type rlNamedNode = types.rdflib.namedNodeMod.NamedNode
	type rlBlank = types.rdflib.blankNodeMod.BlankNode
	type rlLiteral = types.rdflib.literalMod.Literal
	protected type Quad =  rdfTp.Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]
	//	protected type Quad =  rdfTp.Quad[rdfTp.Term, rdfTp.Term, rdfTp.Term, rdfTp.Term]
	//I need to check how far rdflib really allows relative URIs.
	//Because a Store indexes those, it may be better to represent these as Sets
	// since we anly really want to use them for constructing resources to POST or PUT
	override opaque type rGraph = storeMod.IndexedFormula
	override opaque type rTriple = Quad
	override opaque type rURI = types.rdflib.namedNodeMod.NamedNode

	override opaque type Graph = storeMod.IndexedFormula
	override opaque type Triple <: Matchable = Quad
	override opaque type Node <: Matchable = rlNode
	override opaque type URI <: Node =  rlNamedNode //rdfTp.NamedNode
	override opaque type BNode <: Node = rlBlank
	override opaque type Literal <: Node = rlLiteral
	override opaque type Lang <: Matchable = String


//	given uriTT: TypeTest[Node,URI] with {
//		override def unapply(s: Node): Option[s.type & URI] =
//			s match
//				//note: using rjIRI won't compile
//				case x: (s.type & org.eclipse.rdf4j.model.IRI) => Some(x)
//				case _ => None
//	}

//	given literalTT: TypeTest[Node,Literal] with {
//		override def unapply(s: Node): Option[s.type & Literal] =
//			s match
//				//note: this does not compile if we use URI instead of jena.Node_URI
//				case x: (s.type & org.eclipse.rdf4j.model.Literal) => Some(x)
//				case _ => None
//	}

	/**
	 * Here we build up the methods functions allowing RDF.Graph[R] notation to be used.
	 *
	 * This will be the same code in every singleton implementation of RDF.
    **/
	given ops: Ops[R] with {
//		val rdf: types.rdflib.tfTypesMod.RdfJsDataFactory = types.rdflib.rdflibDataFactoryMod.default
		val rdf: types.rdflib.typesMod.IRDFlibDataFactory = types.rdflib.rdflibDataFactoryMod.default
		val rdfDF: types.rdflib.tfTypesMod.RdfJsDataFactory = rdf.asInstanceOf[types.rdflib.tfTypesMod.RdfJsDataFactory]
		import types.rdflib.termsMod
		import scala.collection.mutable
		given Graph: GraphOps with
			def empty: RDF.Graph[R] = rdf.graph()
			def apply(triples: Iterable[RDF.Triple[R]]): RDF.Graph[R] =
				val graph: storeMod.IndexedFormula = rdf.graph()
				triples foreach { (t: Quad) =>
					graph.add(
						t.subject.asInstanceOf[rdfTp.QuadSubject],
						t.predicate.asInstanceOf[rdfTp.QuadPredicate],
						t.`object`.asInstanceOf[rdfTp.Term],
						undefined)
				}
				graph
			def triplesIn(graph: RDF.Graph[R]): Iterable[RDF.Triple[R]] =
				val seq: mutable.Seq[RDF.Triple[R]] =
					graph.`match`(undefined,undefined,undefined,undefined)
				seq.toIterable
			//note the graph size may be bigger as we are using a quad store
			def graphSize(graph: RDF.Graph[R]): Int =
				graph.length.toInt

			//If one modelled Graphs as Named Graphs, then union could just be unioning the names
			//this type of union is very inefficient
			def union(graphs: Seq[RDF.Graph[R]]): RDF.Graph[R] =
				graphs match
					case Seq(x) => x
					case _ =>
						val newGraph: storeMod.default = rdf.graph()
						graphs.foreach(g => newGraph.add( g.`match`(undefined,undefined,undefined,undefined)))
						newGraph

			def difference(g1: RDF.Graph[R], g2: RDF.Graph[R]): RDF.Graph[R] =
				val graph = rdf.graph()
				triplesIn(g1) foreach { triple =>
					if !g2.holdsStatement(triple) then graph.add(triple)
				}
				graph

			def isomorphism(left: RDF.Graph[R], right: RDF.Graph[R]): Boolean = ???
				//note: if we make sure that the opaque Graph, never contains contexts,
				//  then this is all we need to do. Otherwise we need to strip contexts.
				//Models.isomorphic(left, right)
				//todo: no isomorphism in rdflib, use my lib

		val rGraph = new rGraphOps:
			def empty: RDF.rGraph[R] = Graph.empty
			def apply(triples: Iterable[RDF.rTriple[R]]): RDF.rGraph[R] =
				Graph(triples)
			def triplesIn(graph: RDF.rGraph[R]): Iterable[RDF.rTriple[R]] =
				Graph.triplesIn(graph)
			def graphSize(graph: RDF.rGraph[R]): Int =
				Graph.graphSize(graph).toInt

//		given tripleTT: TypeTest[Matchable, RDF.Triple[R]] with {
//			override def unapply(s: Matchable): Option[s.type & Triple] =
//				s match
//					//note: this does not compile if we use URI instead of jena.Node_URI
//					case x: (s.type & Triple) => Some(x)
//					case _ => None
//		}

		given Triple: TripleOps with
			//todo: check whether it really is not legal in rdflib to have a literal as subject
			// warning throws an exception
			def apply(s: RDF.Node[R], p: RDF.URI[R], o: RDF.Node[R]): RDF.Triple[R] =
				if termsMod.isSubject(s) then
					rdfDF.triple(s.asInstanceOf[rdfTp.Term],p.asInstanceOf[rdfTp.Term],o.asInstanceOf[rdfTp.Term]).asInstanceOf[Quad]
				else throw new RuntimeException("makeTriple: in rdflib, subject " + s.toString + " must be a either URI or BlankNode")
			def untuple(t: RDF.Triple[R]): TripleI =
					(subjectOf(t), relationOf(t), objectOf(t))
			def subjectOf(t: RDF.Triple[R]): RDF.Node[R] = t.subject.asInstanceOf[rlNode]
			def relationOf(t: RDF.Triple[R]): RDF.URI[R] = t.predicate.asInstanceOf[rlNamedNode]
			def objectOf(t: RDF.Triple[R]): RDF.Node[R] = t.`object`.asInstanceOf[rlNode]
		end Triple

		//todo: see whether this really works! It may be that we need to create a new construct
		val rTriple = new rTripleOps:
			def apply(s: RDF.rNode[R], p: RDF.rURI[R], o: RDF.rNode[R]): RDF.rTriple[R] =
				Triple(s, p, o)
			def untuple(t: RDF.rTriple[R]): rTripleI =
				(subjectOf(t), relationOf(t), objectOf(t))
			def subjectOf(t: RDF.rTriple[R]): RDF.rNode[R] = Triple.subjectOf(t)
			def relationOf(t: RDF.rTriple[R]): RDF.rURI[R] = Triple.relationOf(t)
			def objectOf(t: RDF.rTriple[R]): RDF.rNode[R] = Triple.objectOf(t)
		end rTriple

		given Node: NodeOps with
			extension (node: RDF.Node[R])
				def fold[A](
					bnF: RDF.BNode[R] => A,
					uriF: RDF.URI[R] => A,
					litF: RDF.Literal[R] => A
				): A =
					if termsMod.isBlankNode(node) then
						bnF(node.asInstanceOf[rlBlank])
					else if termsMod.isNamedNode(node) then
						uriF(node.asInstanceOf[rlNamedNode])
					else if termsMod.isBlankNode(node) then
						litF(node.asInstanceOf[rlLiteral])
					else throw new IllegalArgumentException(
						s"node.fold() received `$node` which is neither a BNode, URI or Literal. Please report."
					)

		given BNode: BNodeOps with
			def apply(s: String): RDF.BNode[R] = types.rdflib.blankNodeMod.default(s)
			def apply(): RDF.BNode[R] = types.rdflib.blankNodeMod.default()
			extension (bn: RDF.BNode[R])
				def label: String = bn.value
		end BNode

		given Literal: LiteralOps with
			private val xsdString = rdfDF.namedNode(xsdStr).nn
			private val xsdLangString = rdfDF.namedNode(xsdLangStr).nn
			import LiteralI as Lit

			def apply(plain: String): RDF.Literal[R] =
				types.rdflib.literalMod.default(plain)
			def apply(lit: Lit): RDF.Literal[R] = lit match
				case Lit.Plain(text) => apply(text)
				case Lit.`@`(text, lang) => types.rdflib.literalMod.default(text,lang)
				case Lit.`^^`(text, tp) => types.rdflib.literalMod.default(text,null,lang)

			def unapply(x: Matchable): Option[LiteralI] =
				if termsMod.isLiteral(x.asInstanceOf[scalajs.js.Any]) then
					val lit: rlLiteral = x.asInstanceOf[rlLiteral]
					val lex: String = lit.value.nn
					val dt: RDF.URI[R] = lit.datatype.asInstanceOf[rlNamedNode]
					val lang: String = lit.language.nn
					if (lang.isEmpty) then
					//todo: this comparison could be costly, check
						if dt == xsdString then Some(Lit.Plain(lex))
						else Some(Lit.^^(lex, dt))
					else if dt == xsdLangString then
						Some(Lit.`@`(lex, Lang(lang)))
					else None
				else  None

			@targetName("langLit")
			def apply(lex: String, lang: RDF.Lang[R]): RDF.Literal[R] =
				types.rdflib.literalMod.default(lex,lang)

			@targetName("dataTypeLit")
			def apply(lex: String, dataTp: RDF.URI[R]): RDF.Literal[R] =
				types.rdflib.literalMod.default(lex,null, lang)

			extension (lit: RDF.Literal[R])
				def text: String = lit.value
		end Literal

		override given literalTT: TypeTest[Matchable, RDF.Literal[R]] with {
			override def unapply(s: Matchable): Option[s.type & RDF.Literal[R]] =
				if termsMod.isLiteral(s.asInstanceOf[scalajs.js.Any]) then
					Some(s.asInstanceOf[s.type & rlLiteral])
				else None
		}

		given Lang: LangOps with {
			def apply(lang: String): RDF.Lang[R] = lang
			extension (lang: RDF.Lang[R])
				def label: String =  lang
		}

		val rURI = new rURIOps:
			def apply(iriStr: String): RDF.rURI[R] = types.rdflib.namedNodeMod.default(iriStr)
			def asString(uri: RDF.rURI[R]): String = uri.toString

		given URI: URIOps with
			//this does throw an exception on non relative URLs!
			def mkUri(iriStr: String): Try[RDF.URI[R]] =
				Try(new types.rdflib.mod.NamedNode(iriStr))
			def asString(uri: RDF.URI[R]): String = uri.value

//			extension(uri: RDF.URI[R])
//				def !=(other: RDF.URI[R]): Boolean = !(uri == other)
		end URI

	}

	// mutable graphs
//	type MGraph = Model
//
//	// types for the graph traversal API
//	type NodeMatch = Value
//	type NodeAny = Null
//	type NodeConcrete = Value
//
//	// types related to Sparql
//	type Query = ParsedQuery
//	type SelectQuery = ParsedTupleQuery
//	type ConstructQuery = ParsedGraphQuery
//	type AskQuery = ParsedBooleanQuery
//
//	//FIXME Can't use ParsedUpdate because of https://openrdf.atlassian.net/browse/SES-1847
//	type UpdateQuery = Rdf4jParseUpdate
//
//	type Solution = BindingSet
//	// instead of TupleQueryResult so that it's eager instead of lazy
//	type Solutions = Vector[BindingSet]
}
