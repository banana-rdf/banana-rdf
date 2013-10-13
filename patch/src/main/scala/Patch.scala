package org.w3.banana

/* the grammar for an LDP PATCH */

case class Patch[Rdf <: RDF](
  delete: Option[Delete[Rdf]],
 insert: Option[Insert[Rdf]],
  where: Option[Where[Rdf]]) {
  val whereVars: Set[Var[Rdf]] = where.map(_.pattern.vars).getOrElse(Set.empty)
  val insertVars: Set[Var[Rdf]] = insert.map(_.pattern.vars).getOrElse(Set.empty)
  val deleteVars: Set[Var[Rdf]] = delete.map(_.pattern.vars).getOrElse(Set.empty)
  assert(delete.isDefined || insert.isDefined, "At least one of the DELETE or INSERT clauses must be defined")
  assert(insertVars subsetOf whereVars, "The variables " + (insertVars -- whereVars).mkString("{", ", ", "}") + " appear in the INSERT clause but are not bound in the WHERE clause")
  assert(deleteVars subsetOf whereVars, "The variables " + (deleteVars -- whereVars).mkString("{", ", ", "}") + " appear in the DELETE clause but are not bound in the WHERE clause")
  assert(where.map(_.pattern.isTreePattern).getOrElse(true), "The BGP in the WHERE clause must be a Tree pattern")

}

case class Delete[Rdf <: RDF](pattern: TriplesPattern[Rdf])

case class Insert[Rdf <: RDF](pattern: TriplesPattern[Rdf])

case class Where[Rdf <: RDF](pattern: TriplesBlock[Rdf])

case class TriplesPattern[Rdf <: RDF](triples: Vector[TriplePattern[Rdf]]) {

  def vars: Set[Var[Rdf]] =
    triples.foldLeft(Set.empty[Var[Rdf]]){ case (acc, TriplePattern(s, p, o)) =>
      var set = Set.empty[Var[Rdf]]
      s match { case v: Var[Rdf] => set += v ; case _ => () }
      p match { case v: Var[Rdf] => set += v ; case _ => () }
      o match { case v: Var[Rdf] => set += v ; case _ => () }
      acc ++ set
    }

}

case class TriplesBlock[Rdf <: RDF](triples: Vector[TriplePath[Rdf]]) {

  def isTreePattern: Boolean = try {
    Forest(triples).isSingleTree
  } catch { case e: Exception =>
    false
  }

  def vars: Set[Var[Rdf]] =
    triples.foldLeft(Set.empty[Var[Rdf]]){ case (acc, TriplePath(s, verb, o)) =>
      var set = Set.empty[Var[Rdf]]
      s match { case v: Var[Rdf] => set += v ; case _ => () }
      verb match { case v: Var[Rdf] => set += v ; case _ => () }
      o match { case v: Var[Rdf] => set += v ; case _ => () }
      acc ++ set
    }

}

case class TriplePattern[Rdf <: RDF](s: VarOrTerm[Rdf], p: VarOrIRIRef[Rdf], o: VarOrTerm[Rdf])

case class TriplePath[Rdf <: RDF](s: VarOrTerm[Rdf], verb: Verb[Rdf], o: VarOrTerm[Rdf])

sealed trait Verb[Rdf <: RDF]

sealed trait VarOrIRIRef[Rdf <: RDF]

case class IRIRef[Rdf <: RDF](uri: Rdf#URI) extends VarOrIRIRef[Rdf] with Verb[Rdf] {
  override def toString: String = uri.toString
}

case class Path[Rdf <: RDF](pathSequence: List[Rdf#URI]) extends Verb[Rdf]

sealed trait VarOrTerm[Rdf <: RDF]

case class Var[Rdf <: RDF](label: String) extends VarOrTerm[Rdf] with VarOrIRIRef[Rdf] with VarPattern[Rdf] with Verb[Rdf] {
  override def toString: String = "?" + label
}

case class Term[Rdf <: RDF](node: Rdf#Node) extends VarOrTerm[Rdf]

/** not part of the PATCH grammar but used as the domain for the
  * Pattern Instance Mapping */
sealed trait VarPattern[Rdf <: RDF]

case class VarBNode[Rdf <: RDF](bnode: Rdf#BNode) extends VarPattern[Rdf]
