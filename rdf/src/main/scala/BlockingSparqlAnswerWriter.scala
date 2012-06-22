package org.w3.banana

import java.io.OutputStream
import org.w3.banana.{BananaException, SPARQL}
import scalaz.Validation

/**
 * typeclass for an RDF Writer
 *
 * @tparam Sparql sparql implementation type
 * @tparam SyntaxType  type of serialisation to write to. Usually a phantom type, useful for type class behavior and
 *                    for aligning writers implemented with different frameworks (eg: Jena or Sesame)
 */
trait BlockingSparqlAnswerWriter[Sparql <: SPARQL, +SyntaxType] {

  def write(answers: Sparql#Solutions, os: OutputStream): Validation[BananaException, Unit]

  def write(answer: Boolean, os: OutputStream): Validation[BananaException, Unit]

  // this can be placed in an object, it needs to call either a graphwriter or a sparqlwriter
  // def write(answers: Either3[Sparql#Solutions, RDF#Graph, Boolean], os: OutputStream): Validation[BananaException, Unit]

}