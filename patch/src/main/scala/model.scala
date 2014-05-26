package org.w3.banana.ldpatch.model

import org.w3.banana._

case class LDPatch[Rdf <: RDF](statements: Seq[Statement[Rdf]])

sealed trait Statement[Rdf <: RDF]

case class Add[Rdf <: RDF](s: Subject[Rdf], p: Predicate[Rdf], o: Object[Rdf]) extends Statement[Rdf]
case class AddList[Rdf <: RDF](s: Subject[Rdf], p: Predicate[Rdf], list: Seq[Object[Rdf]]) extends Statement[Rdf]
case class Delete[Rdf <: RDF](s: Subject[Rdf], p: Predicate[Rdf], o: Object[Rdf]) extends Statement[Rdf]
case class Bind[Rdf <: RDF](varr: Var, startingNode: Value[Rdf], path: Path[Rdf]) extends Statement[Rdf]
case class Replace[Rdf <: RDF](s: Subject[Rdf], p: Predicate[Rdf], slice: Slice, list: Seq[Object[Rdf]]) extends Statement[Rdf]

case class Path[Rdf <: RDF](elems: Seq[PathElement[Rdf]])

sealed trait PathElement[+Rdf <: RDF]
 sealed trait Step[+Rdf <: RDF] extends PathElement[Rdf]
  case class StepForward[Rdf <: RDF](predicate: Predicate[Rdf]) extends Step[Rdf]
  case class StepBackward[Rdf <: RDF](predicate: Predicate[Rdf]) extends Step[Rdf]
  case class StepAt(index: Int) extends Step[Nothing]
 sealed class Constraint[+Rdf <: RDF] extends PathElement[Rdf]
  case class Filter[Rdf <: RDF](path: Path[Rdf], valueOpt: Option[Value[Rdf]]) extends Constraint[Rdf]
  case object UnicityConstraint extends Constraint[Nothing]

sealed trait Slice
case class Range(leftIndex: Int, rightIndex: Int) extends Slice
case class EverythingBefore(index: Int) extends Slice
case class EverythingAfter(index: Int) extends Slice
case object End extends Slice

sealed trait Subject[+Rdf <: RDF]
sealed trait Predicate[Rdf <: RDF]
sealed trait Object[+Rdf <: RDF]
sealed trait Value[+Rdf <: RDF]

case class PatchIRI[Rdf <: RDF](uri: Rdf#URI) extends Subject[Rdf] with Predicate[Rdf] with Object[Rdf] with Value[Rdf]
case class PatchBNode[Rdf <: RDF](bnode: Rdf#BNode) extends Subject[Rdf] with Object[Rdf]
case class PatchLiteral[Rdf <: RDF](literal: Rdf#Literal) extends Object[Rdf] with Value[Rdf]
case class Var(label: String) extends Subject[Nothing] with Object[Nothing] with Value[Nothing]
