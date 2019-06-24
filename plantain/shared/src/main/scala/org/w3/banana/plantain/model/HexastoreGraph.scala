package org.w3.banana.plantain.model

import scala.collection.immutable
import scala.util.Try

object HexastoreMap {

  def empty[T]: HexastoreMap[T] = HexastoreMap(immutable.HashMap.empty[T, immutable.HashMap[T, immutable.List[T]]])
}

case class HexastoreMap[T](map: immutable.HashMap[T, immutable.HashMap[T, immutable.List[T]]]) {
  def +(a: T, b: T, c: T): HexastoreMap[T] = {
    val newMap = map.get(a) match {
      case Some(_bMap) =>
        _bMap.get(b) match {
          case Some(_cList) =>
            if (_cList.contains(c)) {
              map
            } else {
              map + (a -> (_bMap + (b -> (_cList :+ c))))
            }
          case None =>
            map + (a -> (_bMap + (b -> immutable.List(c))))
        }
      case None =>
        map + (a -> immutable.HashMap(b -> immutable.List(c)))
    }
    HexastoreMap(newMap)
  }

  def -(a: T, b: T, c: T): HexastoreMap[T] = {
    val newMap = map.get(a) match {
      case Some(_bMap) =>
        _bMap.get(b) match {
          case Some(_cList) =>
            if (_cList.contains(c)) {
              val newCList = _cList.filter(_ == c)
              if (newCList.nonEmpty) {
                map + (a -> (_bMap + (b -> newCList)))
              } else {
                val newBMap = _bMap - b
                if (newBMap.nonEmpty) {
                  map + (a -> newBMap)
                } else {
                  map - a
                }
              }
            } else {
              //no such element
              map
            }
          case None =>
            //no such element
            map
        }
      case None =>
        //no such element
        map
    }
    HexastoreMap(newMap)

  }

  def contains(a: T, b: T, c: T): Boolean = {
    map.get(a)
      .flatMap(_.get(b)).exists(_.contains(c))

  }

  def ab(a: T, b: T): Iterable[(T, T, T)] = {
    val test = map.get(a)
      .flatMap { bMap =>
        bMap.get(b)
      }

    test.map { cList =>
      cList.map(c => (a, b, c))
    }
      .getOrElse(Iterable.empty)
  }

  def a(a: T): Iterable[(T, T, T)] = {
    map.get(a)
      .map { bMap =>
        bMap.flatMap { b =>
          b._2.map(c => (a, b._1, c))
        }
      }
      .getOrElse(Iterable.empty)
  }


}

object HexastoreTriples {

  def empty[T]: HexastoreTriples[T] = HexastoreTriples(
    HexastoreMap.empty[T],
    HexastoreMap.empty[T],
    HexastoreMap.empty[T],
    HexastoreMap.empty[T],
    HexastoreMap.empty[T],
    HexastoreMap.empty[T]
  )

}

case class HexastoreTriples[T](
                                spo: HexastoreMap[T],
                                sop: HexastoreMap[T],
                                pso: HexastoreMap[T],
                                pos: HexastoreMap[T],
                                osp: HexastoreMap[T],
                                ops: HexastoreMap[T]
                              )

trait SPODictionaries[T, S, P, O] {

  def subjectOf(s: T): S

  def objectOf(o: T): O

  def predicateOf(p: T): P

  def removeFromSubjects(s: S): SPODictionaries[T, S, P, O]

  def removeFromPredicates(p: P): SPODictionaries[T, S, P, O]

  def removeFromObjects(o: O): SPODictionaries[T, S, P, O]

  def tripleOf(triple: (T, T, T)): (S, P, O) = (subjectOf(triple._1), predicateOf(triple._2), objectOf(triple._3))

  def fromSubject(s: S): T

  def fromObject(o: O): T

  def fromPredicate(p: P): T

  def hasSubject(s: S): Boolean

  def hasObject(o: O): Boolean

  def hasPredicate(p: P): Boolean

  def newFromTriple(s: S, p: P, o: O): (T, T, T, SPODictionaries[T, S, P, O])


}

trait HexastoreGraph[T, S, P, O] {

  def size: Int

  def hexaTriples: HexastoreTriples[T]

  def dicts: SPODictionaries[T, S, P, O]


  def triples: Iterable[(S, P, O)] =
    for {
      (s, poMap) <- hexaTriples.spo.map
      (p, oList) <- poMap
      o <- oList
    } yield (dicts.subjectOf(s), dicts.predicateOf(p), dicts.objectOf(o))


