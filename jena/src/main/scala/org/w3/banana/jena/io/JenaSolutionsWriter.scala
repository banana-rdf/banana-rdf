package org.w3.banana.jena.io

import java.io._
import org.w3.banana._
import io._
import jena._
import org.apache.jena.riot.ResultSetMgr

import scala.util._

object JenaSolutionsWriter {

  /** Creates a Sparql writer for the given syntax */
  def apply[T](implicit
    jenaSparqlSyntax: JenaAnswerOutput[T]
  ): SparqlSolutionsWriter[Jena, T] = new SparqlSolutionsWriter[Jena, T] {

    def write(answers: Jena#Solutions, os: OutputStream, base: String) = Try {
      ResultSetMgr.write(os,answers,jenaSparqlSyntax.formatter)
    }

    def asString(answers: Jena#Solutions, base: String): Try[String] = Try {
      val result = new ByteArrayOutputStream()
      ResultSetMgr.write(result, answers, jenaSparqlSyntax.formatter)
      result.toString
    }
  }

  implicit val solutionsWriterJson: SparqlSolutionsWriter[Jena, SparqlAnswerJson] =
    JenaSolutionsWriter[SparqlAnswerJson]

  implicit val solutionsWriterXml: SparqlSolutionsWriter[Jena, SparqlAnswerXml] =
    JenaSolutionsWriter[SparqlAnswerXml]

}
