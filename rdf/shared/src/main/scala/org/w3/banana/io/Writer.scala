package org.w3.banana
package io

import java.io.OutputStream

/**
 * @tparam O the type of object that this Writer can serialize
 * @tparam T the Syntax that this Writer supports
 */
trait Writer[-O, M[_], +T] {

  //todo: add a method that takes a writer

  /**
   * todo: this method is missing an encoding
   * @param obj the object to serialise
   * @param outputstream the output stream to serialise to
   * @param base the optional base of the document, so that URLs are relativised to that base. This essentially takes a graph with absolute URLs and turns it into a relative graph before serialising it. Not needed if the graph is already relative, hence the use of Option.
   **/
  def write(obj: O, outputstream: OutputStream, base: Option[String]): M[Unit]

  def asString(obj: O, base: Option[String]): M[String]
}
