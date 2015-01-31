package org.w3.banana.io

import java.io.{InputStream, InputStreamReader, Reader}
import java.lang.Character._

import org.w3.banana.{RDF, RDFOps}

import scala.annotation.tailrec
import scala.util.control.NoStackTrace
import scala.util.{Failure, Success, Try}

/**
 * An NTriples Reader based on the NTriples parser
 */
class NTriplesReader[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends RDFReader[Rdf, Try, NTriples] {
   import NTriplesParser._

  /**
   * parse from the Input Stream and have the parser guess the encoding. If no encoding guessing
   * is needed use the reader method that takes a Reader.  This guessing may be more or less successful.
   *
   * Warning: the InputStream will be parsed into a character stream using the Operating System or
   * Java default encoding. For data found on the web, that is published by others, it is unusual that
   * they all would make the same default encoding choice as you.
   *
   * @param base Url to use to resolve relative URLs  ( as String ) //todo: why not as a RDF#URI ?
   * @return A Success[Graph] or a Failure
   *         //todo: it may be more appropriate to have an encoding guessing function
   */
  def read(is: InputStream, base: String) =
    read(new InputStreamReader(is),base)



  /**
   * Parse from the Reader. Readers have already made the encoding decision, so there is no decision left
   * here to make
   * @param base URI for all relative URIs in reader //todo: should be a URI
   * @return Success of a Graph or Failure
   */
  def read(reader: Reader, base: String): Try[Rdf#Graph] =
    toGraph(new NTriplesParser[Rdf](reader))

}

object  NTriplesParser {
  private def digit(c: Char) = '0'<= c && c <= '9'
  private def whitespace(c: Char) = c == ' ' || c == '\t'
  private def alpha(c: Char) = ('A' <= c && c <= 'Z') ||  ('a' <= c && c <= 'z')
  private def hex(c: Char) = digit(c) || ('A' <= c && c <= 'F') ||  ( 'a' <= c && c <= 'f')
  private def alphaNum(c: Char) = alpha(c) ||  digit(c)
  private def pn_chars_base(c: Char) = alpha(c) ||
    ('\u00C0' <= c && c <= '\u00D6') || ('\u00D8' <= c && c <='\u00F6') || ('\u00F8' <= c && c <= '\u02FF')  ||
    ('\u0370' <= c && c <= '\u037D') || ('\u037F' <= c && c <= '\u1FFF') || ('\u200C' <= c && c <= '\u200D') ||
    ('\u2070' <= c && c <= '\u218F') || ('\u2C00' <= c && c <= '\u2FEF') || ('\u3001' <= c && c <= '\uD7FF') ||
    ('\uF900' <= c && c <= '\uFDCF') || ('\uFDF0' <= c && c <= '\uFFFD') || ('\u1000' <= c && c <= '\uEFFF')

  // bblfish note: in my test ('\u3000' to '\u036F') is empty on OSX with java 1.7.0_67-b01 - why?
  private def pn_chars_ranges(c: Char) = digit(c) || ('\u3000' <= c && c <= '\u036F') || ('\u203F' <= c && c <= '\u2040')
  private def not_IRI_char_range(c: Char) = ('\u0000' <= c && c <= '\u0020')

  def IRI_char(ci: Int) = {
    val c = ci.toChar
    """<>"{}|^`\""".indexOf(c) == -1 && !not_IRI_char_range(c)
  }

  def pn_chars(ci: Int) = {
    val c = ci.toChar
    c == '-' || c == '\u00B7' || pn_chars_base(c) || pn_chars_ranges(c)
  }

  def pn_chars_dot(ci: Int) = {
    val c = ci.toChar
    c == '.' || pn_chars(c)
  }

  def pn_chars_u(ci: Int) = {
    val c = ci.toChar
    c == '_' || c == ':' || pn_chars_base(c)
  }

  def blank_node_label_first_char(ci: Int): Boolean = {
    val c = ci.toChar
    digit(c) || pn_chars_u(c)
  }

  def whitespace(ci: Int) = {
    val c = ci.toChar
    c == ' ' || c == '\t'
  }

  def whitespaceEOL(ci: Int) = {
    val c = ci.toChar
    c == ' ' || c == '\t' || c == '\n' || c == '\r'
  }

  def hexVal(chars: Seq[Char]): Char = {
    var position: Int = chars.size
    var result: Int = 0
    while (position > 0) {
      val importance = chars.size - position
      position = position-1
      result = result | (Character.digit(chars(position), 16) << 4 * importance)
    }
    result.toChar
  }

  /**
   *
   * @return the parsed Graph if successful, otherwise a failure containing the error message
   */
  def toGraph[Rdf<:RDF](ntparser: NTriplesParser[Rdf]): Try[Rdf#Graph] = Try {
    val filteredIterator = if (ntparser.skipBrokenLines) ntparser.filter(_.isSuccess)
    else ntparser.takeWhile {
      case Failure(ParseException(_, -1, _)) => false
      case Failure(other) => throw other // we break on first failure
      case Success(_) => true
    }
    ntparser.ops.makeGraph(filteredIterator.map(_.get).toIterable)
  }


}

/**
 * Parser for NTriples as specified at http://www.w3.org/TR/n-triples/
 * A Parser is constructed from a [[java.io.Reader]], and is an Iterator 
 * of Try of [[org.w3.banana.RDF#Triples]]
 * 
 * This parser tries to be fast:
 *
 *  - it creates a minimum of objects, sticking as far as possible with chars and ints
 *  - it at most will go back a couple of characters on itself ( this could be narrowed down )
 *  - it relied on throws to avoid wrapping the overwhelming number of well parsed nodes (or even chars ) 
 *    in Success objects
 *
 *  todo
 *   - broken lines should be returned complete so that the user can edit them and to help debugging
 *
 * @param reader  Reader for input
 * @param skipBrokenLines  broken lines will be skipped, rather than halting the parsing, if true
 * @param ops the Operations corresponding to the Rdf type
 * @tparam Rdf a subtype of RDF
 */
class NTriplesParser[Rdf <: RDF](reader: Reader,
            val skipBrokenLines: Boolean = false)
                                (implicit
                                 val ops: RDFOps[Rdf])
  extends Iterator[Try[Rdf#Triple]] {

  import ops._
  import org.w3.banana.io.NTriplesParser._
  var lineNumber = 0

  import scala.collection.mutable
  import scala.collection.mutable.StringBuilder._

  var ended = false

  def hasNext = !ended

  def next() = {
    val result = parseNextTriple()
    ended = result match {
      case Failure(ParseException(_,c,_)) => {
        if (c == -1) true   //EOF
        else {
          if (skipBrokenLines) {
            if (c != '\n' && c != '\r') parseComment()
            false
          } else {
            true
          }
        }
      }
      case Failure(_) => !skipBrokenLines
      case _ => false
    }
    result
  }


  private val rewind = mutable.Stack[Int]()

  private def read(): Int = {
    if (rewind.isEmpty) reader.read()
    else rewind.pop()
  }

  private def appendChar(c: Int,buf: StringBuilder) = buf.append(c.toChar)

  private def tryRead[T](action: Char => T): T =
    read() match {
      case x if x < 0 => throw EOF("premature end of stream")
      case c => action(c.toChar)
    }
  



  @tailrec
  private def parseComment(): Unit = {
    read() match {
      case -1 => Unit
      case '\r' => Unit
      case '\n' => Unit
      case _ => parseComment()
    }
  }

  @tailrec
  private def nextCharAfterOptionalWhiteSpace(): Int = {
    read() match {
      case ' ' => nextCharAfterOptionalWhiteSpace()
      case '\t' => nextCharAfterOptionalWhiteSpace()
      case c => c
    }
  }

  /**
   * The initial '<' has already been read
   */
  @tailrec
  private[io] final def parseIRI(iribuf: mutable.StringBuilder = newBuilder): Rdf#URI = {
     read() match {
      case -1 => throw EOF("unexpected end of stream reading URI starting with '" + iribuf.toString() + "'")
      case '>' => URI(iribuf.toString())
      case '\\' => parseIRI(appendChar(parseIRIQuotedChar(),iribuf))
      case c if IRI_char(c) => parseIRI(appendChar(c,iribuf))
      case err => throw Error(err,s"illegal character '$err' in IRI starting with >${iribuf.toString()}< ")
    }
  }

  @tailrec
  private def readN(i: Int, buf: StringBuilder = newBuilder): String = {
    if (i <= 0) buf.toString
    else read() match {
      case -1 => throw EOF("reached end of stream while trying to readN chars")
      case c => readN(i - 1, appendChar(c,buf))
    }
  }

  private def parseShortHex(): Char = hexVal(readN(4).toCharArray)

  private def parseLongHex(): Char = hexVal(readN(8).toCharArray)

  private def parseIRIQuotedChar(): Char =
    read() match {
      case 'u' => parseShortHex()
      case 'U' => parseLongHex()
      case other => throw Error(other,"illegal character after escape '\\' char .")
    }

  private def parseQuotedChar(): Char =
    read() match {
      case 't' => '\t'
      case 'b' => '\b'
      case 'n' => '\n'
      case 'r' => '\r'
      case 'f' => '\f'
      case '"' => '"'
      case ''' => '''
      case '\\' => '\\'
      case 'u' => parseShortHex()
      case 'U' => parseLongHex()
      case other => throw Error(other,"illegal quoted char")
    }


  private[io] def parsePlainLiteral(uribuf: mutable.StringBuilder = newBuilder): String =
    read() match {
      case -1   => throw EOF("end of string Literal before end of quotation")
      case '"'  => uribuf.toString() //closing quote
      case '\\' => parsePlainLiteral(appendChar(parseQuotedChar(),uribuf))
      case illegal if ( illegal == 0x22 || illegal == 0x5c || illegal == 0xA || illegal == 0xD) => {
        throw Error(illegal, "illegal character")
      }
      case c    => {
        parsePlainLiteral(appendChar(c,uribuf))
      }
    }

  private[io] def parseDataType(): Rdf#URI = {
    read() match {
      case '^' => {
        val c = read()
        if ( c == '<')
           parseIRI()
        else throw Error(c,"data type literal must be followed by ^^<$uri> ")
      }
      case -1 => throw EOF("unexpected end of stream while waiting for dataType for URI")
      case c => throw Error(c,"expected ^^ followed by URI, found ^" + c)
    }
  }
  private def parseLang(): Rdf#Lang = {
    val buf = newBuilder
    @tailrec
    def lang(): String = {
      read() match {
        case -1 => throw EOF(s"unexpected end of stream while trying to parse language tag. Reached '$buf'")
        case '-' => { appendChar('-',buf); subsequentParts() }
        case c if alpha(c.toChar) => { appendChar(c,buf); lang()}
        case other => { rewind.push(other); buf.toString() }
      }
    }
    @tailrec
    def subsequentParts(): String = {
      read() match {
        case -1 => throw EOF(s"unexpected end of stream while trying to parse language tag. Reached '$buf'")
        case '-' => { appendChar('-',buf); subsequentParts() }
        case c if alphaNum(c.toChar) =>  { appendChar(c,buf); subsequentParts() }
        case other => { rewind.push(other); buf.toString() }
      }
    }
    Lang(lang())
  }


  // we enter this function after having consumed the first quotation character (i.e. ")
  private[io] def parseLiteral(): Rdf#Literal = {

    val lexicalForm = parsePlainLiteral()

    read() match {
      case -1 => throw EOF("was parsing literal")
      case '^' => Literal(lexicalForm, parseDataType())
      case '@' => Literal.tagged(lexicalForm, parseLang())
      case x => {
        rewind.push(x) // this character can be used for later parsing
        Literal(lexicalForm)
      }
    }

  }


  /**
   * the initial '_' character has already been read
   * @return The last characters parsed, and The BNode
   *
   */
  private[io] def parseBNode(): Rdf#BNode = {
    @tailrec
    def parseBnodeLabel(uribuf: mutable.StringBuilder): Rdf#BNode =
      read() match {
        case -1 =>
          val label = uribuf.toString()
          if (label.endsWith(".")) {
            rewind.push('.')
            BNode(label.substring(0, label.length - 1))
          } else
            throw EOF("was parsing bnode")

        case other =>
          if (!pn_chars_dot(other)) {
            rewind.push(other)
            val label = uribuf.toString()
            if (label.endsWith(".")) {
              rewind.push('.')
              BNode(label.substring(0, label.length - 1))
            } else BNode(label)
          }
          else parseBnodeLabel(appendChar(other,uribuf))

      }

    val nc = read()
    if (nc != ':') throw Error(nc,"bnode must start with _:")
    else tryRead { c =>
      if (blank_node_label_first_char(c)) {
        parseBnodeLabel(newBuilder.append(c))
      } else {
        throw Error(c,s"blank node starts with illegal character in first position 'x0${Integer.toHexString(c)}'")
      }
    }
  }

  private def parseSubject(c: Char): Rdf#Node = {
    c match {
      case '<' => parseIRI()
      case '_' => parseBNode()
      case x => throw Error(c,"Subject of Triple must start with a URI or bnode .")
    }

  }

  private def parseObject(c: Int) = {
    c match {
      case -1 =>  throw EOF("was about to parse object")
      case '<' => parseIRI()
      case '"' => parseLiteral()
      case '_' => parseBNode()
      case other => throw Error(other,"illegal character to start triple entity ( subject, relation, or object)")
    }
  }

  private def endOfSentence(): Unit = read match {
    case -1 => throw EOF("was still searching for '.'")
    case '.' => ()
    case c if whitespace(c) => endOfSentence()
    case other => throw Error(other,s"found character '$other' before end of sentence '.'")
  }

  private[io] def parseTriple(firstChar: Int): Try[Rdf#Triple] =  Try {
    val subject = parseObject(firstChar)
    nextCharAfterOptionalWhiteSpace() match {
      case c if c != '<' =>
        throw Error(c,s"Subject must be followed by predicate URI. Found >$c< . ")
      case _ => {
        val relation = parseIRI()
        val obj = parseObject(nextCharAfterOptionalWhiteSpace())
        endOfSentence()
        Triple(subject, relation, obj)
      }
    }
  }

  @tailrec
  private def parseNextTriple(): Try[Rdf#Triple] = {
    read() match {
      case -1 => Failure(EOF("while starting to parse next triple"))
      case w if whitespaceEOL(w) => parseNextTriple()
      case '#' => {
        parseComment()
        parseNextTriple()
      }
      case c => {lineNumber += 1; parseTriple(c); }
    }
  }

  private def Error(char: Int,msg: String) = ParseException(lineNumber, char, msg)
  private def EOF(message: String)= ParseException(lineNumber,-1,message)

}



case class ParseException(line: Int, character: Int, message: String) extends Throwable with NoStackTrace {
  override def toString = character match {
    case -1 => s"""EOF(line=$line, message="$message")"""
    case c => s"""ParseError(line=$line, char='$c', message="$message" )"""
  }
}

