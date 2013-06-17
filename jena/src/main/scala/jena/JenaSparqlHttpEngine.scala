package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scalaz.Id._

class JenaSparqlHttpEngine(val endpointUrl: String) extends SparqlEngine[Jena, Id] {

  val querySolution = util.QuerySolution()

  def qexec(query: Jena#Query, bindings: Map[String, Jena#Node]): QueryExecution = {
    val qe = QueryExecutionFactory.sparqlService(endpointUrl, query)
    if (bindings.nonEmpty)
      qe.setInitialBinding(querySolution.getMap(bindings))
    qe
  }

  def executeAsk(query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Boolean =
    qexec(query, bindings).execAsk()

  def executeConstruct(query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Jena#Graph =
    BareJenaGraph(qexec(query, bindings).execConstruct().getGraph())

  def executeSelect(query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Jena#Solutions =
     qexec(query, bindings).execSelect()

  // FIXME added just to avoid compilation error
  def executeUpdate(query: Jena#UpdateQuery, bindings: Map[String, Jena#Node]): Unit = ???
}
