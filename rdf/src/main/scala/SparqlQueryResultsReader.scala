package org.w3.banana

import scalaz.Validation
import java.io._

/**
 * <ul>
 *   <li><a href="http://www.w3.org/TR/rdf-sparql-XMLres/">Sparql Query Results XML Format</a></li>
 *   <li><a href="http://www.w3.org/TR/rdf-sparql-json-res/">Sparql Query Results in JSON</a></li>
 * </ul>
 */
trait SparqlQueryResultsReader[Rdf <: RDF, +S] {

  def read(is: InputStream, base: String): BananaValidation[Either[Rdf#Solutions, Boolean]]

  def read(reader: java.io.Reader, base: String): BananaValidation[Either[Rdf#Solutions, Boolean]]

  def read(file: File, base: String): BananaValidation[Either[Rdf#Solutions, Boolean]] =
    for {
      fis <- WrappedThrowable.fromTryCatch { new BufferedInputStream(new FileInputStream(file)) }
      graph <- read(fis, base)
    } yield graph

  def read(file: File, base: String, encoding: String): BananaValidation[Either[Rdf#Solutions, Boolean]] =
    for {
      fis <- WrappedThrowable.fromTryCatch { new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), encoding) }
      graph <- read(fis, base)
    } yield graph

  def read(s: String, base: String): BananaValidation[Either[Rdf#Solutions, Boolean]] = {
    val reader = new StringReader(s)
    read(reader, base)
  }

}

