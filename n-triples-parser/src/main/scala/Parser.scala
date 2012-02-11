/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.rdf

import nomo._
import nomo.Errors.{TreeError, Single}

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

class NTriplesParser[M <: Module,F,E,X,U](val m: M, val P: Parsers[F, Char, E, X, U]) {
  
  import m._

  //setup, should be in type
  implicit def toTreeError(msg: String): Errors.TreeError = Errors.Single(msg, None)
  implicit val U: Unit = ()
  //end setup

  val alpha_digit_dash = "abcdefghijklmnopqrstuvwxyz0123456789-"
  val hexChar = "1234567890ABCDEFabcdef".seq
  def hex = P.anyOf(hexChar)

  val lang = P.takeWhile1(c => alpha_digit_dash.contains(c.toLower),
    pos => P.err.single('!',pos)).map(l => Lang(l.get.toString))

  val space = P.takeWhile( c => c == ' '|| c == '\t' )
  val anySpace =  P.takeWhile(_.isWhitespace )

  def isUriChar(c: Char) = ( ! c.isWhitespace) && c != '<' && c != '>'


  val uriRef = ( P.single('<') >> P.takeWhile(isUriChar(_) ) << P.single('>')).map(i=>IRI(i.toString))
  import P.++
  
  val bnode = P.word("_:")>>P.takeWhile(_.isLetterOrDigit).map (n=>BNode(n.toString))


  val lit_u = (P.word("\\u")>> hex++hex++hex++hex) map {
    case c1++c2++c3++c4 => Integer.parseInt(new String(Array(c1,c2,c3,c4)),16).toChar
  }
  val lit_U = (P.word("\\U")>> hex++hex++hex++hex++hex++hex++hex++hex) map {
    case c1++c2++c3++c4++c5++c6++c7++c8 => Integer.parseInt(new String(Array(c1,c2,c3,c4,c5,c6,c7,c8)),16).toChar
  }
  val lt_tab = P.word("\\t").map(c=>0x9.toChar)
  val lt_cr = P.word("\\r").map(c=>0xD.toChar)
  val lt_nl = P.word("\\n").map(c=>0xA.toChar)
  val lt_slash = P.word("\\\\").map(c=>"\\")
  val lt_quote = P.word("\\\"").map(c=>'"'.toChar)

  val literal = ( lit_u | lit_U | lt_tab | lt_cr | lt_nl | lt_slash | lt_quote |
      P.takeWhile1(c=> c!= '\\' && c != '"', pos => P.err.single('!',pos))
    ).many

  val xsd = "http://www.w3.org/2001/XMLSchema#"
  val rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  val xsdString = IRI(xsd + "string")

  val plainLit = (P.single('"')>>literal<< P.word("\"")).map(l=> l.mkString)

  val fullLiteral = plainLit ++ (typeFunc | langFunc).optional map {
    case lexicalForm ++ option => Literal(lexicalForm, option.getOrElse(xsdStringIRI))
  }

  val typeFunc = (P.word("^^") >> uriRef)
  val langFunc = (P.word("@") >> lang )


  val node = uriRef | bnode | fullLiteral
  val pred = uriRef
  val dot = P.single('.')

  val sentence = (node++(space>>pred)++(space>>node)).map{case s++r++o=>Triple(s,r,o)} << (space++dot)
  val ntriples = anySpace >> (sentence delimit anySpace )
  

}

object NTriplesParser {

  val hexChar = Array( '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F');


  def hex(c: Char) = {
    val b = new StringBuilder(6)
    b.append("\\u").
      append(hexChar((c >> 12) & 0xF)).
      append(hexChar((c >> 8) & 0xF)).
      append(hexChar((c >> 4) & 0xF)).
      append(hexChar(c & 0xF))
    b
  }
  def hexLong(c: Char) = {
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
