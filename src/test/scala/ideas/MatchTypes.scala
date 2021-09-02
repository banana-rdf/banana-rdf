package ideas

import org.apache.jena.graph.{Factory, NodeFactory}
import org.apache.jena.sparql.resultset.ResultSetCompare.BNodeIso

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

	trait RDF {
		type Graph
		type Triple
		type Node
		type URI <: Node
		type BNode <: Node
		type Literal <: Node

		//we need all implementations to have a given TripleI available
		implicit def tripleI: TripleI

		/** Triple interface */
		trait TripleI {
			def tuple(sibj: Node, rel: URI, obj: Node): Triple
			def untuple(t: Triple): (Node, URI, Node)
			def subjectOf(triple: Triple): Node
			def relationOf(triple: Triple): URI
			def objectOf(triple: Triple): Node
		}

		object Triple {
			def apply(subj: Node, rel: URI, obj: Node)(using tripleI: TripleI): Triple =
				tripleI.tuple(subj,rel,obj)
			def unapply(t: Triple)(using tripleI: TripleI): Some[(Node, URI, Node)] =
				Some(tripleI.untuple(t))
		}

		extension (triple: Triple)(using tripleI: TripleI)
			def subj: Node = tripleI.subjectOf(triple)
			def rel: URI = tripleI.relationOf(triple)
			def obj: Node = tripleI.objectOf(triple)

		//		val Node : Node
		val URI : URIOps
		trait URIOps {
			//todo: this will throw an exception, should return Option
			def apply(uriStr: String): URI
			/* it is questionable whether unapply on a URI to get the string is worthwhile.
			 * Using uri.stringValue or other methods would be a lot more efficient. Also
			 * other possibilities present themselves: should one return a deconstructed version
			 * of the URI (split into scheme, path, etc... )?
			 * I only add it here in order to test pattern matching on Triple(subj,URI(u),lit)
			 * It would make more sense for literals though.
			 **/
			def unapply(uri: URI): Option[String]
			def mkUri(iriStr: String): Try[URI]
		}
//		val BNode : BNode
//		val Literal : Literal

		def mkStringLit(str: String): Literal

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
		import org.apache.jena.{graph as jena}
		import org.apache.jena.graph.{NodeFactory, Factory}

		override opaque type Graph = jena.Graph
		override opaque type Triple = jena.Triple
		override opaque type Node = jena.Node
		override opaque type URI <: Node = jena.Node_URI
		override opaque type BNode <: Node = jena.Node_Blank
		override opaque type Literal <: Node = jena.Node_Literal

		/*private*/ given tripleI : TripleI with {
			override def untuple(t: Triple): (Node, URI, Node) =
				(t.getSubject, t.getPredicate.asInstanceOf[URI], t.getObject)
			override def tuple(subj: Node, rel: URI, obj: Node): Triple =
				jena.Triple.create(subj, rel, obj)
			override inline def subjectOf(triple: Triple): Node = triple.getSubject()
			override inline def relationOf(triple: Triple): URI = triple.getPredicate().asInstanceOf[URI]
			override inline def objectOf(triple: Triple): Node  = triple.getObject()
		}

		val URI : URIOps = new URIOps {
			//todo: this will throw an exception, should return Option
			override inline def apply(uriStr: String): URI = NodeFactory.createURI(uriStr).asInstanceOf[URI]
			override inline def mkUri(iriStr: String): Try[URI] = Try(NodeFactory.createURI(iriStr).asInstanceOf[URI])
		}

		override inline
		def mkStringLit(str: String): Literal =
			NodeFactory.createLiteral(str).asInstanceOf[Literal]

		val Graph = new GraphOps {
			override inline def empty: Graph = Factory.empty()
			override inline def apply(triples: Triple*): Graph =
				val graph = Factory.createDefaultGraph
				triples.foreach { triple =>
					graph.add(triple)
				}
				graph

			import scala.jdk.CollectionConverters.{given,*}
			def triplesIn(graph: Graph): Iterable[Triple] =
				import org.apache.jena.graph.Node.ANY
				graph.find(ANY, ANY, ANY).asScala.to(Iterable)
		}
	}

	object Simple extends RDF {
		override opaque type Triple = Tuple3[Node,URI,Node]
		override opaque type Node = java.net.URI|String|Int
		override opaque type URI <: Node = java.net.URI
		override opaque type BNode <: Node = Int
		override opaque type Literal <: Node = String
		override opaque type Graph = Set[Triple]

		given tripleI : TripleI with {
			override inline def untuple(t: (Triple & Matchable)): (Node, URI, Node) = t
			override inline def tuple(subj: Node, rel: URI, obj: Node): Triple = (subj, rel, obj)
			override inline def subjectOf(triple: Triple): Node = triple._1
			override inline def relationOf(triple: Triple): URI = triple._2
			override inline def objectOf(triple: Triple): Node = triple._3
		}

		val URI : URIOps = new URIOps {
			//todo: this will throw an exception, should return Option
			// note: this implementation also parses the URI which Jena does not (I think).
			override inline def apply(uriStr: String): URI = new java.net.URI(uriStr)
			override inline def unapply(uri: URI): Option[String] = Some(uri.toString)
			override inline def mkUri(iriStr: String): Try[URI] = Try(new java.net.URI(iriStr))
		}

		override inline
		def mkStringLit(str: String): Literal = str

		override val Graph = new GraphOps {
			override inline def empty: Graph = Set[Triple]()
			override inline def apply(triples: Triple*): Graph = Set(triples*)
			override inline def triplesIn(graph: Graph): Iterable[Triple] = graph
		}

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
	def buildATestGraph[Rdf<:RDF](using rdf: Rdf): rdf.Graph = {
		import rdf.{given,*}
		val bbl: URI = URI(bblStr)
		val bKt: Triple = Triple(bbl, URI(knows), URI(timStr))
		import compiletime.asMatchable
		bKt.asMatchable match
			case Triple(sub,URI(relStr),obj) =>
				assertEquals(sub,bbl)
				assertEquals(rel,relStr)
			//case _ => fail("triple did not match")
		assertEquals(Triple(bKt.subj,bKt.rel,bKt.obj), bKt)
		Graph(bKt)
	}

	test("Build a graph in Jena") {
		given rdf: JenaRdf.type = JenaRdf
		val g: rdf.Graph = buildATestGraph[JenaRdf.type]
	}

	test("Build a graph with Simple") {
		given rdf: Simple.type = Simple
		val g: rdf.Graph = buildATestGraph[Simple.type]
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
