package org.w3.banana.plantain

import org.w3.banana._

import com.hp.hpl.jena.query.{ Query => JenaQuery, QueryException, QueryFactory }
import com.hp.hpl.jena.graph.{ Node => JenaNode }
import com.hp.hpl.jena.rdf.model.RDFNode
import scala.collection.JavaConverters._
import scala.util._
import org.w3.banana.jena.JenaUtil.toNode

object PlantainSparqlOps extends SparqlOps[Plantain] {

  def SelectQuery(query: String): Plantain#SelectQuery = QueryFactory.create(query)

  def ConstructQuery(query: String): Plantain#ConstructQuery = QueryFactory.create(query)

  def AskQuery(query: String): Plantain#AskQuery = QueryFactory.create(query)

  def Query(query: String): Try[Plantain#Query] = Try {
    QueryFactory.create(query)
  }

  def fold[T](query: Plantain#Query)(select: Plantain#SelectQuery => T,
    construct: Plantain#ConstructQuery => T,
    ask: Plantain#AskQuery => T) =
    query.getQueryType match {
      case JenaQuery.QueryTypeSelect => select(query)
      case JenaQuery.QueryTypeConstruct => construct(query)
      case JenaQuery.QueryTypeAsk => ask(query)
    }

  def getNode(solution: Plantain#Solution, v: String): Try[Plantain#Node] = {
    val node: RDFNode = solution.get(v)
    if (node == null)
      Failure(VarNotFound("var " + v + " not found in QuerySolution " + solution.toString))
    else
      Success(Node.fromJena(toNode(node)))
  }

  def varnames(solution: Plantain#Solution): Set[String] = solution.varNames.asScala.toSet

  def solutionIterator(solutions: Plantain#Solutions): Iterable[Plantain#Solution] =
    solutions.asScala.toIterable

}
