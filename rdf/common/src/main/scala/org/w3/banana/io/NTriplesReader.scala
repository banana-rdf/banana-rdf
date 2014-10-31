package org.w3.banana.io

import java.io.{InputStream, InputStreamReader, LineNumberReader, Reader}
import java.lang.Character._

import org.w3.banana.{RDF, RDFOps}

import scala.annotation.tailrec
import scala.util.control.NoStackTrace
import scala.util.{Failure, Success, Try}

/**
 * An NTriples Reader based on the NTriples parser
 */
class NTriplesReader[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends RDFReader[Rdf, Try, NTriples] {

  /**
   * parse from the Input Stream and have the parser guess the encoding. If no encoding guessing
   * is needed use the reader method that takes a Reader.  This guessing may be more or less successful.
   * @param is InputStream
   * @param base Url to use to resolve relative URLs  ( as String ) //todo: why not as a RDF#URI ?
   * @return A Success[Graph] or a Failure
   *         //todo: it may be more appropriate to have an encoding guessing function
   */
  override def read(is: InputStream, base: String) = {
    //guess completely randomly that the inputstream is in UTF-8
    read(new InputStreamReader(is),base)
  }


  /**
   * Parse from the Reader. Readers have already made the encoding decision, so there is no decision left
   * here to make
   * @param reader
   * @param base URI for all relative URIs in reader //todo: should be a URI
   * @return Success of a Graph or Failure
   */
  override def read(reader: Reader, base: String): Try[Rdf#Graph] = {
    val lnr = new LineNumberReader(reader)
    new NTriplesParser[Rdf](lnr).parse()
  }
}

object NTriplesParser {
  private def digit(c: Char) = '0'<= c && c <= '9'
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

}

/**
 * parser for NTriples as specified at http://www.w3.org/TR/n-triples/
 * This parser tried to be fast and does not at all aim to be in a functional style, though from
 * the outside it can be used that way. ( why be functional when one uses java.io.* legacy Readers
 * and InputStreams that are blocking ? )
 *
 * The parser creates a minimum of objects:
 *  - it at most will go back a couple of characters on itself
 *  - it perhaps unnecessarily creates a Try for each Triple
 *  - it relied on throws
 *
 * @param reader
 * @param skipBrokenLines if a line does not parse, skip it and move to the next
 * @param ops
 * @tparam Rdf
 */
class NTriplesParser[Rdf <: RDF](reader: LineNumberReader, skipBrokenLines: Boolean = false)(implicit ops: RDFOps[Rdf]) {

  import ops._
  import org.w3.banana.io.NTriplesParser._

import scala.collection.mutable
  import scala.collection.mutable.StringBuilder._

  private val rewind = mutable.Stack[Int]()

  private def read(): Int = {
    if (rewind.isEmpty) reader.read()
    else rewind.pop()
  }

  private def appendChar(c: Int)(implicit buf: StringBuilder) = buf.append(c.toChar)

  private def tryRead[T](action: Char => T): T =
    read() match {
      case x if x < 0 => throw EOF("premature end of stream")
      case c => action(c.toChar)
    }

  /**
   *
   * @return the parsed Graph if successful, otherwise a failure containing the error message
   */
  def parse(): Try[Rdf#Graph] =
    Try {
      val it = if (skipBrokenLines) parseIterator().filter(_.isSuccess)
      else parseIterator().takeWhile {
        case Failure(ParseException(_,-1,_)) => false
        case Failure(other) => throw other // we break on first failure
        case Success(_) =>  true
      }
      makeGraph(it.map(_.get).toIterable)
    }


  /**
   * Much more memory efficient way to parse a large file
   * @return an iterable of Triples ( this probably will require skipping bad triples
   *         ( where would one put the errors ? )
   */
  def parseIterator(): Iterator[Try[Rdf#Triple]] =
    new Iterator[Try[Rdf#Triple]]() {
      var ended = false

      override def hasNext = !ended

      override def next() = {
        var result = parseNextTriple()
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
    }



  @tailrec
  private def parseComment(): Unit = {
    read() match {
      case -1 => Unit
      case eol if eol == '\r' || eol == '\n' => Unit
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
   * @param iribuf
   * @return
   */
  @tailrec
  private[io] final def parseIRI(implicit iribuf: mutable.StringBuilder = new mutable.StringBuilder()): Rdf#URI = {
     read() match {
      case -1 => throw EOF("unexpected end of stream reading URI starting with '" + iribuf.toString() + "'")
      case '>' => URI(iribuf.toString())
      case '\\' => parseIRI(appendChar(parseIRIQuotedChar()))
      case c if IRI_char(c) => parseIRI(appendChar(c))
      case err => throw Error(err,s"illegal character '$err' in IRI starting with >${iribuf.toString()}< ")
    }
  }

  @tailrec
  private def readN(i: Int)(implicit buf: StringBuilder = newBuilder): String = {
    if (i <= 0) buf.toString
    else read() match {
      case -1 => throw EOF("reached end of stream while trying to readN chars")
      case c => readN(i - 1)(appendChar(c))
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


  private[io] def parsePlainLiteral(implicit uribuf: mutable.StringBuilder = newBuilder): String =
    read() match {
      case -1   => throw EOF("end of string Literal before end of quotation")
      case '"'  => uribuf.toString() //closing quote
      case '\\' => parsePlainLiteral(appendChar(parseQuotedChar()))
      case illegal if ( illegal == 0x22 || illegal == 0x5c || illegal == 0xA || illegal == 0xD) => {
        throw Error(illegal, "illegal character")
      }
      case c    => {
        parsePlainLiteral(appendChar(c))
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
    implicit val buf = newBuilder
    @tailrec
    def lang(): String = {
      read() match {
        case -1 => throw EOF(s"unexpected end of stream while trying to parse language tag. Reached '$buf'")
        case '-' => { appendChar('-'); subsequentParts() }
        case c if alpha(c.toChar) => { appendChar(c); lang()}
        case other => { rewind.push(other); buf.toString() }
      }
    }
    @tailrec
    def subsequentParts(): String = {
      read() match {
        case -1 => throw EOF(s"unexpected end of stream while trying to parse language tag. Reached '$buf'")
        case '-' => { appendChar('-'); subsequentParts() }
        case c if alphaNum(c.toChar) =>  { appendChar(c); subsequentParts() }
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
    def parseBnodeLabel(implicit uribuf: mutable.StringBuilder): Rdf#BNode =
      read() match {
        case -1 => {
          val label = uribuf.toString()
          if (label.endsWith(".")) {
            rewind.push('.')
            BNode(label.substring(0, label.length - 1))
          } else
            throw EOF("was parsing bnode")
        }
        case other => {
          if (!pn_chars_dot(other)) {
            rewind.push(other)
            val label = uribuf.toString()
            if (label.endsWith(".")) {
              rewind.push('.')
              BNode(label.substring(0, label.length - 1))
            } else BNode(label)
          }
          else parseBnodeLabel(appendChar(other))
        }
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
      case w if isWhitespace(w) => parseNextTriple()
      case '#' => {
        parseComment()
        parseNextTriple()
      }
      case c => parseTriple(c)
    }
  }

  private def Error(char: Int,msg: String) = ParseException(reader.getLineNumber(), char, msg)
  private def EOF(message: String)= ParseException(reader.getLineNumber,-1,message)


}

case class ParseException(line: Int, character: Int, message: String) extends Throwable with NoStackTrace {
  override def toString = character match {
    case -1 => s"""EOF(line=$line, message="$message")"""
    case c => s"""ParseError(line=$line, char='$c', message="$message" )"""
  }
}

