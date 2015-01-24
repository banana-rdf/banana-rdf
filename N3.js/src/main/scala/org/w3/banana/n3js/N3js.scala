package org.w3.banana
package n3js

import io._
import java.io.{ InputStream, Reader }
import scala.concurrent.Future

class TurtleParser[Rdf <: RDF] extends RDFReader[Rdf, Future, Turtle] {

  def read(reader: Reader, base: String): Future[Rdf#Graph] = ???

  def read(is: InputStream, base: String): Future[Rdf#Graph] = ???

}
