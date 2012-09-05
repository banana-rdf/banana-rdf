package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scalaz.Id._

object JenaGraphSPARQLEngine extends RDFGraphQuery[Jena] {

  def makeSPARQLEngine(graph: Jena#Graph): SPARQLEngine[Jena, Id] = new SPARQLEngine[Jena, Id] {

    lazy val querySolution = util.QuerySolution()

    def executeSelect(query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Jena#Solutions = {
      val model: Model = ModelFactory.createModelForGraph(graph.jenaGraph)
      val qexec: QueryExecution =
        if (bindings.isEmpty)
          QueryExecutionFactory.create(query, model)
        else
          QueryExecutionFactory.create(query, model, querySolution.getMap(bindings))
      val solutions = qexec.execSelect()
      solutions
    }
    
    def executeConstruct(query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Jena#Graph = {
      val model: Model = ModelFactory.createModelForGraph(graph.jenaGraph)
      val qexec: QueryExecution =
        if (bindings.isEmpty)
          QueryExecutionFactory.create(query, model)
        else
          QueryExecutionFactory.create(query, model, querySolution.getMap(bindings))
      val result = qexec.execConstruct()
      BareJenaGraph(result.getGraph())
    }
    
    def executeAsk(query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Boolean = {
      val model: Model = ModelFactory.createModelForGraph(graph.jenaGraph)
      val qexec: QueryExecution =
        if (bindings.isEmpty)
          QueryExecutionFactory.create(query, model)
        else
          QueryExecutionFactory.create(query, model, querySolution.getMap(bindings))
      val result = qexec.execAsk()
      result
    }
  }

}
