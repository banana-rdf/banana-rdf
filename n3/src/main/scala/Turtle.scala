/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */
package org.w3.rdf.n3

import nomo.{Failure, Success, Result, Parsers}
import org.w3.rdf._
import java.io.Serializable


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
class TurtleParser[RDF <: RDFDataType, F, E, X, U <: Listener[RDF]](
    val ops: RDFOperations[RDF],
    val P: Parsers[F, Char, E, X, U]) {
  
  import TurtleParser._
  import P.++
  import ops._

  val rdfType = IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type ")

  /**
   * Note without lazy val, the order of the parsers would be important
   */

  /** Parses the single token given that matches the function */
  def single(isC: Char => Boolean ): P.Parser[Char] = P.any mapResult (s =>
    s.status.flatMap(i => if (isC(i) ) Success(i) else Failure(P.err.single(i, s.position))))

  lazy val err = (pos: X) =>P.err.single('!',pos)
  lazy val COLON = P.single(':')
  lazy val PREFIX = P.word("@prefix")
  lazy val BASE = P.word("@base")
  lazy val dot = P.single('.')
  lazy val eoln = P.word ("\r\n") | P.word("\n")  | P.word("\r")
  lazy val CLOSE_ANGLE = P.word("\\>").map(a=>">")   // unclear in the spec if this is allowed (not allowed by grammar)

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
  lazy val PN_LOCAL_ESC = P.single('\\')>>single((c: Char) => pn_local_esc.contains(c))
  lazy val PLX=  (P.single('%')++hex++hex).map{case '%'++h1++h2=> "%"+h1+h2} | PN_LOCAL_ESC.map(_.toString)

  lazy val UCHAR = u_CHAR | U_CHAR
  lazy val IRICHAR = UCHAR | CLOSE_ANGLE
  lazy val IRICHARS = IRICHAR.many1.map(_.mkString)
  lazy val UCHARS = UCHAR.many1.map(_.mkString)
  lazy val PN_CHARS_BASE = single(pn_chars_base) | UCHAR
  lazy val PN_CHARS_U = PN_CHARS_BASE | P.single ('_')
  lazy val PN_CHARS =  P.takeWhile1(pn_chars_dot, err).map(_.toSeq.mkString) | UCHAR.many1.map(_.toSeq.mkString)
  lazy val PN_decode = PN_CHARS.many1.map(_.mkString)

  /*
   *for a discussion of this rule see: https://bitbucket.org/pchiusano/nomo/issue/6/complex-ebnf-rules
   */
  lazy val PN_PREFIX  : P.Parser[String] = P.takeWhile(_ != ':').mapResult {
    case Result(Success(pfx), pos, us)=> {
      val prefx = pfx.toSeq.mkString
      if (prefx.size == 0) Success("")
      else if (prefx.last == '.') Failure(P.err.single('.',pos))
      else if (!pn_chars_base(prefx.head) && UCHAR(prefx)(us).isFailure) {
        Failure(P.err.single(prefx.head,pos))
      } else {
        val result = (PN_decode<<P.eof)(prefx)(us)
        result.status.mapL(e=>P.err.single(':',pos))
      }
    }
    case other => other.status.map(x=>"this map() never gets called, as 'other' is an error.")
  }

  lazy val PNAME_NS =  (PN_PREFIX << COLON).map(prefix=>prefix+":")
  lazy val IRI_REF =  P.single('<')>>(P.takeWhile1(iri_char, err) | IRICHARS).many.map(i=>IRI(i.mkString))<<P.single('>')
  lazy val PREFIX_Part1 = PREFIX >> SP >> PNAME_NS
  lazy val prefixID =  (PREFIX_Part1 ++ (SP>>IRI_REF)).mapResult{ r=>
    r.status.map(pair=>r.user.addPrefix(pair._1,pair._2))
    r.status
  }

  lazy val base = (BASE >> SP >> IRI_REF).mapResult{ r =>
    //the base is just the namespace without a :
    r.status.map(iri=>r.user.addPrefix("",iri))
    r.status
  }
  lazy val PNL_FIRST = PN_CHARS_U | single(c=> c>='0' && c<='9') | PLX
  lazy val PNL_BODY = PN_CHARS | PLX  //removed . in name, which is in current spec. too difficult to handle right now.

  /**
   *for a discussion of this rule see: https://bitbucket.org/pchiusano/nomo/issue/6/complex-ebnf-rules
   */
  lazy val PN_LOCAL = (PNL_FIRST ++ PNL_BODY.many.map(_.mkString)).map { case first++body => first+body }

  lazy val PNAME_LN = (PNAME_NS ++ PN_LOCAL).map{ case ns++local => PName(ns,local)}
  lazy val PrefixedName = PNAME_LN | PNAME_NS.map(ns => PName(ns,""))
  lazy val IRIref = IRI_REF | PrefixedName

  lazy val obj = IRIref.mapResult{
    r =>
      // TODO if it was of type RDF#Node, you could use "fold"
//      r.get match {
//        case iri: m.IRI => r.user.setObject(iri)
//      }
      r.status
  } //| blank | literal
  lazy val predicate = IRIref
  lazy val verb = predicate | P.single('a').as(rdfType)

  lazy val objectList = (obj >> ( SP.optional >> P.single(',')>> SP.optional >> obj ).manyIgnore )

  lazy val predicateObjectList = verb.mapResult {
    r =>
      // there is really something weird here with the types
//      r.get match {
//        case iri: IRI => r.user.setVerb(iri)
//      }
      r.status
  }<<SP.optional ++ objectList //( ";" verb objectList )* (";")?

  lazy val subject = IRIref.mapResult{
    r =>
      // TODO I don't undersntad what you want to achieve
//      r.get match {
//        case iri: IRI => r.user.setSubject(iri)
//        case pname: PName => r.user.setSubject(pname)
//      }
      r.status
  } //| blank

  lazy val triples =  subject<<SP.optional ++ predicateObjectList

  lazy val directive = prefixID | base

  lazy val statement = ( directive >> dot ) //| ( triples >> dot )
  lazy val turtleDoc = statement.many

}


