package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.concurrent.Future
import scala.concurrent.Future.successful

class JenaSparqlHttpEngine(ops: RDFOps[Jena], endpointUrl: String) extends SparqlEngine[Jena] {

  val querySolution = new util.QuerySolution(ops)

  def qexec(query: Jena#Query, bindings: Map[String, Jena#Node]): QueryExecution = {
    val qe = QueryExecutionFactory.sparqlService(endpointUrl, query)
    if (bindings.nonEmpty)
      qe.setInitialBinding(querySolution.getMap(bindings))
    qe
  }

  def executeAsk(query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Future[Boolean] = successful {
    qexec(query, bindings).execAsk()
  }

  def executeConstruct(query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Future[Jena#Graph] = successful {
    qexec(query, bindings).execConstruct().getGraph()
  }

  def executeSelect(query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Future[Jena#Solutions] = successful {
     qexec(query, bindings).execSelect()
  }

}
