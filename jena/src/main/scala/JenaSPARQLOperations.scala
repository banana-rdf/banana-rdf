package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.query.{Query => JenaQuery, QueryException, QueryFactory}
import com.hp.hpl.jena.graph.{ Node => JenaNode }
import com.hp.hpl.jena.rdf.model.RDFNode
import scalaz.{Failure, Success, Validation}
import scala.collection.JavaConverters._

object JenaSPARQLOperations extends SPARQLOperations[Jena, JenaSPARQL] {

  def SelectQuery(query: String): JenaSPARQL#SelectQuery = QueryFactory.create(query)

  def ConstructQuery(query: String): JenaSPARQL#ConstructQuery = QueryFactory.create(query)

  def AskQuery(query: String): JenaSPARQL#AskQuery = QueryFactory.create(query)

  def Query(query: String): Validation[Exception, JenaSPARQL#Query] = try {
    Success(QueryFactory.create(query))
  } catch {
    case e: QueryException => Failure(e)
  }

  def fold[T](query: JenaSPARQL#Query)(select: (JenaSPARQL#SelectQuery) => T,
                                       construct: (JenaSPARQL#ConstructQuery) => T,
                                       ask: (JenaSPARQL#AskQuery) => T) =
    query.getQueryType match {
      case JenaQuery.QueryTypeSelect => select(query)
      case JenaQuery.QueryTypeConstruct => construct(query)
      case JenaQuery.QueryTypeAsk => ask(query)
    }

  def Rows(solutions: JenaSPARQL#Solutions) = new Iterable[Row[Jena]] {
    def iterator = solutions.asScala map JenaSPARQLEngine.toRow
  }
}