  def +(subject: S, predicate: P, objectt: O): HexastoreGraph[T, S, P, O] = {
    val (s, p, o, newDict) = dicts.newFromTriple(subject, predicate, objectt)
    val (
      spo,
      sop,
      pso,
      pos,
      osp,
      ops) = HexastoreTriples.unapply(hexaTriples).get

    newHexastoreGraph(
      HexastoreTriples(
        spo + (s, p, o),
        sop + (s, o, p),
        pso + (p, s, o),
        pos + (p, o, s),
        osp + (o, s, p),
        ops + (o, p, s)),
      newDict,
      size + 1
    )

  }

  def -(subject: S, predicate: P, objectt: O): HexastoreGraph[T, S, P, O] = {
    val s = dicts.fromSubject(subject)
    val p = dicts.fromPredicate(predicate)
    val o = dicts.fromObject(objectt)
    val (
      spo,
      sop,
      pso,
      pos,
      osp,
      ops) = HexastoreTriples.unapply(hexaTriples).get


    val newHexastoreTriples =
      HexastoreTriples(
        spo - (s, p, o),
        sop - (s, o, p),
        pso - (p, s, o),
        pos - (p, o, s),
        osp - (o, s, p),
        ops - (o, p, s))

    //TODO: do something about ever growing dictionaries (complete rebuild?)

    var newDicts = dicts
    if(!newHexastoreTriples.spo.map.contains(s))
      newDicts = newDicts.removeFromSubjects(subject)
    if(!newHexastoreTriples.pos.map.contains(p))
      newDicts = newDicts.removeFromPredicates(predicate)
    if(!newHexastoreTriples.osp.map.contains(o))
      newDicts = newDicts.removeFromObjects(objectt)

    newHexastoreGraph(
      newHexastoreTriples,
      newDicts,
      size - 1
    )

  }

  def -(s: Option[S], p: Option[P], o: Option[O]): HexastoreGraph[T, S, P, O] = {
    val matchedTriples: Iterable[(S, P, O)] = find(s, p, o)
    matchedTriples.foldLeft(this) { case (graph, (_s, _p, _o)) => graph - (_s, _p, _o) }
  }

  def newHexastoreGraph(_hexaTriples: HexastoreTriples[T], _dicts: SPODictionaries[T, S, P, O], size: Int): HexastoreGraph[T, S, P, O]


  def union(other: HexastoreGraph[T, S, P, O]): HexastoreGraph[T, S, P, O] = {
    val (firstGraph, secondGraph) =
      if (this.size > other.size) (this, other)
      else (other, this)
    secondGraph.triples.foldLeft(firstGraph) { case (graph, (s, p, o)) => graph + (s, p, o) }
  }

  def find(subject: Option[S], predicate: Option[P], objectt: Option[O]): Iterable[(S, P, O)] = {

    if (!(subject.map(dicts.hasSubject(_)).getOrElse(true) &&
      predicate.map(dicts.hasPredicate(_)).getOrElse(true) &&
      objectt.map(dicts.hasObject(_)).getOrElse(true))) {
      return Iterable.empty
    }
    val (_subject, _predicate, _object) = (subject.map(dicts.fromSubject(_)), predicate.map(dicts.fromPredicate(_)), objectt.map(dicts.fromObject(_)))
    val (
      spo,
      sop,
      pso,
      pos,
      osp,
      ops) = HexastoreTriples.unapply(hexaTriples).get

    (_subject, _predicate, _object) match {

      case (None, None, None) => triples
      case (Some(s), Some(p), Some(o)) => {
        if (hexaTriples.spo.contains(s, p, o)) {
          Iterable((dicts.subjectOf(s), dicts.predicateOf(p), dicts.objectOf(o)))
        } else {
          Iterable.empty
        }
      }
      case (Some(s), Some(p), None) =>
        spo.ab(s, p).map(dicts.tripleOf)
      case (Some(s), None, Some(o)) =>
        sop.ab(s, o).map(tr => dicts.tripleOf(tr._1, tr._3, tr._2))
      case (None, Some(p), Some(o)) =>
        pos.ab(p, o).map(tr => dicts.tripleOf(tr._3, tr._1, tr._2))
      case (Some(s), None, None) =>
        spo.a(s).map(dicts.tripleOf)
      case (None, Some(p), None) =>
        pso.a(p).map(tr => dicts.tripleOf(tr._2, tr._1, tr._3))
      case (None, None, Some(o)) =>
        ops.a(o).map(tr => dicts.tripleOf(tr._3, tr._2, tr._1))
    }
  }

  override def toString(): String = triples.mkString("(", " ", ")")

}

object HexastoreGraph {

  // def apply[T, S, P, O](_hexaTriples: HexastoreTriples[T], _dicts: SPODictionaries[T,S,P,O]): HexastoreGraph[T,S,P,O]

}
