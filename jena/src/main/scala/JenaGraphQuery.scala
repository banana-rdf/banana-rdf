package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.graph.{Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _}
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.collection.JavaConverters._

trait JenaRDFGraphQuery extends RDFGraphQuery[Jena, JenaSPARQL] {

  lazy val model: Model =  ModelFactory.createModelForGraph(graph)

  def executeSelect(query: JenaSPARQL#SelectQuery): Iterable[Row[Jena]] = {
    val rows = executeSelectPlain(query).asScala map JenaSPARQLEngine.toRow
    new Iterable[Row[Jena]] {
      def iterator = rows
    }
  }

  def executeConstruct(query: JenaSPARQL#ConstructQuery): JenaGraph = {
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val result = qexec.execConstruct()
    result.getGraph()
  }
  
  def executeAsk(query: JenaSPARQL#AskQuery): Boolean = {
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val result = qexec.execAsk()
    result
  }

  /**
   * This returns the underlying objects, which is useful when needing to serialise the answer
   * for example
   * @param query
   * @return
   */
  def executeSelectPlain(query: JenaSPARQL#SelectQuery) = {
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    qexec.execSelect()
  }
}

case class JenaGraphQuery(val graph: Jena#Graph) extends JenaRDFGraphQuery

case class OpenJenaGraphQuery(val graph: Jena#Graph) extends JenaRDFGraphQuery with OpenGraphQuery[Jena,JenaSPARQL] {
  def ops = JenaSPARQLOperations
}