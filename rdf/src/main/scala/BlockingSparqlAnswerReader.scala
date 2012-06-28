package org.w3.banana

import java.io.InputStream
import scalaz.Validation

/**
 * typeclass for a blocking Reader of Sparql Answers
 *
 * @tparam Sparql sparql implementation type
 * @tparam SyntaxType  type of serialisation to write to. Usually a phantom type, useful for type class behavior and
 *                     for aligning writers implemented with different frameworks (eg: Jena or Sesame)
 */
trait BlockingSparqlAnswerReader[Sparql <: SPARQL, +SyntaxType] {

  def read(in: InputStream): Validation[BananaException, Sparql#Solutions]

  //  def read(reader: Reader): Validation[BananaException, Either[Sparql#Solution,Boolean]]

  // this can be placed in an object, it needs to call either a graphwriter or a sparqlwriter
  // def write(answers: Either3[Sparql#Solutions, RDF#Graph, Boolean], os: OutputStream): Validation[BananaException, Unit]

}