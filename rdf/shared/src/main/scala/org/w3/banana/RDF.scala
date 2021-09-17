package org.w3.banana

import scala.annotation.targetName
import scala.reflect.TypeTest
import scala.util.Try

/**
 * Main RDF types.
 * Implementations will mostly use opaque types, so we need to provide the operations too.
 * todo: how can one modularise this, while taking into account that implementations will
 *   be using opaque types?
 *
 * rURI, rTriple, rGraph are all possibly relative versions of respectively a URI, Triple and Graph.
 * Relative Graphs have fewer applicable methods: they cannot be unioned with another graph for example.
 */
trait RDF:
	rdf =>
	type R = rdf.type

	type rGraph 					// graphs with triples with relative URLs
	type rTriple					// triples with relative URLs
	type rNode = rURI | Node  	// relative node
	type rURI 					   // relative URLs

	type Graph     			   // graphs with no triples with relative URLs
	type Triple <: Matchable	// triples with no relative URLs
	type Node <: Matchable
	type URI <: Node
	type BNode <: Node
	type Literal <: Node
	type Lang <: Matchable


//		def ANY: NodeAny
//		implicit def toConcreteNodeMatch(node: Rdf#Node): Rdf#NodeMatch
//		def foldNodeMatch[T](nodeMatch: Rdf#NodeMatch)(funANY: => T, funNode: Rdf#Node => T): T
//		def find(graph: Rdf#Graph, subject: Rdf#NodeMatch, predicate: Rdf#NodeMatch, objectt: Rdf#NodeMatch): Iterator[Rdf#Triple]


	given ops: Ops[R]

end RDF


// remain to be done:
//  // mutable graphs
//  type MGraph <: AnyRef
//
//  // types for the graph traversal API
//  type NodeMatch
//  type NodeAny <: NodeMatch
//
//  // types related to Sparql
//  type Query
//  type SelectQuery <: Query
//  type ConstructQuery <: Query
//  type AskQuery <: Query
//  type UpdateQuery
//  type Solution
//  type Solutions

/**
 * The idea of using match types by @neko-kai
 * https://github.com/lampepfl/dotty/issues/13416
 */
object RDF {

	type rTriple[R <: RDF] = R match
		case GetRelTriple[t] => t

	type Triple[R <: RDF] <: Matchable = R match
		case GetTriple[t] => t

	type rNode[R <: RDF] <: Matchable = R match
		case GetRelNode[n] => n

	type Node[R <: RDF] <: Matchable = R match
		case GetNode[n] => n

	type rURI[R <: RDF] = R match
		case GetRelURI[ru] => ru

	type URI[R <: RDF] = R match
		case GetURI[u] => u

	type rGraph[R <: RDF] = R match
		case GetRelGraph[g] => g

	type Graph[R <: RDF] = R match
		case GetGraph[g] => g

	type Literal[R <: RDF] <: Matchable = R match
		case GetLiteral[l] => l

	type Lang[R <: RDF] <: Matchable = R match
		case GetLang[l] => l

	type GetRelURI[U] = RDF { type rURI = U }
	type GetURI[U] = RDF { type URI = U }
	type GetRelNode[N <: Matchable] = RDF { type rNode = N }
	type GetNode[N <: Matchable] = RDF { type Node = N }
	type GetLiteral[L <: Matchable] = RDF { type Literal = L }
	type GetLang[L <: Matchable] = RDF { type Lang = L }
	type GetRelTriple[T] = RDF { type rTriple = T }
	type GetTriple[T <: Matchable] = RDF { type Triple = T }
	type GetRelGraph[G] = RDF { type rGraph = G }
	type GetGraph[G] = RDF { type Graph = G }

}


