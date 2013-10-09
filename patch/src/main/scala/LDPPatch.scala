package org.w3.banana

import scala.util.Try

/** a SolutionMapping is one single solution (among others) for a BGP */
case class SolutionMapping[Rdf <: RDF](binding: Map[VarPattern[Rdf], Rdf#Node]) {

  def apply(tp: TriplePattern[Rdf])(ops: RDFOps[Rdf]): Rdf#Triple = {
    def varOrTermtoNode(vt: VarOrTerm[Rdf]): Rdf#Node = vt match {
      case v: Var[Rdf] => binding(v)
      case Term(node)  => ops.foldNode(node)(uri => uri, bnode => binding(VarBNode(bnode)), literal => literal)
    }
    def varOrIRIReftoNode(vi: VarOrIRIRef[Rdf]): Rdf#URI = vi match {
      case v: Var[Rdf] => ops.foldNode(binding(v))(
        uri => uri,
        bnode => sys.error(s"$v matched a bnode and was used in predicate position"),
        literal => sys.error(s"$v matched a literal (value $literal) and was used in predicate position"))
      case IRIRef(uri) => uri
    }
    ops.Triple(varOrTermtoNode(tp.s), varOrIRIReftoNode(tp.p), varOrTermtoNode(tp.o))
  }

}

object ResultSet {
  def empty[Rdf <: RDF]: ResultSet[Rdf] = ResultSet[Rdf](Set.empty, Vector.empty)
}

/** a ResultSet is the set of all solutions for a BGP */
case class ResultSet[Rdf <: RDF](vars: Set[VarPattern[Rdf]], bindings: Vector[SolutionMapping[Rdf]]) {

  /** applies this ResultSet to the given TriplesBlock */
  def apply(block: TriplesBlock[Rdf])(implicit ops: RDFOps[Rdf]): Rdf#Graph = {
    import ops._
    block.triples.foldLeft(emptyGraph){ case (graph, triplePattern) =>
      graph union makeGraph(bindings.map(binding => binding(triplePattern)(ops)))
    }
  }

  /** computes the natural join, which is just the cartesian product
    * where the values for the shared keys are equal */
  def join(other: ResultSet[Rdf]): ResultSet[Rdf] = {
    val commonKeys = this.vars intersect other.vars
    def sameKeys(b1: Map[VarPattern[Rdf], Rdf#Node], b2: Map[VarPattern[Rdf], Rdf#Node]): Boolean =
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

object LDPPatch {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]): LDPPatch[Rdf] = new LDPPatchImpl[Rdf]()(ops)
}

trait LDPPatch[Rdf <: RDF] {
  def PATCH(graph: Rdf#Graph, patch: Patch[Rdf]): Try[Rdf#Graph]
}

class LDPPatchImpl[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) extends LDPPatch[Rdf] {

  import ops._

  def toNodeMatch(vt: VarOrTerm[Rdf]): Rdf#NodeMatch = vt match {
    case _: Var[Rdf] => ANY
    case Term(node)  => foldNode(node)(uri => uri, bnode => ANY, literal => literal)
  }

  def toNodeMatch(vi: VarOrIRIRef[Rdf]): Rdf#NodeMatch = vi match {
    case _: Var[Rdf] => ANY
    case IRIRef(uri) => uri
  }

  /** computes the set of solutions for the given TriplePattern against the given graph */
  def triplePatternMatching(graph: Rdf#Graph, tp: TriplePattern[Rdf]): ResultSet[Rdf] = {
    // the set of vars in the TriplePattern (either from a ?v or from a bnode, which actually behaves like a var)
    val vars: Set[VarPattern[Rdf]] = {
      var vars = Set.empty[VarPattern[Rdf]]
      tp.s match { case v: Var[Rdf] => vars += v ; case Term(node) => node.fold(uri => (), bn => vars += VarBNode(bn), literal => ()) }
      tp.p match { case v: Var[Rdf] => vars += v ; case _ => () }
      tp.o match { case v: Var[Rdf] => vars += v ; case Term(node) => node.fold(uri => (), bn => vars += VarBNode(bn), literal => ()) }
      vars
    }
    // builder accumulates all the bindings satisfying the pattern
    val builder = Vector.newBuilder[SolutionMapping[Rdf]]
    // the vars are turned into wildcars and passed to find which returns the matching triples
    find(graph, toNodeMatch(tp.s), toNodeMatch(tp.p), toNodeMatch(tp.o)) foreach { case t@Triple(s, p, o) =>
      // a binding is just about remember what value is associated with what var
      var binding = Map.empty[VarPattern[Rdf], Rdf#Node]
      tp.s match { case v: Var[Rdf] => binding += (v -> s) ; case Term(node) => node.fold(uri => (), bn => binding += (VarBNode(bn) -> s), literal => ()) }
      tp.p match { case v: Var[Rdf] => binding += (v -> p) ; case _ => () }
      tp.o match { case v: Var[Rdf] => binding += (v -> o) ; case Term(node) => node.fold(uri => (), bn => binding += (VarBNode(bn) -> o), literal => ()) }
      builder += SolutionMapping[Rdf](binding)
    }
    ResultSet(vars, builder.result())
  }

  /** computes the set of solutions for the given BGP (the PATCH grammar
    * restricts the WHERE clause to a simple BGP) against the given
    * graph. We just compute the bindings for each pattern and then
    * apply a join on them. */
  def BGPMatching(graph: Rdf#Graph, where: Where[Rdf]): ResultSet[Rdf] = {
    where.block.triples.map(triplePatternMatching(graph, _)).reduceLeft(_ join _)
  }

  def PATCH(graph: Rdf#Graph, patch: Patch[Rdf]): Try[Rdf#Graph] = Try {
    val Patch(deleteOpt, insertOpt, whereOpt) = patch
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
