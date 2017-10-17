package org.w3.banana.sesame

import java.net.URL

import org.openrdf.model.Value
import org.openrdf.query.parser.{ParsedBooleanQuery, ParsedGraphQuery, ParsedTupleQuery}
import org.openrdf.repository.sparql.SPARQLRepository
import org.w3.banana.SparqlEngine

import scala.util.Try

/**
 * Created by Daniel Maatari Okouya on 9/5/15.
 * TODO: //repo.shutDown()
 */
case class SesameHttpSparqlEngine() extends SparqlEngine[Sesame, Try, URL] {

  val store                                = new SesameStore()
  var repos: Map[String, SPARQLRepository] = Map[String, SPARQLRepository]()


  private def initRepo(sparqlEndpoint: String) = {
    val repo = new SPARQLRepository(sparqlEndpoint);
    repo.initialize();
    repo
  }

  /** Executes a Select query. */
  override def executeSelect(a: URL, query: ParsedTupleQuery, bindings: Map[String, Value]): Try[Sesame#Solutions] = {

    repos.get(a.toString) match {

      case None => {
        val repo = initRepo(a.toString)
        repos += a.toString -> repo
        val con  = repo.getConnection
        val res  = store.executeSelect(con, query, bindings)
        con.close()
        res
      }

      case Some(repo) => {
        val con = repo.getConnection
        val res = store.executeSelect(con, query, bindings)
        con.close()
        res
      }

    }
  }

  /** Executes a Construct query. */
  override def executeConstruct(a: URL, query: ParsedGraphQuery, bindings: Map[String, Value]): Try[Sesame#Graph] = ???

  /** Executes a Ask query. */
  override def executeAsk(a: URL, query: ParsedBooleanQuery, bindings: Map[String, Value]): Try[Boolean] = {

    repos.get(a.toString) match {

      case None => {
        val repo = initRepo(a.toString)
        repos += a.toString -> repo
        val con  = repo.getConnection
        val res  = store.executeAsk(con, query, bindings)
        con.close()
        res
      }

      case Some(repo) => {
        val con = repo.getConnection
        val res = store.executeAsk(con, query, bindings)
        con.close()
        res
      }

    }


  }
}
