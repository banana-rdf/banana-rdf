package org.w3.algebraic

trait UnApply0[R] {
  def unapply(r: R): Boolean
}

trait AlgebraicDataType0[R] extends Function0[R] with UnApply0[R]

trait UnApply1[T, R] {
  def unapply(r: R): Option[T]
}

trait AlgebraicDataType1[T, R] extends Function1[T, R] with UnApply1[T, R]

trait UnApply2[T1, T2, R] {
  def unapply(r: R): Option[(T1, T2)]
}

/**
 * basically, you have to implement both following functions
 *   def apply(t1:T1, t2:T2):R
 *   def unapply(r:R):Option[(T1, T2)]
 */
trait AlgebraicDataType2[T1, T2, R] extends Function2[T1, T2, R] with UnApply2[T1, T2, R]

trait UnApply3[T1, T2, T3, R] {
  def unapply(r: R): Option[(T1, T2, T3)]
}

trait AlgebraicDataType3[T1, T2, T3, R] extends Function3[T1, T2, T3, R] with UnApply3[T1, T2, T3, R]

