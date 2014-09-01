package org.w3.banana.jena

import com.hp.hpl.jena.graph.{Graph => JenaGraph}
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf.model._
import java.net.URL
import org.w3.banana._
import scala.concurrent._

class JenaSparqlHttpEngine(implicit ops: RDFOps[Jena], ec: ExecutionContext) extends SparqlEngine[Jena, URL] {

  val querySolution = new util.QuerySolution(ops)

  def qexec(endpoint: URL, query: Jena#Query, bindings: Map[String, Jena#Node]): QueryExecution = {
    val qe = QueryExecutionFactory.sparqlService(endpoint.toString, query)
    if (bindings.nonEmpty)
      qe.setInitialBinding(querySolution.getMap(bindings))
    qe
  }

  def executeAsk(endpoint: URL, query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Future[Boolean] = Future {
    qexec(endpoint, query, bindings).execAsk()
  }

  def executeConstruct(endpoint: URL, query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Future[Jena#Graph] = Future {
    qexec(endpoint, query, bindings).execConstruct().getGraph()
  }

  def executeSelect(endpoint: URL, query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Future[Jena#Solutions] = Future {
    qexec(endpoint, query, bindings).execSelect()
  }

}
