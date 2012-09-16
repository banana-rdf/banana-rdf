package org.w3.banana.jena

import org.w3.banana._
import java.io._
import com.hp.hpl.jena.rdf.model._

import scalaz.{ Failure, Validation }
import scalaz.Validation._
import com.hp.hpl.jena.sparql.resultset.{ JSONOutput, XMLOutput }

/**
 * Write a graph out using the Jena serialisers
 * @param ops  Rdf operations that will do the transformations of the graph to jena
 * @param graphWriter picks  up a graphWriter for the syntaxType desired
 * @tparam Rdf the rdf implementation of the given graph
 */
class JenaBasedWriter[Rdf <: RDF, SyntaxType](val ops: RDFOperations[Rdf])(implicit graphWriter: RDFBlockingWriter[Jena, SyntaxType],
  syntaxTp: Syntax[SyntaxType])
    extends RDFBlockingWriter[Rdf, SyntaxType] {

  private val MtoJena = new RDFTransformer[Rdf, Jena](ops, JenaOperations)

  def write(graph: Rdf#Graph, os: OutputStream, base: String): BananaValidation[Unit] =
    graphWriter.write(MtoJena.transform(graph), os, base)

  def write(graph: Rdf#Graph, writer: Writer, base: String): BananaValidation[Unit] =
    graphWriter.write(MtoJena.transform(graph), writer, base)

  def syntax[S >: SyntaxType] = syntaxTp
}

