package org.w3.banana.jena

import org.w3.banana._
import java.io._
import com.hp.hpl.jena.rdf.model._

import scalaz.{Failure, Validation}
import scalaz.Validation._
import com.hp.hpl.jena.sparql.resultset.{JSONOutput, XMLOutput}

/**
 * Write a graph out using the Jena engine
 * @param ops
 * @tparam Rdf
 */
class JenaBasedTurtleWriter[Rdf <: RDF](val ops: RDFOperations[Rdf])
  extends RDFBlockingWriter[Rdf, Turtle] {

  private val MtoJena = new RDFTransformer[Rdf, Jena](ops, JenaOperations)

  def write(graph: Rdf#Graph, os: OutputStream, base: String): Validation[BananaException, Unit] =
     JenaRDFBlockingWriter.TurtleWriter.write(MtoJena.transform(graph) ,os,base)
  
  def write(graph: Rdf#Graph, writer: Writer, base: String): Validation[BananaException, Unit] =
    JenaRDFBlockingWriter.TurtleWriter.write(MtoJena.transform(graph) ,writer,base)
}

object JenaBasedTurtleWriter {
  def apply[Rdf <: RDF](ops: RDFOperations[Rdf]): JenaBasedTurtleWriter[Rdf] =
    new JenaBasedTurtleWriter[Rdf](ops)
}


