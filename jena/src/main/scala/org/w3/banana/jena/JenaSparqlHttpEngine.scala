package org.w3.banana.jena

import java.net.URL

import com.hp.hpl.jena.graph.{Graph => JenaGraph}
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.update.UpdateExecutionFactory
import org.w3.banana._

import scala.concurrent._

class JenaSparqlHttpEngine(implicit ops: RDFOps[Jena], ec: ExecutionContext)
    extends SparqlEngine[Jena, Future, URL] with SparqlUpdate[Jena, Future, URL] {

  val querySolution = new util.QuerySolution(ops)

  def qexec(endpoint: URL, query: Jena#Query, bindings: Map[String, Jena#Node]): QueryExecution = {
    val qe = QueryExecutionFactory.sparqlService(endpoint.toString, query)
    if (bindings.nonEmpty)
      qe.setInitialBinding(querySolution.getMap(bindings))
    qe
  }

  def executeAsk(endpoint: URL, query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Future[Boolean] =
    Future {
      qexec(endpoint, query, bindings).execAsk()
    }

  def executeConstruct(endpoint: URL, query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Future[Jena#Graph] =
    Future {
      qexec(endpoint, query, bindings).execConstruct().getGraph()
    }

  def executeSelect(endpoint: URL, query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Future[Jena#Solutions] =
    Future {
      qexec(endpoint, query, bindings).execSelect()
    }

  def executeUpdate(endpoint: URL, query: Jena#UpdateQuery, bindings: Map[String, Jena#Node]): Future[URL] =
    Future {
      val ue = UpdateExecutionFactory.createRemote(query, endpoint.toString)
      // not sure how to set the bindings on ue
      //      if (bindings.nonEmpty)
      //        ue.
      ue.execute()
      endpoint
    }
}
