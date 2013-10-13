package org.w3.banana

/** in this context, Tree is a rooted directed tree, and we don't care care about the tree structure */
case class Tree[Rdf <: RDF](root: VarOrTerm[Rdf], nodes: Set[VarOrTerm[Rdf]]) {

  /** adds a TriplePattern in the root position if possible */
  def addRoot(tp: TriplePattern[Rdf])(implicit ops: RDFOps[Rdf]): Option[Tree[Rdf]] =
    if (tp.o == root && !nodes.contains(tp.s)) Some(Tree(tp.s, nodes + root)) else None

  /** adds a TriplePattern in a branch position if possible */
  def addBranch(tp: TriplePattern[Rdf])(implicit ops: RDFOps[Rdf]): Option[Tree[Rdf]] =
    if (nodes.contains(tp.s) && tp.o != root) Some(Tree(root, nodes + tp.o)) else None

}

object Forest {

  def empty[Rdf <: RDF]: Forest[Rdf] = Forest[Rdf](Set.empty[Tree[Rdf]], Map.empty[VarOrTerm[Rdf], Tree[Rdf]])

  /** builds a Forest out of a bunch of TriplePattern-s */
  def apply[Rdf <: RDF](triples: Iterable[TriplePattern[Rdf]]): Forest[Rdf] =
    triples.foldLeft(Forest.empty[Rdf])(_ add _)

}

/** a Forest is a disjoint set of Trees (i.e. they don't share any
  * node/root). It maintains a tree index, mapping a VarOrTerm to the
  * tree it belongs to */
case class Forest[Rdf <: RDF](trees: Set[Tree[Rdf]], index: Map[VarOrTerm[Rdf], Tree[Rdf]]) {

  def isSingleTree: Boolean = trees.size == 1

  def add(triplePattern: TriplePattern[Rdf]): Forest[Rdf] = {
    val TriplePattern(s, _, o) = triplePattern
    if (s == o) throw new AssertionError(s"subject and object cannot be the same for TriplePattern $triplePattern")
    (index.get(s), index.get(o)) match {
      // neither s nor o appear in any tree
      case (None, None) =>
        val tree = Tree(s, Set(s, o))
        Forest(trees + tree, index + (s -> tree) + (o -> tree))
      // s appears as a node in an existing tree
      case (Some(t), None) =>
        val tree = Tree(t.root, t.nodes + o)
        Forest(trees - t + tree, index ++ tree.nodes.map(_ -> tree))
      // o appears as a node in an existing tree, in the subject position
      case (None, Some(t@Tree(`o`, _))) =>
        val tree = Tree(s, t.nodes + s)
        Forest(trees - t + tree, index ++ tree.nodes.map(_ -> tree))
      // o appears as a node in an existing tree, NOT in the subject position
      case (None, Some(t)) =>
        throw new AssertionError(s"$s is another root for tree $t")
      // the TriplePattern connects two existing trees
      case (Some(t1), Some(t2)) if t1 == t2  =>
        throw new AssertionError(s"$triplePattern is binding that graph onto itself $t1")
      case (Some(t1), Some(t2)) =>
        val tree = Tree(t1.root, t1.nodes ++ t2.nodes)
        Forest(trees - t1 - t2 + tree, index ++ tree.nodes.map(_ -> tree))
    }
  }


}
