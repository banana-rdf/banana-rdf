package ideas

import org.apache.jena.datatypes.{BaseDatatype, RDFDatatype, TypeMapper}
import org.apache.jena.graph.impl.LiteralLabelFactory
import org.apache.jena.graph.{Factory, NodeFactory}
import org.apache.jena.sparql.resultset.ResultSetCompare.BNodeIso

import scala.annotation.targetName
import scala.util.Try
import scala.reflect.TypeTest

/**
 * following an idea by Neko-kai https://twitter.com/kai_nyasha
 *  https://discord.com/channels/632277896739946517/632277897448652844/880944909660606525
 * seems to resolve the problem of loosing projection types
 *  https://github.com/lampepfl/dotty/discussions/12527#discussioncomment-1251112
 *
 * Differences with banana-rdf:
 *  -
 *
 */
object MatchTypes {
	type RDFObj = RDF & Singleton

	trait RDF {
		type Graph
		type Triple
		type Node
		type URI <: Node
		type BNode <: Node
		type Literal <: Node
		type Lang

		//interface types: the way we present the types for pattern matching
		type TripleI = (Node, URI, Node)

		// pre-interpreted literal type for pattern matching
		// it would also be reasonable to have an interpreted types to Int, Long, BigInt, etc...
		//but that would be one step further in interpretation
		enum LiteralI(text: String) {
			case Plain(text: String) extends LiteralI(text)
			case `@`(text: String, lang: Lang) extends LiteralI(text)
			case ^^(text: String, dataTp: URI) extends LiteralI(text)
		}

		//we need all implementations to have a given tripleOps available
		val Triple: TripleOps

		// does not work with Simple implementation below
		//given tripleTT: TypeTest[Any,Triple]

		/** Triple interface */
		trait TripleOps {
			def apply(subj: Node, rel: URI, obj: Node): Triple
			def unapply(t: Triple): Option[TripleI] = Some(untuple(t))
			def untuple(t: Triple): TripleI
			def subjectOf(triple: Triple): Node
			def relationOf(triple: Triple): URI
			def objectOf(triple: Triple): Node
		}

		extension (triple: Triple)
			def subj: Node = Triple.subjectOf(triple)
			def rel: URI = Triple.relationOf(triple)
			def obj: Node = Triple.objectOf(triple)

		//todo: we should add Relative URI type
		val URI: URIOps

		//		val Node : Node
		trait URIOps {
			/** (can) throw an exception (depending on implementation of URI)
			 * different implementations decide to parse at different points, and do
			 * varying quality jobs at that (check).
			 * Need to look at how capability based exceptions could help
			 * https://github.com/lampepfl/dotty/pull/11721/files */
			def apply(uriStr: String): URI = mkUri(uriStr).get
			def mkUri(iriStr: String): Try[URI]
			def asString(uri: URI): String
		}

		extension (uri: URI)
			def asString: String = URI.asString(uri)
			//and then a lot of other methods to get path, domain, etc...


		given uriTT: TypeTest[Any,URI]

//		val BNode : BNode
		val Literal: LiteralOps

		trait LiteralOps {
			import LiteralI.*
			def apply(plain: String): Literal
			def apply(lit: LiteralI): Literal = lit match
				case Plain(text) => apply(text)
				case `@`(text,lang) => langLiteral(text,lang)
				case `^^`(text,tp) => dtLiteral(text,tp)
			def unapply(lit: Literal): Option[LiteralI]
			def langLiteral(lex: String, lang: Lang): Literal
			def dtLiteral(lex: String, dataTp: URI): Literal
		}

		given literalTT: TypeTest[Any,Literal]

		val Lang: LangOps
		//todo Lang, should contain all the supported languages, plus an unsafe way of creating new ones
		trait LangOps {
			def apply(lang: String): Lang
			def label(lang: Lang): String
		}

