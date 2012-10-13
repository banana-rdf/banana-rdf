package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.query.{ Query => JenaQuery, QueryException, QueryFactory }
import com.hp.hpl.jena.graph.{ Node => JenaNode }
import com.hp.hpl.jena.rdf.model.RDFNode
import scala.collection.JavaConverters._
import scala.util._

object JenaSparqlOps extends SparqlOps[Jena] {

  def SelectQuery(query: String): Jena#SelectQuery = QueryFactory.create(query)

  def ConstructQuery(query: String): Jena#ConstructQuery = QueryFactory.create(query)

  def AskQuery(query: String): Jena#AskQuery = QueryFactory.create(query)

  def Query(query: String): Try[Jena#Query] = Try {
    QueryFactory.create(query)
  }

  def fold[T](query: Jena#Query)(select: Jena#SelectQuery => T,
    construct: Jena#ConstructQuery => T,
    ask: Jena#AskQuery => T) =
    query.getQueryType match {
      case JenaQuery.QueryTypeSelect => select(query)
      case JenaQuery.QueryTypeConstruct => construct(query)
      case JenaQuery.QueryTypeAsk => ask(query)
    }

  def getNode(solution: Jena#Solution, v: String): Try[Jena#Node] = {
    val node: RDFNode = solution.get(v)
    if (node == null)
      Failure(VarNotFound("var " + v + " not found in QuerySolution " + solution.toString))
    else
      Success(JenaUtil.toNode(node))
  }

  def varnames(solution: Jena#Solution): Set[String] = solution.varNames.asScala.toSet

  def solutionIterator(solutions: Jena#Solutions): Iterable[Jena#Solution] =
    solutions.asScala.toIterable

}
