package org.w3.banana

import java.io._

import scala.util._

/**
 * <ul>
 *   <li><a href="http://www.w3.org/TR/rdf-sparql-XMLres/">Sparql Query Results XML Format</a></li>
 *   <li><a href="http://www.w3.org/TR/rdf-sparql-json-res/">Sparql Query Results in JSON</a></li>
 * </ul>
 */
trait SparqlQueryResultsReader[Rdf <: RDF, +S] {

  def read(is: InputStream, base: String): Try[Either[Rdf#Solutions, Boolean]]

  def read(reader: java.io.Reader, base: String): Try[Either[Rdf#Solutions, Boolean]]

  def read(file: File, base: String): Try[Either[Rdf#Solutions, Boolean]] =
    for {
      fis <- Try { new BufferedInputStream(new FileInputStream(file)) }
      graph <- read(fis, base)
    } yield graph

  def read(file: File, base: String, encoding: String): Try[Either[Rdf#Solutions, Boolean]] =
    for {
      fis <- Try { new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), encoding) }
      graph <- read(fis, base)
    } yield graph

  def read(s: String, base: String): Try[Either[Rdf#Solutions, Boolean]] = {
    val reader = new StringReader(s)
    read(reader, base)
  }

}

