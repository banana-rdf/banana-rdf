package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.resultset.{JSONOutput, XMLOutput, OutputFormatter}

/** typeclass that reflects a Jena String that can be used to construct a Reader */
trait JenaGraphSyntax[T] {
  val value: String
}


object JenaGraphSyntax {

  implicit val RDFXML: JenaGraphSyntax[RDFXML] = new JenaGraphSyntax[RDFXML] {
    val value = "RDF/XML"
  }

  implicit val Turtle: JenaGraphSyntax[Turtle] = new JenaGraphSyntax[Turtle] {
    val value = "TTL"
  }

}
