package ideas

import ideas.MatchTypes.Java
import ideas.MatchTypes.Java.URI
import ideas.MatchTypes.RDF.GetNode
import org.apache.jena.graph.Graph

/**
 * following an idea by Neko-kai https://twitter.com/kai_nyasha
 *  https://discord.com/channels/632277896739946517/632277897448652844/880944909660606525
 */
object MatchTypes {

	trait RDF {
		type Graph
		type Triple
		type Node
		type URI <: Node
		type BNode <: Node
		type Literal <: Node

		def mkUri(iriStr: String): URI
		def mkStringLit(str: String): Literal
		def mkTriple(subj: Node, rel: URI, obj: Node): Triple
		def mkGraph(triples: Iterable[Triple]): Graph

	}

	object JenaRdf extends RDF {
		import org.apache.jena.graph
		import graph.{NodeFactory, Node_Blank, Node_Literal, Node_URI, Node as JenaNode}
		import graph.{Triple as JenaTriple, Graph as JGraph, Factory}

		override opaque type Graph = JGraph
		override opaque type Triple = JenaTriple
		override opaque type Node = JenaNode
		override opaque type URI <: Node = Node_URI
		override opaque type BNode <: Node = Node_Blank
		override opaque type Literal <: Node = Node_Literal

		override def mkUri(iriStr: String): URI =
			NodeFactory.createURI(iriStr).asInstanceOf[URI]
		override def mkStringLit(str: String): Literal =
			NodeFactory.createLiteral(str).asInstanceOf[Literal]
		override def mkTriple(subj: Node, rel: URI, obj: Node): Triple =
			JenaTriple.create(subj, rel, obj)
		override def mkGraph(triples: Iterable[Triple]): Graph =
			val g = Factory.createDefaultGraph()
			triples.foreach(t => g.add(t))
			g
	}

	object Java extends RDF {
		override opaque type Triple = Tuple3[Node,URI,Node]
		override opaque type Node = java.net.URI|String|Int
		override opaque type URI <: Node = java.net.URI
		override opaque type BNode <: Node = Int
		override opaque type Literal <: Node = String
		override opaque type Graph = Set[Triple]

		override def mkUri(iriStr: String): URI = new java.net.URI(iriStr)
		override def mkStringLit(str: String): String = str
		override def mkTriple(subj: Node, rel: URI, obj: Node): Triple =
			(subj,rel,obj)
		override def mkGraph(triples: Iterable[Triple]): Graph =
			triples.toSet
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

object Opaque {
	opaque type Meter = Double
	def Meter(x: Double): Meter = x

	opaque type Second = Double
	def Second(x: Double): Second = x
}

class MatchTypes extends munit.FunSuite {
	import MatchTypes.*


	test("Jena and Java instance test") {
		import MatchTypes.*
		val bblStr = "https://bblfish.net/#i"
		val timStr = "https://www.w3.org/People/Berners-Lee/card#i"
		val knows = "http://xmlns.com/foaf/0.1/knows"
		val name = "http://xmlns.com/foaf/0.1/name"

		val bblJna: RDF.URI[JenaRdf.type] = JenaRdf.mkUri(bblStr)
		val bblJva: RDF.URI[Java.type] = Java.mkUri("bblStr")

		//work with generic types classes
		import RDF.URI
		type Jena = JenaRdf.type
		type Java = Java.type

		import RDF.Triple
		val knowsJena: Triple[Jena] =
			JenaRdf.mkTriple(bblJna, JenaRdf.mkUri(knows), JenaRdf.mkUri(timStr))
		val knowsJava: Triple[Java] =
			Java.mkTriple(bblJva, Java.mkUri(knows), Java.mkUri(timStr))

		import RDF.Graph
		val grJena: Graph[Jena] = JenaRdf.mkGraph(Seq(knowsJena))
		val grJava: Graph[Java] = Java.mkGraph(Seq(knowsJava))

		val pgJena: PG[Jena] = PG(bblJna,grJena)
		val pgJava: PG[Java] = PG(bblJva,grJava)
	}
}
