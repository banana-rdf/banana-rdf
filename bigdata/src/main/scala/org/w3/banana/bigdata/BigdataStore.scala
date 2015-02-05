package org.w3.banana.bigdata

import java.io.File

import com.bigdata.rdf.sail.{BigdataSailRepositoryConnection, BigdataSail}
import org.w3.banana.RDFStore

import scala.util.Try
/*


class BigdataStore[Rdf<:Bigdata](implicit config:BigdataConfig[Rdf]) extends RDFStore[Rdf, Try, BigdataSailRepositoryConnection ]
{

/*

  lazy val sail: BigdataSail = {
    val journal = new File(config.journal)
    if(!journal.exists())journal.createNewFile()
    val properties = config.properties
    properties.setProperty(BigdataSail.Options.DEFAULT_FILE, journal.getAbsolutePath)
    new BigdataSail(properties)
  }
*/

  override def r[T](con: BigdataSailRepositoryConnection , body: => T): Try[T] = {
    val result: Try[T] = Try { body  }
    con.close()
    result
  }

  override def rw[T](con: BigdataSailRepositoryConnection , body: => T): Try[T] = Try{
    con.setAutoCommit(false) //deprecated by sesame but used by Bigdata
    val result = body
    con.commit() //we have to commit the changes
    con.close()
    result
  }


  override def executeConstruct(con: BigdataSailRepositoryConnection , query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node]): Try[Rdf#Graph] = {
    con.prepare
    val graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL, query.getSourceString)
    bindings foreach { case (name, value) => graphQuery.setBinding(name, value) }
    val result: GraphQueryResult = graphQuery.evaluate()
    val graph = new LinkedHashModel
    while (result.hasNext) {
      graph.add(result.next())
    }
    result.close()
    graph
  }

  override def executeSelect(con: BigdataSailRepositoryConnection , query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node]): Try[Rdf#Solutions] = ???

  override def executeAsk(con: BigdataSailRepositoryConnection , query: Rdf#AskQuery, bindings: Map[String, Rdf#Node]): Try[Boolean] = ???
  

  override def removeTriples(con: BigdataSailRepositoryConnection , uri: Rdf#URI, triples: Iterable[(Rdf#NodeMatch, Rdf#NodeMatch, Rdf#NodeMatch)]): Try[Unit] = ???

  override def appendToGraph(con: BigdataSailRepositoryConnection , uri: Rdf#URI, graph: Rdf#Graph): Try[Unit] = ???

  override def removeGraph(con: BigdataSailRepositoryConnection , uri: Rdf#URI): Try[Unit] = ???

  override def getGraph(con: BigdataSailRepositoryConnection , uri: Rdf#URI): Try[Rdf#Graph] = ???

}
*/
