package org.w3.banana.plantain.model


case class IntHexastoreGraph[S, P, O](hexaTriples: HexastoreTriples[Int], dicts: SPODictionaries[Int, S, P, O], size: Int) extends HexastoreGraph[Int, S, P, O] {
  //override def size: Int = _size

  //override def hexaTriples: HexastoreTriples[Int] = _hexaTriples

  //override def dicts: SPODictionaries[Int, S, P, O] = _dicts

  override def -(subject: S, predicate: P, objectt: O): IntHexastoreGraph[S, P, O] = super.-(subject, predicate, objectt).asInstanceOf[IntHexastoreGraph[S, P, O]]

  override def +(subject: S, predicate: P, objectt: O): IntHexastoreGraph[S, P, O] = super.+(subject, predicate, objectt).asInstanceOf[IntHexastoreGraph[S, P, O]]

  override def newHexastoreGraph(_hexaTriples: HexastoreTriples[Int], _dicts: SPODictionaries[Int, S, P, O], _size: Int): HexastoreGraph[Int, S, P, O] =
    new IntHexastoreGraph[S, P, O](_hexaTriples, _dicts, _size)

}


object IntHexastoreGraph {

  def empty[S, P, O]: IntHexastoreGraph[S, P, O] = IntHexastoreGraph[S, P, O](HexastoreTriples.empty[Int], IntBTreeSPODictionaries.empty[S, P, O], 0)

}
