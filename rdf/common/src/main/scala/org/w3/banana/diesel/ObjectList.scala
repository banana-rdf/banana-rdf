package org.w3.banana.diesel

class ObjectList[T](val ts: Traversable[T]) extends AnyVal {}

object ObjectList {
  def apply[T](ts: Traversable[T]): ObjectList[T] = new ObjectList[T](ts)
}
