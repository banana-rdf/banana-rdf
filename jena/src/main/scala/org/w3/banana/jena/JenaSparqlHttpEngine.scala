package org.w3.banana.jena

import java.net.URL

import org.apache.jena.graph.{Graph => JenaGraph}
import org.apache.jena.query._
import org.apache.jena.update.UpdateExecutionFactory
import org.w3.banana._
import scala.util._

class JenaSparqlHttpEngine(implicit ops: RDFOps[Jena])
    extends SparqlEngine[Jena, Try, URL] with SparqlUpdate[Jena, Try, URL] {

  val querySolution = new util.QuerySolution(ops)

  def qexec(endpoint: URL, query: Jena#Query, bindings: Map[String, Jena#Node]): QueryExecution = {
    val qe = QueryExecutionFactory.sparqlService(endpoint.toString, query)
    if (bindings.nonEmpty)
      qe.setInitialBinding(querySolution.getMap(bindings))
    qe
  }

  def executeAsk(endpoint: URL, query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Try[Boolean] =
    Try {
      qexec(endpoint, query, bindings).execAsk()
    }

  def executeConstruct(endpoint: URL, query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Try[Jena#Graph] =
    Try {
      qexec(endpoint, query, bindings).execConstruct().getGraph()
    }

  def executeSelect(endpoint: URL, query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Try[Jena#Solutions] =
    Try {
      qexec(endpoint, query, bindings).execSelect()
    }

  def executeUpdate(endpoint: URL, query: Jena#UpdateQuery, bindings: Map[String, Jena#Node]): Try[Unit] =
    Try {
      val ue = UpdateExecutionFactory.createRemote(query, endpoint.toString)
      // not sure how to set the bindings on ue
      //      if (bindings.nonEmpty)
      //        ue.
      ue.execute()
    }
}
