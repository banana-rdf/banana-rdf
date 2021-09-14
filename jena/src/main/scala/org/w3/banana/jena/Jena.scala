package org.w3.banana.jena

import org.apache.jena.datatypes.{BaseDatatype, RDFDatatype, TypeMapper}
import org.apache.jena.graph.GraphUtil
import org.apache.jena.graph.Node.ANY as JenaANY
import org.w3.banana.{Ops, RDF}

import scala.reflect.TypeTest
import scala.util.Try
import scala.util.Using
import scala.util.Using.Releasable
import org.apache.jena.util.iterator.ExtendedIterator

object JenaRdf extends RDF {
	type R = JenaRdf.type
	import org.apache.jena.graph as jena
	import org.apache.jena.graph.{NodeFactory, Factory}

	//jena.Graph is modifiable, but we provide no altering methods and always produce new graphs
	override opaque type Graph = jena.Graph
	override opaque type Triple <: Matchable = jena.Triple
	override opaque type Node <: Matchable = jena.Node
	override opaque type URI <: Node = jena.Node_URI
	override opaque type BNode <: Node = jena.Node_Blank
	override opaque type Literal <: Node = jena.Node_Literal
	override opaque type Lang = String

	given [T]: Releasable[ExtendedIterator[T]] with {
		def release(resource: ExtendedIterator[T]): Unit = resource.close()
	}

	override val Triple: TripleOps = new TripleOps  {
		def apply(subj: Node, rel: URI, obj: Node): Triple =
			jena.Triple.create(subj, rel, obj).nn
		override inline
		def untuple(t: Triple): TripleI =
			(subjectOf(t), relationOf(t), objectOf(t))
		override inline
		def subjectOf(triple: Triple): Node = triple.getSubject().nn
		override inline
		def relationOf(triple: Triple): URI = triple.getPredicate.asInstanceOf[URI].nn
		override inline
		def objectOf(triple: Triple): Node  = triple.getObject().nn
	}

	given uriTT: TypeTest[Node,URI] with {
		override def unapply(s: Node): Option[s.type & jena.Node_URI] =
			s match
			//note: this does not compile if we use URI instead of jena.Node_URI
			case x: (s.type & jena.Node_URI) => Some(x)
			case _ => None
	}

	override val URI : URIOps = new URIOps  {
		//todo: this will throw an exception, should return Option
		override inline
		def mkUri(iriStr: String): Try[URI] = Try(NodeFactory.createURI(iriStr).asInstanceOf[URI])
		override inline
		def asString(uri: URI): String = uri.getURI().nn
	}

	override val Literal: LiteralOps = new LiteralOps {
		// TODO the javadoc doesn't say if this is thread safe
		lazy val mapper: TypeMapper = TypeMapper.getInstance.nn
		private val xsdString: RDFDatatype = mapper.getTypeByName("http://www.w3.org/2001/XMLSchema#string").nn
		//			private val __xsdStringURI: URI = URI("http://www.w3.org/2001/XMLSchema#string")
		private val xsdLangString: RDFDatatype = mapper.getTypeByName("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString").nn
		//todo: are we missing a Datatype Type? (check other frameworks)
		def jenaDatatype(datatype: URI): RDFDatatype =
			val iriString: String = URI.asString(datatype)
			val typ: RDFDatatype | Null = mapper.getTypeByName(iriString)
			if typ == null then
				val datatype = new BaseDatatype(iriString)
				mapper.registerDatatype(datatype)
				datatype
			else typ

		def apply(plain: String): Literal =
			NodeFactory.createLiteral(plain).nn.asInstanceOf[Literal]

		def dtLiteral(lex: String, dataTp: URI): Literal =
			NodeFactory.createLiteral(lex, jenaDatatype(dataTp)).nn.asInstanceOf[Literal]

		def langLiteral(lex: String, lang: Lang): Literal =
			NodeFactory.createLiteral(lex, lang).nn.asInstanceOf[Literal]

		def unapply(lit: Literal): Option[LiteralI] =
			import LiteralI.*
			val lex: String = lit.getLiteralLexicalForm.nn
			val dt: RDFDatatype | Null = lit.getLiteralDatatype
			val lang: String | Null = lit.getLiteralLanguage
			if (lang == null || lang.isEmpty) then
				if dt == null || dt == xsdString then Some(Plain(lex))
				else Some(^^(lex, URI(dt.getURI.nn)))
			else if dt == null || dt == xsdLangString then
				Some(`@`(lex, lang))
			else None
	}

