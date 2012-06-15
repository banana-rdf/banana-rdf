package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.graph.{Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _}
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.collection.JavaConverters._

object JenaGraphQuery extends RDFGraphQuery[Jena, JenaSPARQL] {

  def executeSelect(graph: JenaGraph, query: JenaSPARQL#SelectQuery): Iterable[PartialFunction[String, Jena#Node]] = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val solutions: java.util.Iterator[QuerySolution] = qexec.execSelect()
    val pfs = solutions.asScala map JenaSPARQLEngine.toPartialFunction
    new Iterable[PartialFunction[String, Jena#Node]] {
      def iterator = pfs
    }
  }

  def executeConstruct(graph: JenaGraph, query: JenaSPARQL#ConstructQuery): JenaGraph = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val result = qexec.execConstruct()
    result.getGraph()
  }
  
  def executeAsk(graph: JenaGraph, query: JenaSPARQL#AskQuery): Boolean = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val result = qexec.execAsk()
    result
  }

}
