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
object RDF:

	type rTriple[R <: RDF] = R match
		case GetRelTriple[t] => t

	type Triple[R <: RDF] <: Matchable = R match
		case GetTriple[t] => t

	type rNode[R <: RDF] <: Matchable = R match
		case GetRelNode[n] => n

	type Node[R <: RDF] <: Matchable = R match
		case GetNode[n] => n

	type BNode[R <: RDF] = R match
		case GetBNode[bn] => bn

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
	type GetBNode[N <: Matchable] = RDF { type BNode = N }
	type GetLiteral[L <: Matchable] = RDF { type Literal = L }
	type GetLang[L <: Matchable] = RDF { type Lang = L }
	type GetRelTriple[T] = RDF { type rTriple = T }
	type GetTriple[T <: Matchable] = RDF { type Triple = T }
	type GetRelGraph[G] = RDF { type rGraph = G }
	type GetGraph[G] = RDF { type Graph = G }

	/**
	 * these associate a type to the positions in statements (triples or quads)
	 * These are not agreed to by all frameworks, so it would be useful to find a way to parametrise
	 * them. Essentially some (Jena?) allow a literal in Subject position (which is useful for reasoning
	 * and later n3) and others are stricter, which makes them map better to many syntaxes, except N3.
	 *
	 * For the moment I will try the strict mode.
	 **/
	object Statement:
		type Subject[R <: RDF] = URI[R] | BNode[R]
		type Relation[R <: RDF] = URI[R]
		type Object[R <: RDF] = URI[R] | BNode[R] | Literal[R]
		type Graph[R <: RDF] = URI[R] | BNode[R]
	end Statement

	// relative statements
	object rStatement:
		type Subject[R <: RDF] = rURI[R] | BNode[R] | URI[R]
		type Relation[R <: RDF] = rURI[R]
		type Object[R <: RDF] = rURI[R] | BNode[R] | Literal[R] | URI[R]
		type Graph[R <: RDF] = rURI[R] | BNode[R] | URI[R]
	end rStatement

end RDF


