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
class NTriplesParser[M <: RDFModule,F,E,X,U <: ListenerAgent[Any]](val m: M, val P: Parsers[F, Char, E, X, U]) {
  import m._

  //todo: do we really need a tree error for such a simple language (what do TreeErrors enable?)
  implicit def toTreeError(msg: String): Errors.TreeError = Errors.Single(msg, None)

  val alpha_digit_dash = "abcdefghijklmnopqrstuvwxyz0123456789-"
  val hexadecimalChars = "1234567890ABCDEFabcdef"
  def hex = P.anyOf(hexadecimalChars)

  val lang = P.takeWhile1(c => alpha_digit_dash.contains(c.toLower),
    pos => P.err.single('!',pos)).map(l => Lang(l.toSeq.mkString))

  val space1 = P.takeWhile1( c => c == ' '|| c == '\t', pos => P.err.single('!',pos))
  val space = P.takeWhile( c => c == ' '|| c == '\t' )

  val anySpace =  P.takeWhile(_.isWhitespace )
  val eoln = P.word("\n") | P.word ("\r\n")| P.word("\r")

  def isUriChar(c: Char) = ( ! c.isWhitespace) && c != '<' && c != '>'  &&
    c> 0x1F &&  (c < 0x7F || c > 0x9F )  //control characters


  import P.++
  
  val bnode = P.word("_:")>>P.takeWhile1(_.isLetterOrDigit,pos => P.err.single('!',pos)).map (n=>BNode(n.toSeq.mkString))


  val u_CHAR = (P.word("\\u")>> hex++hex++hex++hex) map {
    case c1++c2++c3++c4 => Integer.parseInt(new String(Array(c1,c2,c3,c4)),16).toChar
  }
  val U_CHAR = (P.word("\\U")>> hex++hex++hex++hex++hex++hex++hex++hex) map {
    case c1++c2++c3++c4++c5++c6++c7++c8 => Integer.parseInt(new String(Array(c1,c2,c3,c4,c5,c6,c7,c8)),16).toChar
  }
  val lt_tab = P.word("\\t").map(c=>0x9.toChar)
  val lt_cr = P.word("\\r").map(c=>0xD.toChar)
  val lt_nl = P.word("\\n").map(c=>0xA.toChar)
  val lt_slash = P.word("\\\\").map(c=>'\\')
  val lt_quote = P.word("\\\"").map(c=>'"'.toChar)

  val literal = ( u_CHAR | U_CHAR | lt_tab | lt_cr | lt_nl | lt_slash | lt_quote |
      P.takeWhile1(c=> c!= '\\' && c != '"', pos => P.err.single('!',pos)).map(n=>n.toSeq.mkString)
    ).many.map(l=> l.toSeq.mkString)

  val uriStr = (u_CHAR | U_CHAR | lt_slash | lt_quote |
      P.takeWhile1(c => isUriChar(c),pos => P.err.single('!',pos)).map(n=>n.toSeq.mkString)
     ).many1.map(i=>i.toSeq.mkString)

  val xsd = "http://www.w3.org/2001/XMLSchema#"
  val rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  val xsdString = IRI(xsd + "string")

  val plainLit = (P.single('"')>>literal<< P.single('\"'))

  val fullLiteral = plainLit ++ (typeFunc | langFunc).optional map {
    case lexicalForm ++ None => TypedLiteral(lexicalForm)
    case lexicalForm ++ Some(Left(uriRef)) => TypedLiteral(lexicalForm, uriRef)
    case lexicalForm ++ Some(Right(lang)) => LangLiteral(lexicalForm, lang)
  }

  val typeFunc = (P.word("^^") >> uriRef) map Left.apply
  val langFunc = (P.word("@") >> lang) map Right.apply


  val dot = P.single('.')

  val uriRef = ( P.single('<') >> uriStr  << P.single('>')).map(i=>IRI(i))
  val pred = uriRef
  val subject = uriRef | bnode
  val obj = uriRef | bnode | fullLiteral
  val nTriple = (subject++(space1>>pred)++(space1>>obj)).map{case s++r++o=> Triple(s,r,o)} << (space>>dot>>space)
  val comment = P.single('#') >> P.takeWhile(c =>c != '\r' && c != '\n' )
  val line = space >> (comment.as(None) | nTriple.map(Some(_)) | P.unit(None) )

  /** function that parse NTriples and send results to user in a streaming fashion */
  val nTriples = (line.mapResult{
    r =>
      r.get match {
        case Some(t) => r.user.send(t);
        case None => ()
      }
      Success(r)
  } ).delimitIgnore(eoln)
  
  /** function that parses NTriples and return result to caller as a list */
  val nTriplesList = line.delimit(eoln).map(_.flatten)


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

  def toLiteral(str: String) = {
    val b = new StringBuilder
    for (c <- str) yield {
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
    }
    b.toString()
  }
  
}