	given literalTT: TypeTest[Node,Literal] with {
		override def unapply(s: Node): Option[s.type & Literal] =
			s match
			//note: this does not compile if we use URI instead of jena.Node_URI
			case x: (s.type & jena.Node_Literal) => Some(x)
			case _ => None
	}

	override val Lang: LangOps =  new LangOps {
		override inline def apply(lang: String): Lang = lang
		override inline def label(lang: Lang): String = lang
	}

	override val Graph: GraphOps = new GraphOps {
		override inline def empty: Graph = Factory.empty().nn
		override inline def apply(triples: Triple*): Graph =
			val graph: Graph = Factory.createDefaultGraph.nn
			triples.foreach { triple =>
				graph.add(triple)
			}
			graph

		import scala.jdk.CollectionConverters.{given,*}
		//how should one pass on the information that the Iterable is closeable?
		// https://stackoverflow.com/questions/69153609/is-there-a-cross-platform-autocloseable-iterable-solution-for-scala
		override def triplesIn(graph: Graph): Iterable[Triple] =
			graph.find(JenaANY, JenaANY, JenaANY).nn.asScala.to(Iterable)

		override inline
		def graphSize(graph: Graph): Int =
			graph.size()

		def union(graphs: Seq[Graph]): Graph =
			val g = Factory.createDefaultGraph.nn
			graphs.foreach { graph =>
				Using.resource(graph.find(JenaANY, JenaANY, JenaANY).nn) { it =>
					while it.hasNext do g.add(it.next)
				}
			}
			g

		def diff(g1: Graph, g2: Graph): Graph =
			val g = Factory.createDefaultGraph.nn
			GraphUtil.addInto(g, g1)
			GraphUtil.delete(g, g2.find(JenaANY, JenaANY, JenaANY))
			g

		override inline
		def isomorphism(left: Graph, right: Graph): Boolean =
			left.isIsomorphicWith(right)
	}

	/**
	 * Here we build up the methods functions allowing RDF.Graph[R] notation to be used.
	 *
	 * This will be the same code in every singleton implementation of RDF.
	 * I did not succeed in removing the duplication, as there are Match Type compilation problems.
	 * It does not work to place here the implementations of rdf which can be placed above,
	 * as the RDF.Graph[R] type hides the implementation type (of `graph` field for example) **/
	given ops: Ops[R] with {
		val rdf = JenaRdf

		val Graph = new GraphOps {
			def empty: RDF.Graph[R] = rdf.Graph.empty
			def apply(triples: RDF.Triple[R]*): RDF.Graph[R] = rdf.Graph(triples*)
			def triplesIn(graph: RDF.Graph[R]): Iterable[RDF.Triple[R]] = rdf.Graph.triplesIn(graph)
			def graphSize(graph: RDF.Graph[R]): Int = rdf.Graph.graphSize(graph)
			def union(graphs: Seq[RDF.Graph[R]]): RDF.Graph[R] = rdf.Graph.union(graphs)
			def diff(g1: RDF.Graph[R], g2: RDF.Graph[R]): RDF.Graph[R] = rdf.Graph.diff(g1,g2)
			def isomorphism(left: RDF.Graph[R], right: RDF.Graph[R]): Boolean =
				rdf.Graph.isomorphism(left,right)
		}

		val Triple = new TripleOps {
			def apply(s: RDF.Node[R], p: RDF.URI[R], o: RDF.Node[R]): RDF.Triple[R] =
				rdf.Triple(s, p, o)
		}

		val Literal = new LiteralOps {
			import LiteralI.*
			def apply(plain: String): RDF.Literal[R] = rdf.Literal(plain)
			def apply(lit: LiteralI): RDF.Literal[R] = lit match
				case Plain(text) => apply(text)
				case `@`(text,lang) => langLiteral(text,lang)
				case `^^`(text,tp) => dtLiteral(text,tp)
			def unapply(lit: RDF.Literal[R]): Option[LiteralI] = rdf.Literal.unapply(lit)
			def langLiteral(lex: String, lang: RDF.Lang[R]): RDF.Literal[R] = rdf.Literal.langLiteral(lex,lang)
			def dtLiteral(lex: String, dataTp: RDF.URI[R]): RDF.Literal[R] = rdf.Literal.dtLiteral(lex,dataTp)
		}

		val URI = new URIOps {
			def mkUri(iriStr: String): Try[RDF.URI[R]] = rdf.URI.mkUri(iriStr)
			def asString(uri: RDF.URI[R]): String = rdf.URI.asString(uri)
		}
	}
}