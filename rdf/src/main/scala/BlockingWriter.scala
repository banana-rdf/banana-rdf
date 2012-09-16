package org.w3.banana

import java.io.{ Writer, OutputStream }
import scalaz.Validation

/**
 * A blocking Writer (blocks thread it is used in) that writes Object into a serialisation given
 * by SyntaxType
 *
 * @tparam ObjectType The Object type that the write knows how to serialise
 * @tparam SyntaxType The syntax type that it is serialised to.
 */
trait BlockingWriter[ObjectType, +SyntaxType] {
  def write(input: ObjectType, os: OutputStream, base: String): BananaValidation[Unit]

  def write(input: ObjectType, writer: Writer, base: String): BananaValidation[Unit]

  def syntax[S >: SyntaxType]: Syntax[S]
}
