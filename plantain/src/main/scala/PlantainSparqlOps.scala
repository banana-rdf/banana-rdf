package org.w3.banana.plantain

import org.openrdf.query.parser.sparql.SPARQLParserFactory
import org.openrdf.query.parser.{ ParsedBooleanQuery, ParsedGraphQuery, ParsedTupleQuery }
import org.w3.banana.SparqlOps._
import org.w3.banana.plantain.model._
import org.w3.banana.{ Prefix, SparqlOps, VarNotFound }
import scala.collection.JavaConverters._

import scala.util._

object PlantainSparqlOps extends SparqlOps[Plantain] {

  private val p = new SPARQLParserFactory().getParser()

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

  def solutionIterator(solutions: Plantain#Solutions): Iterator[Plantain#Solution] = solutions.iterator.toIterator

  override def parseSelect(query: String, prefixes: Seq[Prefix[Plantain]]) = Try {
    p.parseQuery(withPrefixes(query, prefixes), "http://todo.example/").asInstanceOf[ParsedTupleQuery]
  }

  override def parseConstruct(query: String, prefixes: Seq[Prefix[Plantain]]) = Try {
    p.parseQuery(withPrefixes(query, prefixes), "http://todo.example/").asInstanceOf[ParsedGraphQuery]
  }

  /**
   * A general query constructor.
   *
   * When this is used it is usually because the query type is not
   * known in advance, ( as when a query is received over the
   * internet). As a result the response is a validation, as the
   * query may not have been tested for validity.
   *
   * @param query a Sparql query
   * @return A validation containing the Query
   */
  override def parseQuery(query: String, prefixes: Seq[Prefix[Plantain]]) = Try {
    p.parseQuery(withPrefixes(query, prefixes), "http://todo.example/")
  }

  override def parseAsk(query: String, prefixes: Seq[Prefix[Plantain]]) = Try {
    p.parseQuery(withPrefixes(query, prefixes), "http://todo.example/").asInstanceOf[ParsedBooleanQuery]

  }

  override def parseUpdate(query: String, prefixes: Seq[Prefix[Plantain]]) = Try {
    p.parseUpdate(withPrefixes(query, prefixes), "http://todo.example/")
    SesameParseUpdate(query)
  }

}
