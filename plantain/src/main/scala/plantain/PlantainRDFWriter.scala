package org.w3.banana.plantain

import org.w3.banana._
import org.w3.banana.sesame.SesameSyntax
import java.io.{ Writer => jWriter, _ }
import scalax.io._
import scala.util._

object PlantainRDFWriter {

  def apply[T](implicit sesameSyntax: SesameSyntax[T], _syntax: Syntax[T]): RDFWriter[Plantain, T] =
    new RDFWriter[Plantain, T] {

      val syntax = _syntax

      def write[R <: jWriter](graph: Plantain#Graph, wcr: WriteCharsResource[R], base: String): Try[Unit] =
        Try {
          wcr.acquireAndGet { writer =>
            val sWriter = sesameSyntax.rdfWriter(writer, base)
            sWriter.startRDF()
            graph.triples foreach { triple => sWriter.handleStatement(triple.asSesame) }
            sWriter.endRDF()
          }
        }

    }

  implicit val rdfxmlWriter: RDFWriter[Plantain, RDFXML] = PlantainRDFWriter[RDFXML]

  implicit val turtleWriter: RDFWriter[Plantain, Turtle] = PlantainRDFWriter[Turtle]

  implicit val selector: RDFWriterSelector[Plantain] =
    RDFWriterSelector[Plantain, RDFXML] combineWith RDFWriterSelector[Plantain, Turtle]

}