/**
 * Turtle Parser as specified at http://www.w3.org/TR/turtle/
 * That version is getting a bit too difficult to implement - due to rules like
 *  PN_PREFIX	   ::=   	PN_CHARS_BASE ( ( PN_CHARS | "." )* PN_CHARS )?
 *
 * trying the older
 *    http://www.w3.org/TeamSubmission/turtle/#sec-grammar-grammar
 *
 *
 * @param m
 * @param P
 * @tparam M
 * @tparam F
 * @tparam E
 * @tparam X
 * @tparam U
 */
class TurtleParser[M <: RDFModule,F,E,X,U <: ListenerAgent[Any]](val m: M, val P: Parsers[F, Char, E, X, U]) {
  import TurtleParser._
  import P.++

  /** Parses the single token given that matches the function */
  def single(isC: Char => Boolean ): P.Parser[Char] = P.any mapResult (s =>
    s.status.flatMap(i => if (isC(i) ) Success(i) else Failure(P.err.single(i, s.position))))

  val err = (pos: X) =>P.err.single('!',pos)
  val COLON = P.single(':')
  val PREFIX = P.word("@prefix")
  val dot = P.single('.')
  val eoln = P.word ("\r\n") | P.word("\n")  | P.word("\r")

  val comment = P.single('#')>>P.takeWhile(c=> c != '\r' && c != '\n') << eoln
  val SP = (P.takeWhile1(c=> " \t\r\n".contains(c),err) | comment ).many1

  val hexadecimalChars = "1234567890ABCDEFabcdef"
  def hex = P.anyOf(hexadecimalChars)

  val u_CHAR = (P.word("\\u")>> hex++hex++hex++hex) map {
    case c1++c2++c3++c4 => Integer.parseInt(new String(Array(c1,c2,c3,c4)),16).toChar
  }
  val U_CHAR = (P.word("\\U")>> hex++hex++hex++hex++hex++hex++hex++hex) map {
    case c1++c2++c3++c4++c5++c6++c7++c8 => Integer.parseInt(new String(Array(c1,c2,c3,c4,c5,c6,c7,c8)),16).toChar
  }

  val UCHAR = u_CHAR | U_CHAR
  val UCHARS = UCHAR.many1.map(_.mkString)
  val PN_CHARS_BASE = single(pn_chars_simple) | UCHAR
  val PN_CHARS_U = PN_CHARS_BASE | P.single ('_')
  val PN_CHARS =  P.takeWhile1(pn_chars_dot, err).map(_.toSeq.mkString) | UCHAR.many1.map(_.toSeq.mkString)
  val PN_decode = PN_CHARS.many1.map(_.mkString)

  val PN_PREFIX = P.takeWhile(_ != ':').mapResult {
    case Result(Success(pfx), pos, us)=> {
      val prefx = pfx.toSeq.mkString
      if (prefx.size == 0) Success("")
      else if (prefx.last == '.') Failure(P.err.single('.',pos))
      else if (!pn_chars_simple(prefx.head) && UCHAR(prefx)(us).isFailure) {
        Failure(P.err.single(prefx.head,pos))
      } else {
        PN_decode(prefx)(us).status.mapL(e=>P.err.single(':',pos))
      }
    }
    case other => other.status
  }

  val PNAME_NS =  PN_PREFIX << COLON
  val IRI_REF =  P.single('<')>>(P.takeWhile1(iri_char, pos =>P.err.single('!',pos)) | UCHARS).many.map(_.mkString)<<P.single('>')
  val PREFIX_Part1 = PREFIX >> SP >> PNAME_NS
  val prefixID =  PREFIX_Part1 ++ SP>>IRI_REF
  val directive = prefixID //| base

  val statement = ( directive << dot ) //| ( turtleTriples << dot )
  val turtleDoc = statement.many

}


object TurtleParser {
  val pn_simple_set = List[Pair[Int, Int]](('A'.toInt,'Z'.toInt),('a'.toInt,'z'.toInt),
    (0x00C0,0x00D6), (0x00D8,0x00F6), (0x00F8,0x02FF), (0x0370,0x037D),
    (0x037F,0x1FFF), (0x200C,0x200D), (0x2070,0x218F), (0x2C00,0x2FEF),
    (0x3001,0xD7FF), (0xF900,0xFDCF), (0xFDF0,0xFFFD), (0x10000,0xEFFFF)
  )
  val non_iri_chars = Array('<','>','"','{','}','|','^','`','\\')

  val pn_chars_set = ('0'.toInt,'9'.toInt)::pn_simple_set:::List((0x300,0x36F),(0x203F,0x2040))

  def pn_chars_simple(c: Char): Boolean = pn_simple_set.exists(in(_)(c))

  def pn_chars_dot(c: Char) = c == '.' || pn_chars(c)
  def pn_chars(c: Char) = c == '-' || c == '_' || c == 0xB7 || pn_chars_set.exists(in(_)(c))

  def iri_char(c: Char) = !( non_iri_chars.contains(c) || in((0,' '.toInt))(c) )

  def in(interval: Pair[Int, Int])(c: Char) =  c>=interval._1 && c<=interval._2


}
