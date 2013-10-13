package org.w3.banana

/** in this context, Tree is a rooted directed tree, and we don't care care about the tree structure */
case class Tree[Rdf <: RDF](root: VarOrTerm[Rdf], nodes: Set[VarOrTerm[Rdf]]) {

  assert(! nodes.contains(root))

  /** adds a TriplePattern in the root position if possible */
  def addRoot(tp: TriplePattern[Rdf])(implicit ops: RDFOps[Rdf]): Option[Tree[Rdf]] =
    if (tp.o == root && !nodes.contains(tp.s)) Some(Tree(tp.s, nodes + root)) else None

  /** adds a TriplePattern in a branch position if possible */
  def addBranch(tp: TriplePattern[Rdf])(implicit ops: RDFOps[Rdf]): Option[Tree[Rdf]] =
    if (nodes.contains(tp.s) && tp.o != root) Some(Tree(root, nodes + tp.o)) else None

}

object Forest {

  def empty[Rdf <: RDF]: Forest[Rdf] = Forest[Rdf](Set.empty[Tree[Rdf]])

  /** builds a Forest out of a bunch of TriplePattern-s */
  def apply[Rdf <: RDF](triples: Iterable[TriplePattern[Rdf]]): Forest[Rdf] =
    triples.foldLeft(Forest.empty[Rdf])(_ add _)

}

/** a Forest is a disjoint set of Trees (i.e. they don't share any node/root) */
case class Forest[Rdf <: RDF](trees: Set[Tree[Rdf]]) {

  def isSingleTree: Boolean = trees.size == 1

  def add(triplePattern: TriplePattern[Rdf]): Forest[Rdf] = {
    val TriplePattern(s, _, o) = triplePattern
    var rootFor: Option[Tree[Rdf]] = None
    var branchFor: Option[Tree[Rdf]] = None
    trees foreach { tree =>
      if (s == tree.root)
        throw new AssertionError("")
      if (!rootFor.isDefined && tree.root == o)
        rootFor = Some(tree)
      if (!branchFor.isDefined && tree.nodes.contains(s))
        branchFor = Some(tree)
    }
    (rootFor, branchFor) match {
      case (None, None)                      => Forest(trees + Tree(s, Set(o)))
      case (Some(t@Tree(root, nodes)), None) => Forest(trees - t + Tree(s, nodes + root))
      case (None, Some(t@Tree(root, nodes))) => Forest(trees - t + Tree(root, nodes + o))
      case (Some(t1), Some(t2)) if t1 == t2  =>
        throw new AssertionError(s"$triplePattern is binding that graph onto itself $t1")
      case (Some(t1@Tree(root1, nodes1)), Some(t2@Tree(root2, nodes2))) =>
        Forest(trees - t1 - t2 + Tree(root2, nodes2 ++ nodes1 + root1))
    }
  }


}
