package org.w3.util

import scalaz.Validation

class ValidationW[E, S](private val inner: Validation[E, S]) {

  def failMap[EE](f: E => EE): Validation[EE, S] = inner.fail.map(f).validation

}
