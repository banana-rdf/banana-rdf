/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */
package org.w3.rdf.n3

import nomo.{Failure, Success, Result, Parsers}
import org.w3.rdf.n3.ListenerAgent
import org.w3.rdf.RDFModule


/**
 * Turtle Parser as specified at http://www.w3.org/TR/turtle/
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

  /**
   * Note without lazy val, the order of the parsers would be important
   */

  /** Parses the single token given that matches the function */
  def single(isC: Char => Boolean ): P.Parser[Char] = P.any mapResult (s =>
    s.status.flatMap(i => if (isC(i) ) Success(i) else Failure(P.err.single(i, s.position))))

  lazy val err = (pos: X) =>P.err.single('!',pos)
  lazy val COLON = P.single(':')
  lazy val PREFIX = P.word("@prefix")
  lazy val dot = P.single('.')
  lazy val eoln = P.word ("\r\n") | P.word("\n")  | P.word("\r")

  lazy val SP = (P.takeWhile1(c=> " \t\r\n".contains(c),err) | comment ).many1
  lazy val comment = P.single('#')>>P.takeWhile(c=> c != '\r' && c != '\n') << eoln

  lazy val hexadecimalChars = "1234567890ABCDEFabcdef"
  lazy val hex = P.anyOf(hexadecimalChars)

  lazy val u_CHAR = (P.word("\\u")>> hex++hex++hex++hex) map {
    case c1++c2++c3++c4 => Integer.parseInt(new String(Array(c1,c2,c3,c4)),16).toChar
  }
  lazy val U_CHAR = (P.word("\\U")>> hex++hex++hex++hex++hex++hex++hex++hex) map {
    case c1++c2++c3++c4++c5++c6++c7++c8 => Integer.parseInt(new String(Array(c1,c2,c3,c4,c5,c6,c7,c8)),16).toChar
  }

  lazy val UCHAR = u_CHAR | U_CHAR
  lazy val UCHARS = UCHAR.many1.map(_.mkString)
  lazy val PN_CHARS_BASE = single(pn_chars_simple) | UCHAR
  lazy val PN_CHARS_U = PN_CHARS_BASE | P.single ('_')
  lazy val PN_CHARS =  P.takeWhile1(pn_chars_dot, err).map(_.toSeq.mkString) | UCHAR.many1.map(_.toSeq.mkString)
  lazy val PN_decode = PN_CHARS.many1.map(_.mkString)

  lazy val PN_PREFIX = P.takeWhile(_ != ':').mapResult {
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

  lazy val PNAME_NS =  PN_PREFIX << COLON
  lazy val IRI_REF =  P.single('<')>>(P.takeWhile1(iri_char, err) | UCHARS).many.map(_.mkString)<<P.single('>')
  lazy val PREFIX_Part1 = PREFIX >> SP >> PNAME_NS
  lazy val prefixID =  PREFIX_Part1 ++ (SP>>IRI_REF)
  lazy val directive = prefixID //| base

  lazy val statement = ( directive << dot ) //| ( turtleTriples << dot )
  lazy val turtleDoc = statement.many

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
