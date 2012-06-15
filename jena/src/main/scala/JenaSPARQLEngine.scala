package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.collection.JavaConverters._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.sparql.modify.GraphStoreBasic
import scalaz.{Right3, Middle3, Left3}


object JenaSPARQLEngine {

  def toPartialFunction(qs: QuerySolution): PartialFunction[String, Jena#Node] =
    new PartialFunction[String, Jena#Node] {
      def apply(v: String): Jena#Node = {
        val node: RDFNode = qs.get(v)
        JenaGraphTraversal.toNode(node)
      }
      def isDefinedAt(v: String): Boolean =
        qs.contains(v)
    }

}

trait JenaSPARQLEngine extends SPARQLEngine[Jena, JenaSPARQL] {

  def store: DatasetGraph

  def executeSelect(query: JenaSPARQL#SelectQuery): Iterable[PartialFunction[String, Jena#Node]] = {
    val dataset = new GraphStoreBasic(store).toDataset
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val solutions: java.util.Iterator[QuerySolution] = qexec.execSelect()
    val pfs = solutions.asScala map JenaSPARQLEngine.toPartialFunction
    new Iterable[PartialFunction[String, Jena#Node]] {
      def iterator = pfs
    }
  }

  def executeConstruct(query: JenaSPARQL#ConstructQuery): JenaGraph = {
    val dataset = new GraphStoreBasic(store).toDataset
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val result = qexec.execConstruct()
    result.getGraph()
  }

  def executeAsk(query: JenaSPARQL#AskQuery): Boolean = {
    val dataset = new GraphStoreBasic(store).toDataset
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val result = qexec.execAsk()
    result
  }


}
