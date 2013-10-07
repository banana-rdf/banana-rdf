package org.w3.banana

case class LDPPatch[Rdf <: RDF](
  delete: Option[Delete[Rdf]],
  insert: Option[Insert[Rdf]],
  where: Option[Where[Rdf]])
case class Delete[Rdf <: RDF](block: TriplesBlock[Rdf])
case class Insert[Rdf <: RDF](block: TriplesBlock[Rdf])
case class Where[Rdf <: RDF](block: TriplesBlock[Rdf])
case class TriplesBlock[Rdf <: RDF](triples: Vector[TriplePattern[Rdf]])
case class TriplePattern[Rdf <: RDF](s: VarOrTerm[Rdf], p: Rdf#URI, o: VarOrTerm[Rdf])
sealed trait VarOrTerm[Rdf <: RDF]
case class Var[Rdf <: RDF](label: String) extends VarOrTerm[Rdf]
case class Term[Rdf <: RDF](node: Rdf#Node) extends VarOrTerm[Rdf]
