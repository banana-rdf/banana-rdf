package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.query.parser.sparql.SPARQLParserFactory
import org.openrdf.query.parser.{ ParsedBooleanQuery, ParsedGraphQuery, ParsedTupleQuery }
import org.openrdf.query.MalformedQueryException
import scala.collection.JavaConverters._
import scala.util._

object SesameSparqlOps extends SparqlOps[Sesame] {

  private val p = new SPARQLParserFactory().getParser()

  def SelectQuery(query: String): Sesame#SelectQuery =
    p.parseQuery(query, "http://todo.example/").asInstanceOf[ParsedTupleQuery]

  def ConstructQuery(query: String): Sesame#ConstructQuery =
    p.parseQuery(query, "http://todo.example/").asInstanceOf[ParsedGraphQuery]

  def AskQuery(query: String): Sesame#AskQuery =
    p.parseQuery(query, "http://todo.example/").asInstanceOf[ParsedBooleanQuery]

  def Query(query: String): Try[Sesame#Query] = Try {
    p.parseQuery(query, "http://todo.example/")
  }

  def fold[T](query: Sesame#Query)(select: (Sesame#SelectQuery) => T,
    construct: (Sesame#ConstructQuery) => T,
    ask: Sesame#AskQuery => T) =
    query match {
      case qs: Sesame#SelectQuery => select(qs)
      case qc: Sesame#ConstructQuery => construct(qc)
      case qa: Sesame#AskQuery => ask(qa)
    }

  def getNode(solution: Sesame#Solution, v: String): Try[Sesame#Node] = {
    val node = solution.getValue(v)
    if (node == null)
      Failure(VarNotFound("var " + v + " not found in BindingSet " + solution.toString))
    else
      Success(node)
  }

  def varnames(solution: Sesame#Solution): Set[String] = solution.getBindingNames.asScala.toSet

  def solutionIterator(solutions: Sesame#Solutions): Iterable[Sesame#Solution] = solutions

}
