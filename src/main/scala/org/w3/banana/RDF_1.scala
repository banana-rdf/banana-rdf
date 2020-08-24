//package org.w3.banana
//
//class RDF[Rdf <: RDF[Rdf]](using 
//  val g: GraphTyp[Rdf], val t: TripleTyp[Rdf], val n: NodeTyp[Rdf],
//  val uri: URITyp[Rdf], val bn: BNodeTyp[Rdf], val lit: LiteralTyp[Rdf],
//  val lang: LangTyp[Rdf], val mg: MGraphTyp[Rdf], val nm: NodeAnyTyp[Rdf],
//  val any: NodeMatchTyp[Rdf], val qu: QueryTyp[Rdf], val sq: SelectQueryTyp[Rdf],
//  val cons: ConstructQueryTyp[Rdf], val ask: AskQueryTyp[Rdf], val upd: UpdateQueryTyp[Rdf],
//  val sol: SolutionTyp[Rdf], val sols: SolutionsTyp[Rdf]
//) {
//  type Graph = g.Out
//  type Triple = t.Out
//  type Node = n.Out
//  type URI  = uri.Out 
//  type BNode = bn.Out 
//  type Literal = lit.Out 
//  type Lang = lang.Out 
//
//  // mutable graphs
//  type MGraph = mg.Out 
//
//  // types for the graph traversal API
//  type NodeMatch = nm.Out 
//  type NodeAny = any.Out 
//
//  // types related to Sparql
//  type Query = qu.Out 
//  type SelectQuery  = sq.Out 
//  type ConstructQuery  = cons.Out 
//  type AskQuery = ask.Out 
//  type UpdateQuery = upd.Out 
//  type Solution = sol.Out 
//  type Solutions = sols.Out 
//
//}
//
//trait GraphTyp[Rdf <: RDF[Rdf]] { type Out }
//
//trait TripleTyp[Rdf <: RDF[Rdf]] { type Out }
//
//trait NodeTyp[Rdf <: RDF[Rdf]] { type Out }
//
//trait URITyp[Rdf <: RDF[Rdf]] extends NodeTyp[Rdf] { type Out }
//trait BNodeTyp[Rdf <: RDF[Rdf]] extends NodeTyp[Rdf] { type Out }
//trait LiteralTyp[Rdf <: RDF[Rdf]] extends NodeTyp[Rdf] { type Out }
//
//trait LangTyp[Rdf <: RDF[Rdf]]  { type Out }
//
//// mutable graphs
//trait MGraphTyp[Rdf <: RDF[Rdf]] { type Out }
//
//// types for the graph traversal API
//trait NodeMatchTyp[Rdf <: RDF[Rdf]]  { type Out }
//trait NodeAnyTyp[Rdf <: RDF[Rdf]] extends NodeMatchTyp[Rdf] { type Out }
//
//// types related to Sparql
//trait QueryTyp[Rdf <: RDF[Rdf]]  { type Out }
//trait SelectQueryTyp[Rdf <: RDF[Rdf]] extends QueryTyp[Rdf] { type Out }
//trait ConstructQueryTyp[Rdf <: RDF[Rdf]] extends QueryTyp[Rdf] { type Out }
//trait AskQueryTyp[Rdf <: RDF[Rdf]] extends QueryTyp[Rdf] { type Out }
//trait UpdateQueryTyp[Rdf <: RDF[Rdf]]  { type Out }
//trait SolutionTyp[Rdf <: RDF[Rdf]]  { type Out }
//trait SolutionsTyp[Rdf <: RDF[Rdf]]  { type Out }