		//this can be an external import
		object LiteralSyntax:
			extension (str: String)
				@targetName("dt")
				infix def ^^(dtType: URI): Literal = Literal.dtLiteral(str,dtType)
				@targetName("lang")
				infix def `@`(lang: Lang): Literal = Literal.langLiteral(str,lang)
		end LiteralSyntax

		extension (lang: Lang)
			def label: String =  Lang.label(lang)

		object LangLit {
			inline def apply(lex: String, lang: Lang): Literal = Literal.langLiteral(lex, lang)
		}
		object TypedLit {
			inline def apply(lex: String, dataTp: URI): Literal = Literal.dtLiteral(lex, dataTp)
		}

		// this method allows for multiple implementation of the Graph object
		val Graph : GraphOps
		trait GraphOps {
			def empty: Graph
			def apply(triples: Triple*): Graph
			def triplesIn(graph: Graph): Iterable[Triple]
		}

		extension (graph: Graph)
			def triples: Iterable[Triple] = Graph.triplesIn(graph)

	}

	object JenaRdf extends RDF {
		import org.apache.jena.graph as jena
		import org.apache.jena.graph.{NodeFactory, Factory}

		override opaque type Graph = jena.Graph
		override opaque type Triple = jena.Triple
		override opaque type Node = jena.Node
		override opaque type URI <: Node = jena.Node_URI
		override opaque type BNode <: Node = jena.Node_Blank
		override opaque type Literal <: Node = jena.Node_Literal
		override opaque type Lang = String

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

//		given tripleTT: TypeTest[Any,Triple] with {
//			import compiletime.asMatchable
//			override def unapply(s: Any): Option[s.type & Triple] = s.asMatchable match
//				case x: (s.type & jena.Triple) => Some(x)
//				case _ => None
//		}

		given uriTT: TypeTest[Any,URI] with {
			import compiletime.asMatchable
			override def unapply(s: Any): Option[s.type & jena.Node_URI] = s.asMatchable match
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

		given literalTT: TypeTest[Any,Literal] with {
			import compiletime.asMatchable
			override def unapply(s: Any): Option[s.type & Literal] = s.asMatchable match
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
			def triplesIn(graph: Graph): Iterable[Triple] =
				import org.apache.jena.graph.Node.ANY
				graph.find(ANY, ANY, ANY).nn.asScala.to(Iterable)
		}
	}

	object Simple extends RDF {
		override opaque type Triple = Tuple3[Node,URI,Node]
		override opaque type Node = java.net.URI|LiteralI|Int
		override opaque type URI <: Node = java.net.URI
		override opaque type BNode <: Node = Int
		override opaque type Literal <: Node = LiteralI
		override opaque type Graph = Set[Triple]
		override opaque type Lang = String

		override val Triple: TripleOps = new TripleOps {
			override inline def apply(subj: Node, rel: URI, obj: Node): Triple = (subj, rel, obj)
			override inline def untuple(t: Triple): (Node, URI, Node) = t
			override inline def subjectOf(triple: Triple): Node = triple._1
			override inline def relationOf(triple: Triple): URI = triple._2
			override inline def objectOf(triple: Triple): Node = triple._3
		}

		// this does not compile
//		given tripleTT: TypeTest[Any,Triple] with {
//			import compiletime.asMatchable
//			type N = java.net.URI|LiteralI|Int
//			import java.net.URI
//			override def unapply(s: Any): Option[s.type & Triple] =
//				s.asMatchable match
//				case x: (s.type & (N, java.net.URI, N)) => Some(x)
//				case _ => None
//		}

		given uriTT: TypeTest[Any,URI] with {
			import compiletime.asMatchable
			override def unapply(s: Any): Option[s.type & URI] = s.asMatchable match
				//note: this does now compile if we use URI instead of java.net.URI
				case x: (s.type & java.net.URI) => Some(x)
				case _ => None
		}

