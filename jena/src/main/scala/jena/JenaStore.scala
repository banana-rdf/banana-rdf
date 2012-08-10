package org.w3.banana.jena

import org.w3.banana._
import JenaOperations._
import JenaDiesel._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.sparql.modify.GraphStoreBasic
import com.hp.hpl.jena.datatypes.{ TypeMapper, RDFDatatype }
import scalaz.{ Validation, Success, Failure }
import scala.collection.JavaConverters._
import com.hp.hpl.jena.rdf.model.ModelFactory.createModelForGraph

object JenaStore {

  def apply(dataset: Dataset, defensiveCopy: Boolean): JenaStore =
    new JenaStore(dataset, defensiveCopy)

  def apply(dg: DatasetGraph, defensiveCopy: Boolean = false): JenaStore = {
    val dataset = new GraphStoreBasic(dg).toDataset
    JenaStore(dataset, defensiveCopy)
  }

}

class JenaStore(dataset: Dataset, defensiveCopy: Boolean) extends RDFStore[Jena] {

  // there is a huge performance impact when using transaction (READs are 3x slower)
  // it's fine to deactivate that if there is only one active thread with akka
  // we should find something clever here...
  val supportsTransactions: Boolean = false // dataset.supportsTransactions()

  val dg: DatasetGraph = dataset.asDatasetGraph

  val modelForBindings = ModelFactory.createDefaultModel()

  val typeMapper = TypeMapper.getInstance()

  def readTransaction[T](body: => T): T = {
    if (supportsTransactions) {
      dataset.begin(ReadWrite.READ)
      try {
        body
      } finally {
        dataset.end()
      }
    } else {
      body
    }
  }

  def writeTransaction[T](body: => T): T = {
    if (supportsTransactions) {
      dataset.begin(ReadWrite.WRITE)
      try {
        val result = body
        dataset.commit()
        result
      } finally {
        dataset.end()
      }
    } else {
      body
    }
  }

  def appendToGraph(uri: Jena#URI, graph: Jena#Graph): Unit = writeTransaction {
    graphToIterable(graph) foreach {
      case Triple(s, p, o) =>
        dg.add(uri, s, p, o)
    }
  }

  def patchGraph(uri: Jena#URI, delete: Jena#Graph, insert: Jena#Graph): Unit = writeTransaction {
    graphToIterable(delete) foreach {
      case Triple(s, p, o) =>
        dg.delete(uri, s, p, o)
    }
    graphToIterable(insert) foreach {
      case Triple(s, p, o) =>
        dg.add(uri, s, p, o)
    }
  }

  def getGraph(uri: Jena#URI): Jena#Graph = readTransaction {
    val graph = BareJenaGraph(dg.getGraph(uri))
    if (defensiveCopy)
      JenaUtil.copy(graph)
    else
      graph
  }

  def removeGraph(uri: Jena#URI): Unit = writeTransaction {
    dg.removeGraph(uri)
  }

  // oh my, what have I done to deserve this?
  def toRDFNode(node: Jena#Node): RDFNode = foldNode(node)(
    { case URI(str) => modelForBindings.createResource(str) },
    { case BNode(label) => modelForBindings.createResource(AnonId.create(label)) },
    {
      _.fold(
        { case TypedLiteral(lexicalForm, URI(datatype)) => modelForBindings.createTypedLiteral(lexicalForm, typeMapper.getSafeTypeByName(datatype)) },
        { case LangLiteral(lexicalForm, Lang(lang)) => modelForBindings.createLiteral(lexicalForm, lang) }
      )
    }
  )

  def querySolutionMap(bindings: Map[String, Jena#Node]): QuerySolutionMap = {
    val map = new QuerySolutionMap()
    bindings foreach {
      case (name, node) =>
        map.add(name, toRDFNode(node))
    }
    map
  }

  def executeSelect(query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Jena#Solutions = readTransaction {
    val qexec: QueryExecution =
      if (bindings.isEmpty)
        QueryExecutionFactory.create(query, dataset)
      else
        QueryExecutionFactory.create(query, dataset, querySolutionMap(bindings))
    val solutions = qexec.execSelect()
    solutions
  }

  def executeConstruct(query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Jena#Graph = readTransaction {
    val qexec: QueryExecution =
      if (bindings.isEmpty)
        QueryExecutionFactory.create(query, dataset)
      else
        QueryExecutionFactory.create(query, dataset, querySolutionMap(bindings))
    val result = qexec.execConstruct()
    BareJenaGraph(result.getGraph())
  }

  def executeAsk(query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Boolean = readTransaction {
    val qexec: QueryExecution =
      if (bindings.isEmpty)
        QueryExecutionFactory.create(query, dataset)
      else
        QueryExecutionFactory.create(query, dataset, querySolutionMap(bindings))
    val result = qexec.execAsk()
    result
  }

}

