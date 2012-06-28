package org.w3.banana

import java.io.InputStream
import scalaz.Validation

/**
 * typeclass for a blocking BlockingReader of Sparql Query Results
 * such as those defined
 * <ul>
 *   <li><a href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query Results XML Format</a></li>
 *   <li><a href="http://www.w3.org/TR/rdf-sparql-json-res/">SPARQL Query Results in JSON</a></li>
 * </ul>
 *
 *
 * @tparam Sparql sparql implementation type
 * @tparam SyntaxType  type of serialisation to write to. Usually a phantom type, useful for type class behavior and
 *                     for aligning writers implemented with different frameworks (eg: Jena or Sesame)
 */
trait SparqlQueryResultsReader[Sparql <: SPARQL, +SyntaxType]
  extends BlockingReader[Either[Sparql#Solutions,Boolean],SyntaxType]