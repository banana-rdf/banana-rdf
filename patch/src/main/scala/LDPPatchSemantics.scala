package org.w3.banana

import scala.util.Try

//case class InstanceMapping[Rdf <: RDF](map: Map[Rdf#BNode, Rdf#Node])

case class SolutionMapping[Rdf <: RDF](map: Map[Var[Rdf], Rdf#Node]) {

  def apply(tp: TriplePattern[Rdf])(ops: RDFOps[Rdf]): Rdf#Triple = {
    def toNode(vt: VarOrTerm[Rdf]): Rdf#Node = vt match {
      case v: Var[Rdf] => map(v)
      case Term(node)  => node
    }
    ops.Triple(toNode(tp.s), tp.p, toNode(tp.o))
  }

}

//case class PatternInstanceMapping[Rdf <: RDF](σ: InstanceMapping[Rdf], μ: SolutionMapping[Rdf])

object ResultSet {
  def empty[Rdf <: RDF]: ResultSet[Rdf] = ResultSet[Rdf](Set.empty, Vector.empty)
}

case class ResultSet[Rdf <: RDF](vars: Set[Var[Rdf]], bindings: Vector[SolutionMapping[Rdf]]) {

  def apply(block: TriplesBlock[Rdf])(implicit ops: RDFOps[Rdf]): Rdf#Graph = {
    import ops._
    block.triples.foldLeft(emptyGraph){ case (graph, triplePattern) =>
      graph union makeGraph(bindings.map(binding => binding(triplePattern)(ops)))
    }
  }

  def join(other: ResultSet[Rdf]): ResultSet[Rdf] = {
    val commonKeys = this.vars intersect other.vars
    def sameKeys(b1: Map[Var[Rdf], Rdf#Node], b2: Map[Var[Rdf], Rdf#Node]): Boolean =
      commonKeys forall { k => b1(k) == b2(k) }
    val builder = Vector.newBuilder[SolutionMapping[Rdf]]
    bindings foreach { case SolutionMapping(binding) =>
        other.bindings foreach {
          case SolutionMapping(b) if sameKeys(binding, b) => builder += SolutionMapping(binding ++ b)
          case _                                          => ()
        }
    }
    ResultSet(this.vars ++ other.vars, builder.result())
  }

}

trait LDPPatchCommand[Rdf <: RDF] {
  def PATCH(graph: Rdf#Graph, patch: LDPPatch[Rdf]): Try[Rdf#Graph]
}

class LDPPatchCommandImpl[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) extends LDPPatchCommand[Rdf] {

  import ops._

  def toNodeMatch(vt: VarOrTerm[Rdf]): Rdf#NodeMatch = vt match {
    case Var(_)     => ANY
    case Term(node) => node
  }

  def triplePatternMatching(graph: Rdf#Graph, tp: TriplePattern[Rdf]): ResultSet[Rdf] = {
    val vars: Set[Var[Rdf]] = {
      var vars = Set.empty[Var[Rdf]]
      tp.s match { case v: Var[Rdf] => vars += v ; case _ => () }
      tp.o match { case v: Var[Rdf] => vars += v ; case _ => () }
      vars
    }
    val builder = Vector.newBuilder[SolutionMapping[Rdf]]
    find(graph, toNodeMatch(tp.s), tp.p, toNodeMatch(tp.o)) foreach { case t@Triple(s, _, o) =>
      var binding = Map.empty[Var[Rdf], Rdf#Node]
      tp.s match { case v: Var[Rdf] => binding += (v -> s) ; case _ => () }
      tp.o match { case v: Var[Rdf] => binding += (v -> o) ; case _ => () }
      builder += SolutionMapping[Rdf](binding)
    }
    ResultSet(vars, builder.result())
  }

  def BGPMatching(graph: Rdf#Graph, where: Where[Rdf]): ResultSet[Rdf] = {
    where.block.triples.map(triplePatternMatching(graph, _)).reduceLeft(_ join _)
  }

  def PATCH(graph: Rdf#Graph, patch: LDPPatch[Rdf]): Try[Rdf#Graph] = Try {
    val LDPPatch(deleteOpt, insertOpt, whereOpt) = patch
    val resultSet = whereOpt match {
      case Some(where) => BGPMatching(graph, where)
      case None        => ResultSet.empty[Rdf]
    }
    var result = graph
    deleteOpt match {
      case Some(Delete(tripleBlock)) => result = result diff resultSet(tripleBlock)
      case _                         => ()
    }
    insertOpt match {
      case Some(Insert(tripleBlock)) => result = result union resultSet(tripleBlock)
      case _                         => ()
    }
    result
  }

}
