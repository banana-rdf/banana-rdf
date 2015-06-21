package org.w3.banana.bigdata

import com.bigdata.rdf.model.BigdataResource
import com.bigdata.rdf.sail.BigdataSailRepositoryConnection
import org.openrdf.model.Resource
import org.w3.banana._
import org.w3.banana.bigdata.extensions._

import scala.util.Try


class BigdataGraphStore(implicit ops:RDFOps[Bigdata]) extends GraphStore[Bigdata, Try,BigdataSailRepositoryConnection] with BigdataTransactor {

  def appendToGraph(conn: BigdataSailRepositoryConnection, uri: Bigdata#URI, graph: Bigdata#Graph): Try[Unit] = this.rw(conn,  {
    graph.triples.foreach(t => conn.add(t.getSubject, t.getPredicate, t.getObject, uri))
  })

  def getGraph(conn: BigdataSailRepositoryConnection, uri: Bigdata#URI): Try[Bigdata#Graph] = this.r(conn, {
    conn.getStatements(null, null, null, true, uri).foldLeft(ops.emptyGraph)(
      (acc, el) => el match {
        case b: Bigdata#Triple => acc + b
        case _ =>
          throw new IllegalArgumentException(
            """
              |getStatement that returns BigdataStatement is private, although it is used underneath Sesame getStatement
              |that means that this _=> case will never occur
            """.stripMargin
          )
      }
    )
  }
  )

  def removeGraph(conn: BigdataSailRepositoryConnection, uri: Bigdata#URI): Try[Unit] = this.rw(conn,  {
    conn.remove(null: Resource, null: Bigdata#URI, null, uri)
  })

  /**
   * To the graph at `uri`, removes the matching triples
   */
  override def removeTriples(conn: BigdataSailRepositoryConnection, uri: Bigdata#URI, triples: Iterable[(Bigdata#NodeMatch, Bigdata#NodeMatch, Bigdata#NodeMatch)]): Try[Unit] =
    rw(conn, triples foreach {
      case (s: BigdataResource, p: Bigdata#URI, o) => conn.remove(s, p, o, uri)
      case other => throw new IllegalArgumentException(
        """
            |Triples removal broke because unlike in Banana-RDF Sesame-based RDF stores
            |support only Resource as subject and URI as property"
          """.stripMargin
      )
    } )

}
