package org.w3.banana

import java.io.InputStream
import scalaz.Validation
import scala.Either

/**
 * typeclass for a blocking BlockingReader of Sparql Query Results
 * such as those defined
 * <ul>
 *   <li><a href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query Results XML Format</a></li>
 *   <li><a href="http://www.w3.org/TR/rdf-sparql-json-res/">SPARQL Query Results in JSON</a></li>
 * </ul>
 *
 * The BlockingReader's answers are of type Either[Rdf#Solutions, Boolean], where the
 * left is reserved for multiple tuple solutions for SELECT queries, and the right is
 * reserved for boolean queries ("yes, sir, yes, sir, three bags full sir")
 *
 * @tparam Rdf RDF implementation of SPARQL
 * @tparam SyntaxType  type of serialisation to write to. Usually a phantom type, useful for type class behavior and
 *                     for aligning writers implemented with different frameworks (eg: Jena or Sesame)
 */
trait SparqlQueryResultsReader[Rdf <: RDF, +SyntaxType]
  extends BlockingReader[Either[Rdf#Solutions, Boolean], SyntaxType]
