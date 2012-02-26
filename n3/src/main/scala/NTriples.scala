/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.rdf.n3

import nomo._
import nomo.Errors.{TreeError, Single}
import scala.collection.mutable
import org.w3.rdf._


/**
 * Async Parser for the simplest of all RDF encodings: NTriples
 * http://www.w3.org/TR/rdf-testcases/#ntriples
 *
 * This is using the nomo library that is being developed
 * here:  https://bitbucket.org/pchiusano/nomo
 *
 * @author bblfish
 * @since 02/02/2012
 */

//todo: can't work out how to get the right dependent type for ListenerAgent. Should be ListenerAgent[m.Triple]
class NTriplesParser[Rdf <: RDF, F, E, X, U <: Listener[Rdf]](
    val ops: RDFOperations[Rdf],
    val P: Parsers[F, Char, E, X, U]) {
  
  import ops._

  //todo: do we really need a tree error for such a simple language (what do TreeErrors enable?)
  implicit def toTreeError(msg: String): Errors.TreeError = Errors.Single(msg, None)

  val alpha_digit_dash = "abcdefghijklmnopqrstuvwxyz0123456789-"
  val hexadecimalChars = "1234567890ABCDEFabcdef"
  def hex = P.anyOf(hexadecimalChars)

  /** Parses the single token given that matches the function */
  def single(isC: Char => Boolean ): P.Parser[Char] = P.any mapResult (s =>
    s.status.flatMap(i => if (isC(i) ) Success(i) else Failure(P.err.single(i, s.position))))

  val lang = P.takeWhile1(c => alpha_digit_dash.contains(c.toLower),
    pos => P.err.single('!',pos)).map(l => Lang(l.toSeq.mkString))

  val space1 = P.takeWhile1( c => c == ' '|| c == '\t', pos => P.err.single('!',pos))
  val space = P.takeWhile( c => c == ' '|| c == '\t' )

  val anySpace =  P.takeWhile(_.isWhitespace )
  val eoln = P.word("\n") | P.word ("\r\n")| P.word("\r")

  def isUriChar(c: Char) = ( ! c.isWhitespace) && c != '<' && c != '>'  &&
    c> 0x1F &&  (c < 0x7F || c > 0x9F )  //control characters


  import P.++
  
  lazy val bnode = P.word("_:")>>P.takeWhile1(_.isLetterOrDigit,pos => P.err.single('!',pos)).map (n=>BNode(n.toSeq.mkString))


  lazy val u_CHAR = (P.word("\\u")>> hex++hex++hex++hex) map {
    case c1++c2++c3++c4 => Integer.parseInt(new String(Array(c1,c2,c3,c4)),16).toChar
  }
  lazy val U_CHAR = (P.word("\\U")>> hex++hex++hex++hex++hex++hex++hex++hex) map {
    case c1++c2++c3++c4++c5++c6++c7++c8 => Integer.parseInt(new String(Array(c1,c2,c3,c4,c5,c6,c7,c8)),16).toChar
  }
  lazy val lt_tab = P.word("\\t").map(c=>0x9.toChar)
  lazy val lt_cr = P.word("\\r").map(c=>0xD.toChar)
  lazy val lt_nl = P.word("\\n").map(c=>0xA.toChar)
  lazy val lt_slash = P.word("\\\\").map(c=>'\\')
  lazy val lt_quote = P.word("\\\"").map(c=>'"'.toChar)

  lazy val literal = ( u_CHAR | U_CHAR | lt_tab | lt_cr | lt_nl | lt_slash | lt_quote |
      P.takeWhile1(c=> c!= '\\' && c != '"', pos => P.err.single('!',pos)).map(n=>n.toSeq.mkString)
    ).many.map(l=> l.toSeq.mkString)

  lazy val uriStr = (u_CHAR | U_CHAR |
      lt_slash | lt_quote |
      single(c => isUriChar(c))
    ).many1.map(i=>i.toSeq.mkString)

  // these are already provided by RDFOperations
  val xsd = "http://www.w3.org/2001/XMLSchema#"
  val rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  lazy val xsdString = IRI(xsd + "string")

  lazy val plainLit = (P.single('"')>>literal<< P.single('\"'))

  lazy val fullLiteral = plainLit ++ (typeFunc | langFunc).optional map {
    case lexicalForm ++ None => TypedLiteral(lexicalForm)
    case lexicalForm ++ Some(Left(uriRef)) => TypedLiteral(lexicalForm, uriRef)
    case lexicalForm ++ Some(Right(lang)) => LangLiteral(lexicalForm, lang)
  }

  lazy val typeFunc = (P.word("^^") >> uriRef) map Left.apply
  lazy val langFunc = (P.word("@") >> lang) map Right.apply


  lazy val dot = P.single('.')

  lazy val uriRef = ( P.single('<') >> uriStr  << P.single('>')).map(i=>IRI(i))
  lazy val pred = uriRef
  lazy val subject = uriRef | bnode
  lazy val obj = uriRef | bnode | fullLiteral
  lazy val nTriple = (subject++(space1>>pred)++(space1>>obj)).map{case s++r++o=> Triple(s,r,o)} << (space>>dot>>space)
  lazy val comment = P.single('#') >> P.takeWhile(c =>c != '\r' && c != '\n' )
  lazy val line = space >> (comment.as(None) | nTriple.map(Some(_)) | P.unit(None) )

  /** function that parse NTriples and send results to user in a streaming fashion */
  lazy val nTriples = (line.mapResult{
    r =>
      r.get match {
        case Some(t) => r.user.sendTriple(t);
        case None => ()
      }
      r.status
  } ).delimitIgnore(eoln)
  
  /** function that parses NTriples and return result to caller as a list */
  lazy val nTriplesList = line.delimit(eoln).map(_.flatten)


}

