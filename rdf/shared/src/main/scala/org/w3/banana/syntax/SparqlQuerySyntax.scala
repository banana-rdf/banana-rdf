package org.w3.banana.syntax

import org.w3.banana._

import scala.util.Try

trait SparqlQuerySyntax[Rdf <: RDF] { self: SparqlQuerySyntax[Rdf] =>

  implicit def sparqlQueryW(query: Rdf#Query) = new SparqlQueryW[Rdf](query)

  def parseSelect(query: String)(implicit sparqlOps: SparqlOps[Rdf]): Try[Rdf#SelectQuery] =
    sparqlOps.parseSelect(query, Seq.empty)

  def parseConstruct(query: String)(implicit sparqlOps: SparqlOps[Rdf]): Try[Rdf#ConstructQuery] =
    sparqlOps.parseConstruct(query, Seq.empty)

  def parseAsk(query: String)(implicit sparqlOps: SparqlOps[Rdf]): Try[Rdf#AskQuery] =
    sparqlOps.parseAsk(query, Seq.empty)

  def parseUpdate(query: String)(implicit sparqlOps: SparqlOps[Rdf]): Try[Rdf#UpdateQuery] =
    sparqlOps.parseUpdate(query, Seq.empty)

  def parseQuery(query: String)(implicit sparqlOps: SparqlOps[Rdf]): Try[Rdf#Query] =
    sparqlOps.parseQuery(query, Seq.empty)

}

class SparqlQueryW[Rdf <: RDF](val query: Rdf#Query) extends AnyVal {

  def fold[T](
    select: Rdf#SelectQuery => T,
    construct: Rdf#ConstructQuery => T,
    ask: Rdf#AskQuery => T)(
      implicit sparqlOps: SparqlOps[Rdf]): T = sparqlOps.fold(query)(select, construct, ask)

}
