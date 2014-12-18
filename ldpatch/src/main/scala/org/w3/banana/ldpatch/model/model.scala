package org.w3.banana.ldpatch.model

import org.w3.banana._

import scalaz.Equal

// note: be careful when comparing elements in this model using
// .equals(). bnodes should be compared with a known mapping created
// at parsing time

case class LDPatch[Rdf <: RDF](statements: Seq[Statement[Rdf]])

sealed trait Statement[+Rdf <: RDF]

sealed trait Mode
case object Strict extends Mode
case object Lax extends Mode

case class Add[Rdf <: RDF](mode: Mode, triples: Vector[Triple[Rdf]]) extends Statement[Rdf]
case class Delete[Rdf <: RDF](mode: Mode, triples: Vector[Triple[Rdf]]) extends Statement[Rdf]
case class Bind[Rdf <: RDF](varr: Var, startingNode: VarOrConcrete[Rdf], path: Path[Rdf]) extends Statement[Rdf]
case class Cut(varr: Var) extends Statement[Nothing]
case class UpdateList[Rdf <: RDF](s: VarOrConcrete[Rdf], p: Rdf#URI, slice: Slice, head: Rdf#Node, triples: Vector[Triple[Rdf]]) extends Statement[Rdf]

object Add {
  def apply[Rdf <: RDF](triples: Vector[Triple[Rdf]]): Add[Rdf] = Add(Lax, triples)
}

object AddNew {
  def apply[Rdf <: RDF](triples: Vector[Triple[Rdf]]): Add[Rdf] = Add(Strict, triples)
}

object Delete {
  def apply[Rdf <: RDF](triples: Vector[Triple[Rdf]]): Delete[Rdf] = Delete(Lax, triples)
}

object DeleteExisting {
  def apply[Rdf <: RDF](triples: Vector[Triple[Rdf]]): Delete[Rdf] = Delete(Strict, triples)
}

case class Triple[Rdf <: RDF](s: VarOrConcrete[Rdf], p: Rdf#URI, o: VarOrConcrete[Rdf])

case class Path[Rdf <: RDF](elems: Seq[PathElement[Rdf]])

sealed trait PathElement[+Rdf <: RDF]
 sealed trait Step[+Rdf <: RDF] extends PathElement[Rdf]
  case class StepForward[Rdf <: RDF](predicate: Rdf#URI) extends Step[Rdf]
  case class StepBackward[Rdf <: RDF](predicate: Rdf#URI) extends Step[Rdf]
  case class StepAt(index: Int) extends Step[Nothing]
 sealed trait Constraint[+Rdf <: RDF] extends PathElement[Rdf]
  case class Filter[Rdf <: RDF](path: Path[Rdf], valueOpt: Option[VarOrConcrete[Rdf]]) extends Constraint[Rdf]
  case object UnicityConstraint extends Constraint[Nothing]

sealed trait Slice
case class Range(leftIndex: Int, rightIndex: Int) extends Slice
case class EverythingAfter(index: Int) extends Slice
case object End extends Slice

sealed trait VarOrConcrete[+Rdf <: RDF]
case class Concrete[Rdf <: RDF](node: Rdf#Node) extends VarOrConcrete[Rdf]
case class Var(label: String) extends VarOrConcrete[Nothing]
