package org.w3.banana.diesel

class ObjectList[T](val ts: Iterable[T]) extends AnyVal {}

object ObjectList {
  def apply[T](ts: Iterable[T]): ObjectList[T] = new ObjectList[T](ts)
}