object TurtleParser {
  private val pn_char_intervals_base = List[Pair[Int, Int]](('A'.toInt,'Z'.toInt),('a'.toInt,'z'.toInt),
    (0x00C0,0x00D6), (0x00D8,0x00F6), (0x00F8,0x02FF), (0x0370,0x037D),
    (0x037F,0x1FFF), (0x200C,0x200D), (0x2070,0x218F), (0x2C00,0x2FEF),
    (0x3001,0xD7FF), (0xF900,0xFDCF), (0xFDF0,0xFFFD), (0x10000,0xEFFFF)
  )
  private val non_iri_chars = Array('<','>','"','{','}','|','^','`','\\')
  
  val pn_local_esc = Array('_' , '~' , '.' , '-' , '!' , '$' , '&' , '\'' , '(' , ')' , '*' , '+' , ',' , ';' , '=' , ':' , '/' , '?' , '#' , '@' , '%' )

  private val pn_chars_set = ('0'.toInt,'9'.toInt)::pn_char_intervals_base:::List((0x300,0x36F),(0x203F,0x2040))



  def pn_chars_base(c: Char): Boolean = pn_char_intervals_base.exists(in(_)(c))

  def pn_chars_dot(c: Char) = c == '.' || pn_chars(c)
  def pn_chars(c: Char) = c == '-' || c == '_' || c == 0xB7 || pn_chars_set.exists(in(_)(c))

  def iri_char(c: Char) = !( non_iri_chars.contains(c) || in((0,' '.toInt))(c) )

  def in(interval: Pair[Int, Int])(c: Char) =  c>=interval._1 && c<=interval._2


}
