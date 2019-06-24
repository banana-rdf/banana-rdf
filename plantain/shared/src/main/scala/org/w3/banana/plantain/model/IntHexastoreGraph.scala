package org.w3.banana.plantain.model

class IntSPODictionary[S, P, O](
                                 subjectMap: Map[Int, S],
                                 predicateMap: Map[Int, P],
                                 objectMap: Map[Int, O],
                                 rsubjectMap: Map[S, Int],
                                 rpredicateMap: Map[P, Int],
                                 robjectMap: Map[O, Int],
                                 counter: Int
                               ) extends SPODictionaries[Int, S, P, O] {


  override def subjectOf(s: Int): S = subjectMap(s)

  override def objectOf(o: Int): O = objectMap(o)

  override def predicateOf(p: Int): P = predicateMap(p)

  override def fromSubject(s: S): Int = rsubjectMap(s)

  override def fromObject(o: O): Int = robjectMap(o)

  override def fromPredicate(p: P): Int = rpredicateMap(p)

  override def hasSubject(s: S): Boolean = rsubjectMap.contains(s)

  override def hasObject(o: O): Boolean = robjectMap.contains(o)

  override def hasPredicate(p: P): Boolean = rpredicateMap.contains(p)

  override def newFromTriple(s: S, p: P, o: O): (Int, Int, Int, SPODictionaries[Int, S, P, O]) = {
    var newCounter = counter
    val subj = rsubjectMap.get(s) match {
      case Some(num) => (num, subjectMap, rsubjectMap)
      case _ => newCounter = newCounter + 1
        (newCounter, subjectMap + (newCounter -> s), rsubjectMap + (s -> newCounter))
    }
    val pred = rpredicateMap.get(p) match {
      case Some(num) => (num, predicateMap, rpredicateMap)
      case _ => newCounter = newCounter + 1
        (newCounter, predicateMap + (newCounter -> p), rpredicateMap + (p -> newCounter))
    }
    val obj = robjectMap.get(o) match {
      case Some(num) => (num, objectMap, robjectMap)
      case _ => newCounter = newCounter + 1
        (newCounter, objectMap + (newCounter -> o), robjectMap + (o -> newCounter))
    }
    (subj._1, pred._1, obj._1, new IntSPODictionary(
      subj._2, pred._2, obj._2,
      subj._3, pred._3, obj._3,
      newCounter
    ))


  }

  override def removeFromSubjects(s: S): SPODictionaries[Int, S, P, O] = new IntSPODictionary[S, P, O](
    subjectMap - fromSubject(s), predicateMap, objectMap,
    rsubjectMap - s, rpredicateMap, robjectMap,
    counter
  )

  override def removeFromPredicates(p: P): SPODictionaries[Int, S, P, O] = new IntSPODictionary[S, P, O](
    subjectMap, predicateMap - fromPredicate(p), objectMap,
    rsubjectMap, rpredicateMap - p, robjectMap,
    counter
  )

  override def removeFromObjects(o: O): SPODictionaries[Int, S, P, O] = new IntSPODictionary[S, P, O](
    subjectMap, predicateMap, objectMap - fromObject(o),
    rsubjectMap, rpredicateMap, robjectMap - o,
    counter
  )
}

object IntSPODictionary {

  def empty[S, P, O]: IntSPODictionary[S, P, O] = new IntSPODictionary[S, P, O](
    Map.empty[Int, S],
    Map.empty[Int, P],
    Map.empty[Int, O],
    Map.empty[S, Int],
    Map.empty[P, Int],
    Map.empty[O, Int],
    0
  )

}

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

  def empty[S, P, O]: IntHexastoreGraph[S, P, O] = IntHexastoreGraph[S, P, O](HexastoreTriples.empty[Int], IntSPODictionary.empty[S, P, O], 0)

}