		override val URI : URIOps = new URIOps {
			//todo: this will throw an exception, should return Option
			// note: this implementation also parses the URI which Jena does not (I think).
			override inline def asString(uri: URI): String = uri.toString
			override inline def mkUri(iriStr: String): Try[URI] = Try(new java.net.URI(iriStr))
		}

		override val Literal: LiteralOps = new LiteralOps {
			import LiteralI.*
			def apply(plain: String): Literal = Plain(plain)
			def langLiteral(plain: String, lang: Lang): Literal = `@`(plain, lang)
			def dtLiteral(lex: String, dataTp: URI): Literal = `^^`(lex, dataTp)
			def unapply(lit: Literal): Option[LiteralI] = Some(lit)
		}

		given literalTT: TypeTest[Any,Literal] with {
			import compiletime.asMatchable
			override def unapply(s: Any): Option[s.type & LiteralI] = s.asMatchable match
				//note: this does not compile if we use URI instead of jena.Node_URI
				case x: (s.type & LiteralI) => Some(x)
				case _ => None
		}

		override val Lang: LangOps =  new LangOps {
			override inline def apply(lang: String): Lang = lang
			override inline def label(lang: Lang): String = lang
		}

		override val Graph: GraphOps = new GraphOps {
			override inline def empty: Graph = Set[Triple]()
			override inline def apply(triples: Triple*): Graph = Set(triples*)
			override inline def triplesIn(graph: Graph): Iterable[Triple] = graph
		}
	}

	object RDF {
		type Triple[R <: RDF] = R match
			case GetTriple[t] => t

		type Node[R <: RDF] = R match
			case GetNode[n] => n

		type URI[R <: RDF] = R match
			case GetURI[u] => u

		type Graph[R <: RDF] = R match
			case GetGraph[g] => g

		type Literal[R <: RDF] = R match
			case GetLiteral[l] => l

		type GetNode[N] = RDF { type Node = N }
		type GetLiteral[L] = RDF { type Literal = L }
		type GetURI[U] = RDF { type URI = U }
		type GetTriple[T] = RDF { type Triple = T }
		type GetGraph[G] = RDF { type Graph = G }
	}

	final case class PG[Rdf <: RDF](uri: RDF.Node[Rdf], graph: RDF.Graph[Rdf])
}

class MatchTypes extends munit.FunSuite {
	import MatchTypes.*

	def bbl(name: String): String = "https://bblfish.net/#" + name
	val bblStr = bbl("i")
	val anais = bbl("Anaïs")
	val timStr = "https://www.w3.org/People/Berners-Lee/card#i"
	def foaf[Rdf<:RDF](name: String)(using rdf: Rdf): rdf.URI = rdf.URI("http://xmlns.com/foaf/0.1/"+name)
	val knows = "http://xmlns.com/foaf/0.1/"+"knows"
	def xsd[Rdf<:RDF](name: String)(using rdf: Rdf): rdf.URI = rdf.URI("http://www.w3.org/2001/XMLSchema#"+name)

	test("Jena and Java instance test") {
		import MatchTypes.*

		val bblJna: RDF.URI[JenaRdf.type] = JenaRdf.URI(bblStr)
		val bblJva: RDF.URI[Simple.type] = Simple.URI(bblStr)

		//work with generic types classes
		import RDF.URI
		type Jena = JenaRdf.type
		type Simple = Simple.type

		import RDF.Triple
		val knowsJena: Triple[Jena] =
			JenaRdf.Triple(bblJna, JenaRdf.URI(knows), JenaRdf.URI(timStr))
		val knowsJava: Triple[Simple] =
			Simple.Triple(bblJva, Simple.URI(knows), Simple.URI(timStr))

		val henryLit: RDF.Literal[Jena] =
			JenaRdf.LangLit("Henry",JenaRdf.Lang("en"))
		val hlDeconstr: Option[JenaRdf.LiteralI] = JenaRdf.Literal.unapply(henryLit)
		assertEquals(hlDeconstr.get,JenaRdf.LiteralI.`@`("Henry", JenaRdf.Lang("en")))

		import RDF.Graph
		val grJena: Graph[Jena] = JenaRdf.Graph(knowsJena)
		val grJava: Graph[Simple] = Simple.Graph(knowsJava)

		val pgJena: PG[Jena] = PG(bblJna,grJena)
		val pgJava: PG[Simple] = PG(bblJva,grJava)

	}



//	import RDF.{Graph,URI}

