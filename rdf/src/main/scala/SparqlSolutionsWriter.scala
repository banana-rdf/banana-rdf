package org.w3.banana

import java.io.{ Writer, OutputStream }
import scalaz.Validation

/**
 * typeclass for a Writer of Sparql Solutions
 *
 * @tparam Sparql sparql implementation type
 * @tparam SyntaxType  type of serialisation to write to. Usually a phantom type, useful for type class behavior and
 *                    for aligning writers implemented with different frameworks (eg: Jena or Sesame)
 */
trait SparqlSolutionsWriter[Sparql <: SPARQL, +SyntaxType] extends BlockingWriter[Sparql#Solutions, SyntaxType]