object NTriplesParser {

  val hexChar = Array( '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F');


  private def hex(c: Char) = {
    val b = new StringBuilder(6)
    b.append("\\u").
      append(hexChar((c >> 12) & 0xF)).
      append(hexChar((c >> 8) & 0xF)).
      append(hexChar((c >> 4) & 0xF)).
      append(hexChar(c & 0xF))
    b
  }
  private def hexLong(c: Char) = {
    val b = new StringBuilder(10)
    b.append("\\U").
      append(hexChar((c >> 28) & 0xF)).
      append(hexChar((c >> 24) & 0xF)).
      append(hexChar((c >> 20) & 0xF)).
      append(hexChar((c >> 16) & 0xF)).
      append(hexChar((c >> 12) & 0xF)).
      append(hexChar((c >> 8) & 0xF)).
      append(hexChar((c >> 4) & 0xF)).
      append(hexChar(c & 0xF))
    b
  }
  private def iri(c: Char, b: StringBuilder): Boolean = {
    if ("<>\\{}\"|^'\t\f\r\n".contains(c)) b.append(hex(c))
    else return false
    true
  }

  private def literal(c: Char, b: StringBuilder): Boolean = {
    if (c <= 0x8) b.append(hex(c))
    else if (c == 0x9) b.append("\\t")
    else if (c == 0xA) b.append("\\n")
    else if (c == 0xB || c == 0xC) b.append(hex(c))
    else if (c == 0xD) b.append("\\r")
    else if (c >= 0xE && c <= 0x1F) b.append(hex(c))
    else if (c == 0x20 || c == 0x21) b.append(c)
    else if (c == 0x22) b.append('\\').append('"')
    else if (c >= 0x23 && c <= 0x5b) b.append(c)
    else if (c == 0x5c) b.append('\\').append('\\')
    else if (c >= 0x5d && c <= 0x7e) b.append(c)
    else if (c >= 0x7f && c <= 0xffff) b.append(hex(c))
    else if (c >= 0x10000 & c <= 0x10FFFF) b.append(hexLong(c))
    else {
      return false
    }
    true
  }

  /**
   * encode a string so that it can appear in a ASCII only Literal
   **/
  def toAsciiLiteral(str: String) = {
    val b = new StringBuilder
    for (c <- str)  {
      literal(c,b)
    }
    b.toString()
  }

  /**
   * encode a string so that it can appear in a URI that is fully ASCII.
   * Not 100% sure about these rules yet.
   * For utf one should be able to devise more sophisticated rules for iri
   * construction.
   */
  def toIRI(str: String) = {
    val b = new StringBuilder
    for (c <- str) {
      iri(c,b) || literal(c,b)
    }
    b.toString
  }
  
}


