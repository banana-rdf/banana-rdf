package org.w3.banana.ldp

import org.openrdf.model.{ URI => SesameURI, _ }
import org.openrdf.query.algebra.evaluation.TripleSource
import org.openrdf.query.QueryEvaluationException
import info.aduna.iteration.CloseableIteration
import org.w3.banana.plantain.model.Node
import org.w3.banana.plantain.PlantainUtil._
import org.w3.banana.plantain.Plantain
import org.w3.banana.syntax

class TMapTripleSource(tmap: scala.collection.mutable.Map[String, LocalLDPR[Plantain]]) extends TripleSource {
  import syntax.URISyntax.uriW

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
        ldpr <- tmap.lift(uriW[Plantain](Node.fromSesame(uri)).lastPathSegment).toIterator
        statement <- ldpr.graph.getStatements(subject, predicate, objectt).toIterator
      } yield {
        statement.withContext(uri)
      }
    }
    iterator.toCloseableIteration[QueryEvaluationException]
  }


}
