package ideas

import org.apache.jena.sparql.resultset.ResultSetCompare.BNodeIso


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

	trait RDF {
		type Graph
		type Triple
		type Node
		type URI <: Node
		type BNode <: Node
		type Literal <: Node

//		val Graph : ...
		val Triple : TripleOps
		trait TripleOps {
			def apply(subj: Node, rel: URI, obj: Node): Triple
			def unapply(triple: Triple): Option[(Node,URI,Node)]
			def subjectOf(triple: Triple): Node
			def relationOf(triple: Triple): URI
			def objectOf(triple: Triple): Node

			extension (triple: Triple)
				def subj: Node = Triple.subjectOf(triple)
				def rel: URI = Triple.relationOf(triple)
				def obj: Node = Triple.relationOf(triple)
		}

		//		val Node : Node
//		val URI : URI
//		val BNode : BNode
//		val Literal : Literal

		def mkUri(iriStr: String): URI
		def mkStringLit(str: String): Literal
		def mkGraph(triples: Iterable[Triple]): Graph
	}

	object JenaRdf extends RDF {
		import org.apache.jena.{graph as jena}
		import org.apache.jena.graph.{NodeFactory, Factory}

		override opaque type Graph = jena.Graph
		override opaque type Triple = jena.Triple
		override opaque type Node = jena.Node
		override opaque type URI <: Node = jena.Node_URI
		override opaque type BNode <: Node = jena.Node_Blank
		override opaque type Literal <: Node = jena.Node_Literal

		override val Triple: TripleOps = new TripleOps {
			override inline def apply(subj: Node, rel: URI, obj: Node): Triple =
				jena.Triple.create(subj, rel, obj)
			override def unapply(triple: Triple): Some[(Node, URI, Node)] =
				Some((triple.getSubject,triple.getPredicate.asInstanceOf[URI],triple.getObject))

			override inline def subjectOf(triple: Triple): Node = triple.getSubject()
			override inline def relationOf(triple: Triple): URI = triple.getPredicate().asInstanceOf[URI]
			override inline def objectOf(triple: Triple): Node = triple.getObject()
		}

		override inline
		def mkUri(iriStr: String): URI =
			NodeFactory.createURI(iriStr).asInstanceOf[URI]

		override inline
		def mkStringLit(str: String): Literal =
			NodeFactory.createLiteral(str).asInstanceOf[Literal]

		override inline
		def mkGraph(triples: Iterable[Triple]): Graph =
			val g = Factory.createDefaultGraph()
			triples.foreach(t => g.add(t))
			g
	}

	object Simple extends RDF {
		override opaque type Triple = Tuple3[Node,URI,Node]
		override opaque type Node = java.net.URI|String|Int
		override opaque type URI <: Node = java.net.URI
		override opaque type BNode <: Node = Int
		override opaque type Literal <: Node = String
		override opaque type Graph = Set[Triple]

		override val Triple: TripleOps = new TripleOps {
			override inline def apply(subj: Node, rel: URI, obj: Node): (Node, URI, Node) = (subj, rel, obj)
			override inline def subjectOf(triple: Triple): Node = triple._1
			override inline def relationOf(triple: Triple): URI = triple._2
			override inline def objectOf(triple: Triple): Node = triple._3
			override inline def unapply(triple: Triple): Option[(Node, URI, Node)] = Some(triple)
		}

		override inline
		def mkUri(iriStr: String): URI = new java.net.URI(iriStr)
		override inline
		def mkStringLit(str: String): Literal = str
		override inline
		def mkGraph(triples: Iterable[Triple]): Graph = triples.toSet
	}

	object RDF {
		type Triple[R <: RDF] = R match
			case GetTriple[t] => t

		type Node[R<:RDF] = R match
			case GetNode[n] => n

		type URI[R<:RDF] = R match
			case GetURI[u] => u

		type Graph[R <: RDF] = R match
			case GetGraph[g] => g

		type GetNode[N] = RDF { type Node = N }
		type GetURI[U] = RDF { type URI = U }
		type GetTriple[T] = RDF { type Triple = T }
		type GetGraph[G] = RDF { type Graph = G }
	}

	final case class PG[Rdf <: RDF](uri: RDF.Node[Rdf], graph: RDF.Graph[Rdf])
}

class MatchTypes extends munit.FunSuite {
	import MatchTypes.*

	val bblStr = "https://bblfish.net/#i"
	val timStr = "https://www.w3.org/People/Berners-Lee/card#i"
	val knows = "http://xmlns.com/foaf/0.1/knows"
	val name = "http://xmlns.com/foaf/0.1/name"

	test("Jena and Java instance test") {
		import MatchTypes.*

		val bblJna: RDF.URI[JenaRdf.type] = JenaRdf.mkUri(bblStr)
		val bblJva: RDF.URI[Simple.type] = Simple.mkUri("bblStr")

		//work with generic types classes
		import RDF.URI
		type Jena = JenaRdf.type
		type Simple = Simple.type

		import RDF.Triple
		val knowsJena: Triple[Jena] =
			JenaRdf.Triple(bblJna, JenaRdf.mkUri(knows), JenaRdf.mkUri(timStr))
		val knowsJava: Triple[Simple] =
			Simple.Triple(bblJva, Simple.mkUri(knows), Simple.mkUri(timStr))

		import RDF.Graph
		val grJena: Graph[Jena] = JenaRdf.mkGraph(Seq(knowsJena))
		val grJava: Graph[Simple] = Simple.mkGraph(Seq(knowsJava))

		val pgJena: PG[Jena] = PG(bblJna,grJena)
		val pgJava: PG[Simple] = PG(bblJva,grJava)
	}

	import RDF.{Graph,URI,Triple}

	//next we want to develop methods that are completely independent of the implementation
	def buildGraph[Rdf<:RDF](using rdf: Rdf): RDF.Graph[rdf.type] = {
		type Rdf = rdf.type
		val bbl: URI[Rdf] = rdf.mkUri(bblStr)
		val bKt: Triple[Rdf] = rdf.Triple(bbl, rdf.mkUri(knows), rdf.mkUri(timStr))
		import compiletime.asMatchable
		bKt.asMatchable match
			case rdf.Triple(sub,rel,obj) => assertEquals(sub,bbl)
			case _ => fail("triple did not match")
		import rdf.Triple.*
		assertEquals(bKt.subj,bbl)
		rdf.mkGraph(Seq(bKt))
	}

	test("Build a graph in Jena") {
		given rdf: JenaRdf.type = JenaRdf
		val g: rdf.Graph = buildGraph[JenaRdf.type]
	}

	test("Build a graph with Simple") {
		given rdf: Simple.type = Simple
		val g: rdf.Graph = buildGraph[Simple.type]
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
