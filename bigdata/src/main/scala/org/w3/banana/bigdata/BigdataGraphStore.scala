package org.w3.banana.bigdata

import com.bigdata.rdf.model.{BigdataResource, BigdataStatement}
import com.bigdata.rdf.sail.BigdataSailRepositoryConnection
import com.sun.org.apache.xpath.internal.operations.Variable
import org.openrdf.model.{URI, Resource, Value, Statement}
import org.openrdf.query.{GraphQueryResult, BindingSet, TupleQueryResult}
import org.openrdf.repository.{RepositoryResult, RepositoryConnection}
import org.w3.banana._
import scala.collection.immutable.{Map, List}
import scala.util.Try
import scala.collection.JavaConversions._


class BigdataGraphStore extends GraphStore[Bigdata, Try,BigdataSailRepositoryConnection] with BigdataTransactor {

  val ops = implicitly[RDFOps[Bigdata]]


  def appendToGraph(conn: BigdataSailRepositoryConnection, uri: Bigdata#URI, graph: Bigdata#Graph): Try[Unit] = this.rw(conn, () => {
    graph.triples.foreach(t => conn.add(t.getSubject, t.getPredicate, t.getObject, t.getContext))
  })

  /*
  def removeTriples(conn: RepositoryConnection, uri: Bigdata#URI, tripleMatches: Iterable[TripleMatch[Bigdata]]): Try[Unit] = Try {
  val ts = tripleMatches.map {
    case (s, p, o) => new StatementImpl(s.asInstanceOf[Resource], p.asInstanceOf[URI], o)
  }
  conn.remove(ts.asJava, uri)
  }*/

  def getGraph(conn: BigdataSailRepositoryConnection, uri: Bigdata#URI): Try[Bigdata#Graph] = this.r(conn, {
    conn.getStatements(null, null, null, true, uri).foldLeft(ops.emptyGraph)(
      (acc, el) => el match {
        case b: Bigdata#Triple => acc + b
        case _ => //getStatement that returns BigdataStatement is private, although it is used underneath Sesame getStatement
          //that means that this _=> case will neve occur
          acc
      }
    )
  }
  )


  def removeGraph(conn: BigdataSailRepositoryConnection, uri: Bigdata#URI): Try[Unit] = this.rw(conn, () => {
    conn.remove(null: Resource, null: Bigdata#URI, null, uri)
  })

  /**
   * To the graph at `uri`, removes the matching triples
   */
  override def removeTriples(conn: BigdataSailRepositoryConnection, uri: Bigdata#URI, triples: Iterable[(Bigdata#NodeMatch, Bigdata#NodeMatch, Bigdata#NodeMatch)]): Try[Unit] =
    rw(conn,()=> triples foreach {
      case (s: BigdataResource, p: Bigdata#URI, o) => conn.remove(s, p, o, uri)
      case other => throw new Exception(
        """
            |Triples removal broke because unlike in Banana-RDF Sesame-based RDF stores
            |support only Resource as subject and URI as property"
          """.stripMargin
      )
    } )

}
