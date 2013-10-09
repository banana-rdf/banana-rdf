package org.w3.banana

/* the grammar for an LDP PATCH */

case class Patch[Rdf <: RDF](
  delete: Option[Delete[Rdf]],
  insert: Option[Insert[Rdf]],
  where: Option[Where[Rdf]])
case class Delete[Rdf <: RDF](block: TriplesBlock[Rdf])
case class Insert[Rdf <: RDF](block: TriplesBlock[Rdf])
case class Where[Rdf <: RDF](block: TriplesBlock[Rdf])
case class TriplesBlock[Rdf <: RDF](triples: Vector[TriplePattern[Rdf]])
case class TriplePattern[Rdf <: RDF](s: VarOrTerm[Rdf], p: VarOrIRIRef[Rdf], o: VarOrTerm[Rdf])
sealed trait VarOrIRIRef[Rdf <: RDF]
case class IRIRef[Rdf <: RDF](uri: Rdf#URI) extends VarOrIRIRef[Rdf]
sealed trait VarOrTerm[Rdf <: RDF]
case class Var[Rdf <: RDF](label: String) extends VarOrTerm[Rdf] with VarOrIRIRef[Rdf] with VarPattern[Rdf]
case class Term[Rdf <: RDF](node: Rdf#Node) extends VarOrTerm[Rdf]

/** not part of the PATCH grammar but used as the domain for the
  * Pattern Instance Mapping */
sealed trait VarPattern[Rdf <: RDF]
case class VarBNode[Rdf <: RDF](bnode: Rdf#BNode) extends VarPattern[Rdf]