	/**
	 * construct a Graph completely independent of any implementation.
	 * Note: here we don't seem to need the Graph[Rdf] notation.
	 * @param rdf The rdf implementation that comes with the methods (used to be RDFOps[Rdf])
	 * @tparam Rdf A subtype of RDF trait
	 * @return an rdf.Graph (note: returning an RDF.Graph[Rdf] does not work).
	 */
	def buildATestGraph[Rdf<:ideas.MatchTypes.RDF](using rdf: Rdf): rdf.Graph = {
		import rdf.{given,*}
		val bbl: URI = URI(bblStr)
		val tim: URI = URI(timStr)
		val bKt: Triple = Triple(bbl, foaf("knows"), URI(timStr))
		import compiletime.asMatchable
		import rdf.LiteralSyntax.*
		bKt match
			case Triple(sub: URI,rel,obj: URI) =>
				assertEquals(sub,bbl)
				assertEquals(rel, foaf("knows"))
				assertEquals(obj, URI(timStr))
			case _ => fail("subject and objects of this triple must be URIs")
		assertEquals(Triple(bKt.subj,bKt.rel,bKt.obj), bKt)
		val hname = Triple(bbl,foaf("name"),"Henry" `@` Lang("en"))
		val tname = Triple(tim,foaf("name"),Literal("Tim"))
		val anaisAge = Triple(URI(anais),foaf("age"),"7"^^xsd("int"))
		Graph(bKt,hname,tname,anaisAge)
	}

	def testGraph[Rdf <: ideas.MatchTypes.RDF](using rdf: Rdf)(
		name: String, g : rdf.Graph
	)(using loc: munit.Location): Unit = {
		test(name) {
			import rdf.{given,*}
			import LiteralI.*
			val Bbl: URI = URI(bblStr)
			val Tim: URI = URI(timStr)
			val Knows: URI = URI(knows)
			val Name: URI = foaf("name")
			val XsdInt: URI = xsd("int")
			g.triples.foreach { t =>
				import compiletime.asMatchable
				println("t="+t)
				t match
					case Triple(Bbl,Knows,Tim) => ()
					case Triple(Bbl,Name,Literal("Henry" `@` en)) =>
						assertEquals(en.label,"en")
					case Triple(Tim,Name,Literal(Plain("Tim")))	=> ()
					case Triple(s,r,Literal("7" `^^` XsdInt)) =>
						assertEquals(s,URI(anais))
						assertEquals(r,foaf("age"))
					case t => fail(s"triple $t does not match")
			}
		}
	}
	{
		given jg: ideas.MatchTypes.RDF  = JenaRdf
		type J = jg.type

		val g1: RDF.Graph[J] = buildATestGraph[J]
		testGraph[J]("Test graph in Jena", g1)
	}
	{
		given sg: ideas.MatchTypes.RDF  = Simple
		type S = sg.type

		val g2: RDF.Graph[S] = buildATestGraph[S]
		testGraph[S]("Test graph in Simple", g2)
	}
//	enum NodeType:
//		case Uri, Lit, BN
//
//	def nodeType[Rdf<:RDF](using rdf: Rdf)(node: rdf.Node): NodeType = {
//		node match
//		case u: rdf.BNode => NodeType.BN
//		case l: rdf.Literal => NodeType.Lit
//		case u: rdf.URI => NodeType.Uri
//	}

//	test("pattern matching on opaque types") {
//
//	}

}
