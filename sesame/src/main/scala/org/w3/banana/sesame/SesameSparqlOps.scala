package org.w3.banana.sesame

import org.openrdf.query.parser.sparql.SPARQLParserFactory
import org.openrdf.query.parser.{ ParsedBooleanQuery, ParsedGraphQuery, ParsedTupleQuery }
import org.w3.banana.SparqlOps.withPrefixes
import org.w3.banana._

import scala.collection.JavaConverters._
import scala.util._

object SesameSparqlOps extends SparqlOps[Sesame] {

  private val p = new SPARQLParserFactory().getParser()

  def parseSelect(query: String, prefixes: Seq[Prefix[Sesame]]): Try[Sesame#SelectQuery] = Try {
    p.parseQuery(withPrefixes(query, prefixes), "http://todo.example/").asInstanceOf[ParsedTupleQuery]
  }

  def parseConstruct(query: String, prefixes: Seq[Prefix[Sesame]]): Try[Sesame#ConstructQuery] = Try {
    p.parseQuery(withPrefixes(query, prefixes), "http://todo.example/").asInstanceOf[ParsedGraphQuery]
  }

  def parseAsk(query: String, prefixes: Seq[Prefix[Sesame]]): Try[Sesame#AskQuery] = Try {
    p.parseQuery(withPrefixes(query, prefixes), "http://todo.example/").asInstanceOf[ParsedBooleanQuery]
  }

  //FIXME
  def parseUpdate(query: String, prefixes: Seq[Prefix[Sesame]]): Try[Sesame#UpdateQuery] = Try {
    p.parseUpdate(withPrefixes(query, prefixes), "http://todo.example/")
    SesameParseUpdate(query)
  }

  def parseQuery(query: String, prefixes: Seq[Prefix[Sesame]]): Try[Sesame#Query] = Try {
    p.parseQuery(withPrefixes(query, prefixes), "http://todo.example/")
  }

  def fold[T](
    query: Sesame#Query)(
      select: (Sesame#SelectQuery) => T,
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

  def solutionIterator(solutions: Sesame#Solutions): Iterator[Sesame#Solution] = solutions.iterator

}
