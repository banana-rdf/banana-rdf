/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */
package org.w3.rdf.n3

import org.w3.rdf._
import nomo._


/**
 * Turtle Parser as specified at http://www.w3.org/TR/turtle/
 * Using Nomo library https://bitbucket.org/pchiusano/nomo
 *
 * This is a non blocking parser for Turtle. It does not block on input,
 * and serialises its output. Ie: it can send triples to another process as
 * it parses them.
 *
 * @param  P
 * @tparam F
 * @tparam E
 * @tparam X
 * @tparam U The Listener, which keeps prefixes and sends its results on
 */
class TurtleParser[Rdf <: RDF, F, E, X, U <: Listener[Rdf]](
    val ops: RDFOperations[Rdf],
    val P: Parsers[F, Char, E, X, U]) {
  
  import TurtleParser._
  import P.++
  import ops._

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

  /**
   * This does not deal with final dot properly. It will consume it and fail, when it might need to consume up
   * to and not including the dot.

   * For a discussion of this rule see: https://bitbucket.org/pchiusano/nomo/issue/6/complex-ebnf-rules
   */
  lazy val PN_PREFIX  : P.Parser[String] =  (PN_CHARS_BASE ++ PN_CHARS.many).mapResult{ r =>
      r.status.flatMap {
        case c1 ++ more => if (more.size != 0 && more.last == '.') Error(err(r.position)) else Success(c1+more.mkString)
      }
  }

  lazy val PNAME_NS =  (PN_PREFIX.optional << COLON).map(prefix=>prefix.getOrElse("")+":")
  lazy val IRI_REF =  P.single('<')>>(P.takeWhile1(iri_char, err) | IRICHARS).many.map(i=>IRI(i.mkString))<<P.single('>')
  lazy val PREFIX_Part1 = PREFIX >> SP >> PNAME_NS
  lazy val prefixID =  (PREFIX_Part1 ++ (SP.optional>>IRI_REF)).mapResult{ r=>
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
  lazy val IRIref = IRI_REF | PrefixedName.mapResult[IRI]{ r =>
    r.status.flatMap{ pn =>
        //todo: work out how to set more friendly errors https://bitbucket.org/pchiusano/nomo/issue/7/errors
        r.user.resolve(pn).map(i=>Success(i)).getOrElse(Failure(P.err.empty))
      }
  }
  val q1 = P.single('\'')
  val q2 = P.single('"')
  val Q1 = P.word("\'\'\'")
  val Q2 = P.word("\"\"\"")

  val ECHAR = P.single('\\')>> P.anyOf("tbnrf\\\"'").map {
    case 't' => '\t'
    case 'b' => '\b'
    case 'n' => '\n'
    case 'r' => '\r'
    case 'f' => '\f'
    case x => x
  }

  lazy val STRING_LITERAL1 = (
    q1 >>  ( P.takeWhile1(c => !"\\'\n\r".contains(c),err).map(_.toSeq.mkString) | ECHAR | UCHAR ).many << q1
    ).map(_.mkString)

  lazy val STRING_LITERAL2 = (
    q2 >>  ( P.takeWhile1(c => !"\\\"\n\r".contains(c),err).map(_.toSeq.mkString) | ECHAR | UCHAR ).many << q2
    ).map(_.mkString)

  lazy val STRING_LITERAL_LONG1 = (
    Q1 >> ((
      (q1.as("'") | P.word("''").as("''")).optional ++ (
        P.takeWhile1(c => c != '\'' && c != '\\', err).map(_.toSeq.mkString) | ECHAR | UCHAR)
      ).map {
      case Some(quote) ++ c => quote + c
      case None ++ c => c.toString
    }).many << Q1
    ).map(_.mkString)


  lazy val STRING_LITERAL_LONG2 = (
    Q2 >> ((
      (q2.as("\"") | P.word("\"\"").as("\"\"")).optional ++ (
        P.takeWhile1(c => c != '\"' && c != '\\', err).map(_.toSeq.mkString) | ECHAR | UCHAR)
      ).map {
      case Some(quote) ++ c => quote + c
      case None ++ c => c.toString
    }).many << Q2
    ).map(_.mkString)


  val LANGTAG = P.single('@') >> {
    P.takeWhile1(alphabet(_),err) ++ (P.single('-') >> P.takeWhile1(alphaNumeric(_),err).map(_.toSeq.mkString) ).many
  }.map{
    case c ++ list => Lang(c+(if (list.size==0) "" else "-")+list.mkString("-"))
  }

  lazy val StringLit =  STRING_LITERAL_LONG1 | STRING_LITERAL_LONG2   | STRING_LITERAL1 | STRING_LITERAL2


  lazy val RDFLiteral = {
    StringLit ++ (
      LANGTAG.map(tag => (lit: String) => LangLiteral(lit,tag)) |
      ( P.word("^^")>>IRIref ).map (tp => (lit: String) => TypedLiteral(lit,tp)) )
      .optional
  }.map {
    case str ++ Some(func) => func(str)
    case str ++ None => TypedLiteral(str)
  }

  lazy val INTEGER = P.takeWhile1(numeric(_),err).map(_.toSeq.mkString)

  lazy val Exponent = ( P.anyOf("eE") ++ P.anyOf("+-").optional ++ INTEGER ) map {
    case e++pos++exp => ""+e+pos.getOrElse("")+exp
  }

  val SIGN = P.anyOf("+-").optional

  //having done all this parsing, it would be nice to just return a scala.Double
  //perhaps we can have a wrapped Literal, which contains it's value...
  lazy val NumericLiteral =
    (SIGN++INTEGER.optional++(dot>>INTEGER).optional++Exponent.optional).mapResult {  r =>
      def w(o: Option[String])=o.getOrElse("")
      def c(o: Option[Char])=o.getOrElse("")
      r.status.flatMap {
        case _   ++None++None++ _       => Failure(err(r.position))
        case sign++i   ++None++None     => Success(TypedLiteral(c(sign)+w(i),                      xsdInteger))
        case sign++i   ++dec ++None     => Success(TypedLiteral(c(sign)+w(i)+w(dec.map("."+_)),    xsdDecimal))
        case sign++i   ++dec ++Some(exp)=> Success(TypedLiteral(c(sign)+w(i)+w(dec.map("."+_))+exp,xsdDouble ))
        //todo: improve error message https://bitbucket.org/pchiusano/nomo/issue/7/errors
      }
    }

  val BooleanLiteral = P.word("true").as(xsdTrueLit) | P.word("false").as(xsdFalseLit)

  lazy val literal = RDFLiteral  | NumericLiteral | BooleanLiteral

  lazy val ANON = (P.single('[')>> SP.optional >> P.single(']')).as(BNode())
  lazy val BLANK_NODE_LABEL = P.word("_:")>>PN_LOCAL.map(BNode(_))
  lazy val BlankNode = BLANK_NODE_LABEL | ANON

  lazy val blankNodePropertyList = P.single('[').mapResult  {  r =>
     r.status.map(node=>{ r.user.pushSubject(BNode());  r })
  } >> SP.optional >> (predicateObjectList << SP.optional << P.single(';').optional << SP.optional >> P.single(']')).mapResult{ r =>
    r.status.map(node=>{ r.user.pop; r })
  }

  lazy val collection = P.single('(').mapResult  {  r =>
    r.status.map(node=>{ r.user.pushList; r })
  } >> SP.optional >> (obj.delimit1Ignore(SP).optional << SP.optional << P.single(')')).mapResult{ r =>
    r.status.map(node=>{ r.user.pop; r })
  }

  lazy val objBlank =  blankNodePropertyList | collection
  lazy val obj: P.Parser[Any] = (IRIref | literal | BlankNode ).mapResult{r =>
    r.status.map{ node => { r.user.setObject(node); r } }
  } | objBlank
  lazy val predicate = IRIref
  lazy val verb =  ( predicate | P.single('a').as(rdfType) ).mapResult{ r =>
    r.status.map{ iri => { r.user.setVerb(iri); r } }
  }

  lazy val objectList = obj.delimit1Ignore( SP.optional >> P.single(',')>> SP.optional )

  lazy val predicateObjectList: P.Parser[Unit] = ( verb<<SP.optional ++ objectList).delimit1Ignore( SP.optional >> P.single (';') >> SP.optional)

  lazy val subject = ( IRIref | BlankNode ).mapResult { r =>
    r.status.map{ node => { r.user.pushSubject(node); r } }
  } | objBlank

  lazy val triples =  subject ++ (SP.optional>>predicateObjectList)

  lazy val directive = prefixID | base

  lazy val statement = ( directive << SP.optional << dot ) | ( triples << SP.optional << dot ).mapResult { r=>
    r.status.map(n=>{r.user.pop; r})
  }
  lazy val turtleDoc = (SP.optional>>statement<<SP.optional).manyIgnore

}


object TurtleParser {
  private val romanAlphabet = List[Pair[Int, Int]](('A'.toInt,'Z'.toInt),('a'.toInt,'z'.toInt))
  private val romanAlphaNumeric = ('0'.toInt,'9'.toInt)::romanAlphabet

  private val pn_char_intervals_base = romanAlphabet:::List[Pair[Int, Int]](
    (0x00C0,0x00D6), (0x00D8,0x00F6), (0x00F8,0x02FF), (0x0370,0x037D),
    (0x037F,0x1FFF), (0x200C,0x200D), (0x2070,0x218F), (0x2C00,0x2FEF),
    (0x3001,0xD7FF), (0xF900,0xFDCF), (0xFDF0,0xFFFD), (0x10000,0xEFFFF)
  )
  private val non_iri_chars = Array('<','>','"','{','}','|','^','`','\\')
  
  val pn_local_esc = Array('_' , '~' , '.' , '-' , '!' , '$' , '&' , '\'' ,
    '(' , ')' , '*' , '+' , ',' , ';' , '=' , ':' , '/' , '?' , '#' , '@' , '%' )

  private val pn_chars_set = ('0'.toInt,'9'.toInt)::pn_char_intervals_base:::List((0x300,0x36F),(0x203F,0x2040))


  def alphabet(c: Char): Boolean =  romanAlphabet.exists(in(_)(c))
  def numeric(c: Char): Boolean = '0'<=c && c<='9'
  def alphaNumeric(c: Char): Boolean = romanAlphaNumeric.exists(in(_)(c))
  def pn_chars_base(c: Char): Boolean = pn_char_intervals_base.exists(in(_)(c))

  def pn_chars_dot(c: Char) = c == '.' || pn_chars(c)
  def pn_chars(c: Char) = c == '-' || c == '_' || c == 0xB7 || pn_chars_set.exists(in(_)(c))

  def iri_char(c: Char) = !( non_iri_chars.contains(c) || in((0,' '.toInt))(c) )

  def in(interval: Pair[Int, Int])(c: Char) =  c>=interval._1 && c<=interval._2


}
