package org.w3.banana

import scala.util.Try

case class LDPPatch[Rdf <: RDF](
  delete: Option[Delete[Rdf]],
  insert: Option[Insert[Rdf]],
  where: Option[Where[Rdf]])
case class Delete[Rdf <: RDF](block: TriplesBlock[Rdf])
case class Insert[Rdf <: RDF](block: TriplesBlock[Rdf])
case class Where[Rdf <: RDF](block: TriplesBlock[Rdf])
case class TriplesBlock[Rdf <: RDF](triples: Vector[TriplePattern[Rdf]])
case class TriplePattern[Rdf <: RDF](s: VarOrTerm[Rdf], p: Rdf#URI, o: VarOrTerm[Rdf])
sealed trait VarOrTerm[Rdf <: RDF]
case class Var[Rdf <: RDF](label: String) extends VarOrTerm[Rdf]
case class Term[Rdf <: RDF](node: Rdf#Node) extends VarOrTerm[Rdf]

trait LDPPatchParser[Rdf <: RDF] {
  def parse(s: String): Try[List[LDPPatch[Rdf]]]
}

import scala.collection.JavaConverters._
import org.w3.banana.jena._
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.sparql.lang.ParserSPARQL11
import com.hp.hpl.jena.sparql.syntax._
import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.update.UpdateFactory
import com.hp.hpl.jena.update.{ Update => JenaUpdate }
import com.hp.hpl.jena.sparql.modify.request._
import org.apache.jena.atlas.lib.Sink
import com.hp.hpl.jena.sparql.core.Quad
import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode, TripleMatch => JenaTripleMatch, _ }

import scala.util.parsing.combinator._

object PCPatchParser {

  val uri = """[a-zA-Z0-9:/#_\.\-\+]+""".r
  val integer = """[0-9]+""".r
  val name = """[a-zA-Z][a-zA-Z0-9_-]*|[a-zA-Z_][a-zA-Z0-9_]+""".r

}

class PCPatchParser[Rdf <: RDF](
  var base: Option[String] = None,
  var prefixes: Map[String, String] = Map.empty)(
  implicit ops: RDFOps[Rdf])
  extends LDPPatchParser[Rdf] with RegexParsers with JavaTokenParsers with PackratParsers {

  import PCPatchParser._
  import ops._

  def patch: Parser[LDPPatch[Rdf]] =
    prologue ~ opt(delete) ~ opt(insert) ~ opt(where) ^^ {
      case _ ~ deleteOpt ~ insertOpt ~ whereOpt => LDPPatch(deleteOpt, insertOpt, whereOpt)
    }

  def prologue: Parser[Unit] =
    rep(baseDecl | prefixDecl) ^^ { _ => () }

  def baseDecl: Parser[Unit] =
    "BASE" ~ uri ^^ { case "BASE" ~ uri => base = Some(uri) }

  def prefixDecl: Parser[Unit] =
    "PREFIX" ~ name ~ uri ^^ { case "PREFIX" ~ qname ~ uri => prefixes += (qname -> uri) }

  def delete: Parser[Delete[Rdf]] =
    ( "DELETE" | "delete" ) ~ "{" ~ triplesBlock ~ "}" ^^ {
      case _ ~ "{" ~ block ~ "}" => Delete[Rdf](block)
    }

  def insert: Parser[Insert[Rdf]] =
    ( "INSERT" | "insert" ) ~ "{" ~ triplesBlock ~ "}" ^^ {
      case _ ~ "{" ~ block ~ "}" => Insert[Rdf](block)
    }

  def where: Parser[Where[Rdf]] =
    ( "WHERE" | "where" ) ~ "{" ~ triplesBlock ~ "}" ^^ {
      case _ ~ "{" ~ block ~ "}" => Where[Rdf](block)
    }

  def triplesBlock: Parser[TriplesBlock[Rdf]] =
    rep1sep(triplePattern, ".") ~ opt(".") ^^ { case pats ~ x => TriplesBlock[Rdf](pats.toVector) }

  def triplePattern: Parser[TriplePattern[Rdf]] =
    subject ~ predicate ~ objectt ^^ { case s ~ p ~ o => TriplePattern[Rdf](s, p, o) }

  def subject: Parser[VarOrTerm[Rdf]] = (
      qnameORuri ^^ { case x => Term(x) }
    | bnode ^^ { x => Term(x) }
    | varr
  )

  def predicate: Parser[Rdf#URI] = (
      qnameORuri
    | "a" ^^ { _ => rdf.typ }
  )

  def objectt: Parser[VarOrTerm[Rdf]] = (
      qnameORuri ^^ { case x => Term(x) }
    | bnode ^^ { x => Term(x) }
    | varr
    | literal ^^ { x => Term(x) }
  )

  def qnameORuri: Parser[Rdf#URI] = (
      "<" ~ uri ~ ">" ^^ {
        case "<" ~ uri ~ ">" => base match {
          case Some(b) => URI(uri).resolveAgainst(URI(b))
          case None    => URI(uri)
        }
      }
    | name ~ ":" ~ name ^^ {
      case prefix ~ ":" ~ localName => prefixes.get(prefix) match {
        case Some(uri) => URI(uri + localName)
        case None      => sys.error(s"unknown prefix ${prefix}")
      }
    }
  )

  def bnode: Parser[Rdf#BNode] = (
      "_:" ~ name ^^ { case "_:" ~ name => BNode(name) }
    | "[]" ^^ { _ => BNode() }
  )

  def literal: Parser[Rdf#Literal] = (
      stringLiteral ~ opt("^^" ~ qnameORuri) ^^ {
        case lit ~ Some("^^" ~ dt) => TypedLiteral(lit.substring(1, lit.size - 1), dt)
        case lit ~ None            => TypedLiteral(lit.substring(1, lit.size - 1), xsd.string)
      }
    | integer ^^ { i => TypedLiteral(i, xsd.integer) }
  )

  def varr: Parser[Var[Rdf]] = "?" ~ ident ^^ { case "?" ~ x => Var(x) }





  def parse(s: String): Try[List[LDPPatch[Rdf]]] = Try {

    ???
  }

  def main(args: Array[String]): Unit = {
    val update = """
DELETE {
<s> <p> <o> .
}
INSERT {
<s> <p> <o> .
}
WHERE {
<s> <p> <o> .
}
"""

    val parser = new PCPatchParser[Jena]
    parser.parse(update)

  }

}

