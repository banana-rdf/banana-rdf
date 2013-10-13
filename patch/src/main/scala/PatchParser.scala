package org.w3.banana

import scala.util.Try
import scala.util.parsing.combinator._
import java.io._

trait PatchParser[Rdf <: RDF] {
  def parseOne(s: String): Try[Patch[Rdf]]
  def parse(s: String): Try[List[Patch[Rdf]]]
}

object PatchParser {

  def parseOne[Rdf <: RDF](s: String)(implicit ops: RDFOps[Rdf]): Try[Patch[Rdf]] = Try {
    val parser = new PatchParserCombinator[Rdf]
    parser.parse(parser.patch, new StringReader(s)).get
  }

  def parse[Rdf <: RDF](s: String)(implicit ops: RDFOps[Rdf]): Try[List[Patch[Rdf]]] = Try {
    val parser = new PatchParserCombinator[Rdf]
    parser.parse(parser.patches, new StringReader(s)).get
  }

}
/** a Parser Combinator-based parser for the Patch format */
object PatchParserCombinator {

  val uri = """[a-zA-Z0-9:/#_\.\-\+]+""".r
  val integer = """[0-9]+""".r
  val name = """[a-zA-Z][a-zA-Z0-9_-]*|[a-zA-Z_][a-zA-Z0-9_]+""".r

}

class PatchParserCombinator[Rdf <: RDF](
  var base: Option[String] = None,
  var prefixes: Map[String, String] = Map.empty)(
  implicit ops: RDFOps[Rdf])
  extends PatchParser[Rdf] with RegexParsers with JavaTokenParsers with PackratParsers {

  import PatchParserCombinator._
  import ops._

  def patches: Parser[List[Patch[Rdf]]] =
    rep(patch)

  def patch: Parser[Patch[Rdf]] =
    opt(prologue) ~ opt(delete) ~ opt(insert) ~ opt(where) ^^ {
      case _ ~ deleteOpt ~ insertOpt ~ whereOpt => Patch(deleteOpt, insertOpt, whereOpt)
    }

  def prologue: Parser[Unit] =
    rep(baseDecl | prefixDecl) ^^ { _ => () }

  def baseDecl: Parser[Unit] =
    "BASE" ~ uri ^^ { case "BASE" ~ uri => base = Some(uri) }

  def prefixDecl: Parser[Unit] =
    "PREFIX" ~ name ~ ":" ~ "<" ~ uri ~ ">" ^^ { case "PREFIX" ~ qname ~ ":" ~ "<" ~ uri ~ ">" => prefixes += (qname -> uri) }

  def delete: Parser[Delete[Rdf]] =
    ( "DELETE" | "delete" ) ~ "{" ~ triplesPattern ~ "}" ^^ {
      case _ ~ "{" ~ pattern ~ "}" => Delete[Rdf](pattern)
    }

  def insert: Parser[Insert[Rdf]] =
    ( "INSERT" | "insert" ) ~ "{" ~ triplesPattern ~ "}" ^^ {
      case _ ~ "{" ~ pattern ~ "}" => Insert[Rdf](pattern)
    }

  def where: Parser[Where[Rdf]] =
    ( "WHERE" | "where" ) ~ "{" ~ triplesBlock ~ "}" ^^ {
      case _ ~ "{" ~ block ~ "}" => Where[Rdf](block)
    }

  def triplesPattern: Parser[TriplesPattern[Rdf]] =
    rep1sep(triplePattern, ".") ~ opt(".") ^^ { case pats ~ x => TriplesPattern[Rdf](pats.toVector) }

  def triplesBlock: Parser[TriplesBlock[Rdf]] =
    rep1sep(triplePath, ".") ~ opt(".") ^^ { case paths ~ x => TriplesBlock[Rdf](paths.toVector) }

  def triplePattern: Parser[TriplePattern[Rdf]] =
    subject ~ predicate ~ objectt ^^ { case s ~ p ~ o => TriplePattern[Rdf](s, p, o) }

  def triplePath: Parser[TriplePath[Rdf]] =
    subject ~ verb ~ objectt ^^ { case s ~ verb ~ o => TriplePath[Rdf](s, verb, o) }

  def verb: Parser[Verb[Rdf]] = (
      qnameORuri ^^ { uri => IRIRef(uri) }
    | rep1sep(qnameORuri, "/") ^^ { elements => Path(elements) }
    | "a" ^^ { _ => IRIRef(rdf.typ) }
    | varr
  )


  def subject: Parser[VarOrTerm[Rdf]] = (
      qnameORuri ^^ { case x => Term(x) }
    | bnode ^^ { x => Term(x) }
    | varr
  )

  def predicate: Parser[VarOrIRIRef[Rdf]] = (
      qnameORuri ^^ { uri => IRIRef(uri) }
    | "a" ^^ { _ => IRIRef(rdf.typ) }
    | varr
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

  def parseOne(s: String): Try[Patch[Rdf]] = Try {
    val parser = new PatchParserCombinator[Rdf]
    parser.parse(parser.patch, new StringReader(s)).get
  }

  def parse(s: String): Try[List[Patch[Rdf]]] = Try {
    val parser = new PatchParserCombinator[Rdf]
    parser.parse(parser.patches, new StringReader(s)).get
  }

}

