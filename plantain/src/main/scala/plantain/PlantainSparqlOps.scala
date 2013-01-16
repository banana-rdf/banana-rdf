package org.w3.banana.plantain

import org.w3.banana._
import org.openrdf.query.parser.sparql.SPARQLParserFactory
import org.openrdf.query.parser.{ ParsedBooleanQuery, ParsedGraphQuery, ParsedTupleQuery }
import scala.collection.JavaConverters._
import scala.util._

object PlantainSparqlOps extends SparqlOps[Plantain] {

  private val p = new SPARQLParserFactory().getParser()

  def SelectQuery(query: String): Plantain#SelectQuery =
    p.parseQuery(query, "http://todo.example/").asInstanceOf[ParsedTupleQuery]

  def ConstructQuery(query: String): Plantain#ConstructQuery =
    p.parseQuery(query, "http://todo.example/").asInstanceOf[ParsedGraphQuery]

  def AskQuery(query: String): Plantain#AskQuery =
    p.parseQuery(query, "http://todo.example/").asInstanceOf[ParsedBooleanQuery]

  def Query(query: String): Try[Plantain#Query] = Try {
    p.parseQuery(query, "http://todo.example/")
  }

  def fold[T](query: Plantain#Query)(
    select: (Plantain#SelectQuery) => T,
    construct: (Plantain#ConstructQuery) => T,
    ask: Plantain#AskQuery => T) =
    query match {
      case qs: Plantain#SelectQuery => select(qs)
      case qc: Plantain#ConstructQuery => construct(qc)
      case qa: Plantain#AskQuery => ask(qa)
    }

  def getNode(solution: Plantain#Solution, v: String): Try[Plantain#Node] = {
    val node = solution.getValue(v)
    if (node == null)
      Failure(VarNotFound(s"""var $v not found in BindingSet $solution"""))
    else
      Success(Node.fromSesame(node))
  }

  def varnames(solution: Plantain#Solution): Set[String] = solution.getBindingNames.asScala.toSet

  def solutionIterator(solutions: Plantain#Solutions): Iterable[Plantain#Solution] = solutions.iterator.toIterable

}
