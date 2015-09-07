package org.w3.banana.bigdata

import com.bigdata.rdf.model.BigdataValue
import com.bigdata.rdf.sail.BigdataSailRepositoryConnection
import org.openrdf.query.{BindingSet, QueryLanguage, TupleQueryResultHandler}
import org.w3.banana.bigdata.extensions._
import org.w3.banana.{RDFOps, RDFStore}

import scala.util.Try

class BindingsAccumulator() extends TupleQueryResultHandler {

  private val builder = Vector.newBuilder[BindingSet]

  def endQueryResult(): Unit = ()

  def startQueryResult(bindingNames: java.util.List[String]): Unit = ()

  def handleSolution(bindingSet: BindingSet): Unit =
    builder += bindingSet

  def handleBoolean(boolean: Boolean): Unit = new UnsupportedOperationException

  def handleLinks(linkUrls: java.util.List[String]): Unit = new UnsupportedOperationException

  def bindings(): Vector[BindingSet] = builder.result()

}


class BigdataStore(implicit val ops:RDFOps[Bigdata])
  extends BigdataGraphStore
  with BigdataSparqlEngine
  with RDFStore[Bigdata, Try, BigdataSailRepositoryConnection] {

  /** Executes a Select query. */
  override def executeSelect(conn: BigdataSailRepositoryConnection,
                             query: Bigdata#SelectQuery,
                             bindings: Map[String, BigdataValue]): Try[Vector[BindingSet]] = this.r(conn,{
    val accumulator = new BindingsAccumulator()
    val tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query,BigdataConfig.basePrefix)
    bindings foreach { case (name, value) => tupleQuery.setBinding(name, value) }
    tupleQuery.evaluate(accumulator)
    accumulator.bindings()
  })

  /** Executes a Construct query. */
  override def executeConstruct(conn: BigdataSailRepositoryConnection,
                                query: Bigdata#ConstructQuery,
                                bindings: Map[String, BigdataValue]): Try[BigdataGraph] = this.r(conn,{
    val graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL,query,BigdataConfig.basePrefix)
    val result = graphQuery.evaluate()
    val g = ops.makeGraph(result.toIterable.collect{case s:Bigdata#Triple=>s})
    g
  })

  /** Executes a Ask query. */
  override def executeAsk(conn: BigdataSailRepositoryConnection, query: Bigdata#AskQuery, bindings: Map[String, BigdataValue]): Try[Boolean] = this.r(conn,{
    conn.prepareBooleanQuery(QueryLanguage.SPARQL,query,BigdataConfig.basePrefix).evaluate()
  })
}
