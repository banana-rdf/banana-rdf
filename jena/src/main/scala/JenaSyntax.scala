package org.w3.banana.jena

import org.w3.banana._

/** typeclass that reflects a Jena String that can be used to construct a Reader */
trait JenaSyntax[T] {
  val value: String
}

object JenaSyntax {

  implicit val RDFXML: JenaSyntax[RDFXML] = new JenaSyntax[RDFXML] {
    val value = "RDF/XML"
  }

  implicit val Turtle: JenaSyntax[Turtle] = new JenaSyntax[Turtle] {
    val value = "TTL"
  }

}
