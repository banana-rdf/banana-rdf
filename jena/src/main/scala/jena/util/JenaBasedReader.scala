package org.w3.banana.jena.util

import org.w3.banana._
import org.w3.banana.jena._
import java.io._

import scalaz.Validation

/**
 * a Jena based reader: it reads input and transforms it into a graph using a Jena parser
 * and transforms that into an Rdf Graph
 *
 * @param ops
 * @param graphReader The RdfReader to use to read the information
 * @tparam Rdf The RDF framework the resulting graphs will be in
 * @tparam SyntaxType  type of serialisation to write to. Usually a phantom type, useful for type class behavior and
 *                     for aligning writers implemented with different frameworks (eg: Jena or Sesame)
 */
class JenaBasedReader[Rdf <: RDF, SyntaxType](val ops: RDFOperations[Rdf])(implicit graphReader: RDFReader[Jena, SyntaxType])
    extends RDFReader[Rdf, Turtle] {

  private val jenaToM = new RDFTransformer[Jena, Rdf](JenaOperations, ops)

  def read(is: InputStream, base: String): BananaValidation[Rdf#Graph] =
    graphReader.read(is, base) map jenaToM.transform

  def read(reader: Reader, base: String): BananaValidation[Rdf#Graph] =
    graphReader.read(reader, base) map jenaToM.transform

}
