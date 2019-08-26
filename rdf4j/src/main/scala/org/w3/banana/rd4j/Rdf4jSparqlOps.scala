package org.w3.banana.rd4j

import org.eclipse.rdf4j.query.parser.{ParsedBooleanQuery, ParsedGraphQuery, ParsedTupleQuery, QueryParserUtil}
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParserFactory
import org.w3.banana.SparqlOps.withPrefixes
import org.w3.banana._

import scala.util._
import scala.collection.JavaConverters._

object Rdf4jSparqlOps extends SparqlOps[Rdf4j] {

  private val p = new SPARQLParserFactory().getParser()

  def parseSelect(query: String, prefixes: Seq[Prefix[Rdf4j]]): Try[Rdf4j#SelectQuery] = Try {
    p.parseQuery(withPrefixes(query, prefixes), "http://todo.example/").asInstanceOf[ParsedTupleQuery]
  }

  def parseConstruct(query: String, prefixes: Seq[Prefix[Rdf4j]]): Try[Rdf4j#ConstructQuery] = Try {
    p.parseQuery(withPrefixes(query, prefixes), "http://todo.example/").asInstanceOf[ParsedGraphQuery]
  }

  def parseAsk(query: String, prefixes: Seq[Prefix[Rdf4j]]): Try[Rdf4j#AskQuery] = Try {
    p.parseQuery(withPrefixes(query, prefixes), "http://todo.example/").asInstanceOf[ParsedBooleanQuery]
  }

  def parseUpdate(query: String, prefixes: Seq[Prefix[Rdf4j]]): Try[Rdf4j#UpdateQuery] = Try {
    p.parseUpdate(withPrefixes(query, prefixes), "http://todo.example/")
  }

  def parseQuery(query: String, prefixes: Seq[Prefix[Rdf4j]]): Try[Rdf4j#Query] = Try {
    p.parseQuery(withPrefixes(query, prefixes), "http://todo.example/")
  }

  def fold[T](
    query: Rdf4j#Query)(
      select: (Rdf4j#SelectQuery) => T,
      construct: (Rdf4j#ConstructQuery) => T,
      ask: Rdf4j#AskQuery => T) =
    query match {
      case qs: Rdf4j#SelectQuery => select(qs)
      case qc: Rdf4j#ConstructQuery => construct(qc)
      case qa: Rdf4j#AskQuery => ask(qa)
    }

  def getNode(solution: Rdf4j#Solution, v: String): Try[Rdf4j#Node] = {
    val node = solution.getValue(v)
    if (node == null)
      Failure(VarNotFound("var " + v + " not found in BindingSet " + solution.toString))
    else
      Success(node)
  }

  def varnames(solution: Rdf4j#Solution): Set[String] = solution.getBindingNames.asScala.toSet

  def solutionIterator(solutions: Rdf4j#Solutions): Iterator[Rdf4j#Solution] = solutions.iterator

}
