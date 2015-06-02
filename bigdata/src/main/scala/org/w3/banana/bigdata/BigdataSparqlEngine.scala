package org.w3.banana.bigdata

import com.bigdata.rdf.model.BigdataValue
import com.bigdata.rdf.sail._
import org.openrdf.query.{BindingSet, QueryLanguage}
import org.w3.banana.bigdata.extensions._
import org.w3.banana.{RDFOps, SparqlEngine}

import scala.util.Try

trait BigdataSparqlEngine extends SparqlEngine[Bigdata,Try,BigdataSailRepositoryConnection]  with BigdataTransactor {


  implicit def ops: RDFOps[Bigdata]

  implicit def config:BigdataConfig[Bigdata]


  /** Executes a Select query. */
  override def executeSelect(conn: BigdataSailRepositoryConnection,
                             query: Bigdata#SelectQuery,
                             bindings: Map[String, BigdataValue]): Try[Vector[BindingSet]] = this.r(conn,{
    val accumulator = new BindingsAccumulator()
    val tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query,config.basePrefix)
    bindings foreach { case (name, value) => tupleQuery.setBinding(name, value) }
    tupleQuery.evaluate(accumulator)
    accumulator.bindings()
  })

  /** Executes a Construct query. */
  override def executeConstruct(conn: BigdataSailRepositoryConnection,
                                query: Bigdata#ConstructQuery,
                                bindings: Map[String, BigdataValue]): Try[BigdataGraph] = this.r(conn,{
    val graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL,query,config.basePrefix)
    val result = graphQuery.evaluate()
    val g = ops.makeGraph(result.toIterable.collect{case s:Bigdata#Triple=>s})
    g
  })

  /** Executes a Ask query. */
  override def executeAsk(conn: BigdataSailRepositoryConnection, query: Bigdata#AskQuery, bindings: Map[String, BigdataValue]): Try[Boolean] = this.r(conn,{
    conn.prepareBooleanQuery(QueryLanguage.SPARQL,query,config.basePrefix).evaluate()
  })
}