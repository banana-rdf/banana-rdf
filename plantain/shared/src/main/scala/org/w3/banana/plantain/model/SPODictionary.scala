package org.w3.banana.plantain.model

trait SPODictionary[T, S, P, O] {

  def subjectOf(s: T): S

  def objectOf(o: T): O

  def predicateOf(p: T): P

  def removeSubjectKey(s: T): SPODictionary[T, S, P, O]

  def removePredicateKey(p: T): SPODictionary[T, S, P, O]

  def removeObjectKey(o: T): SPODictionary[T, S, P, O]

  def tripleOf(triple: (T, T, T)): (S, P, O) = (subjectOf(triple._1), predicateOf(triple._2), objectOf(triple._3))

  def fromSubject(s: S): T

  def fromObject(o: O): T

  def fromPredicate(p: P): T

  def hasSubject(s: S): Boolean

  def hasObject(o: O): Boolean

  def hasPredicate(p: P): Boolean

  def newFromTriple(s: S, p: P, o: O): (T, T, T, SPODictionary[T, S, P, O])

}
