package org.w3.banana.plantain

import org.w3.banana._
import java.nio.file._
import scala.concurrent._
import scala.concurrent.stm._
import akka.actor._
import akka.util._
import akka.pattern.{ ask, pipe }
import akka.transactor._
import scalaz.Free
import org.openrdf.model.{ URI => SesameURI, _ }
import org.openrdf.model.impl._
import org.openrdf.query.algebra.evaluation.TripleSource
import org.openrdf.query.QueryEvaluationException
import info.aduna.iteration.CloseableIteration
import PlantainUtil._
import PlantainOps.uriSyntax
import org.slf4j.{ Logger, LoggerFactory }

class TMapTripleSource(tmap: scala.collection.mutable.Map[String, PlantainLDPR]) extends TripleSource {

  def getValueFactory(): org.openrdf.model.ValueFactory = ???

  def getStatements(subject: Resource, predicate: SesameURI, objectt: Value, contexts: Resource*): CloseableIteration[Statement, QueryEvaluationException] = {
    val iterator: Iterator[Statement] = if (contexts.isEmpty) {
      for {
        ldpr <- tmap.values.iterator
        statement <- ldpr.graph.getStatements(subject, predicate, objectt).toIterator
      } yield {
        statement.withContext(ldpr.uri.asSesame.asInstanceOf[Resource])
      }
    } else {
      for {
        context <- contexts.iterator
        if context.isInstanceOf[SesameURI]
        uri = context.asInstanceOf[SesameURI]
        ldpr <- tmap.lift(Node.fromSesame(uri).lastPathSegment).toIterator
        statement <- ldpr.graph.getStatements(subject, predicate, objectt).toIterator
      } yield {
        statement.withContext(uri)
      }
    }
    iterator.toCloseableIteration[QueryEvaluationException]
  }


}
