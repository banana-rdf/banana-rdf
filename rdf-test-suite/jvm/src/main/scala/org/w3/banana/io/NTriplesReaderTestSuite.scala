package org.w3.banana.io

import java.io._

import org.scalatest.{Matchers, WordSpec}
import org.w3.banana.{FOAFPrefix, RDF, RDFOps}

import scala.util.{Failure, Success, Try}

class NTriplesWriterTestSuite[Rdf <: RDF]()(
  implicit ops: RDFOps[Rdf], reader: RDFReader[Rdf, Try, NTriples], writer: RDFWriter[Rdf,Try,NTriples]
  )  extends WordSpec with Matchers {
  import ops._

  val foaf = FOAFPrefix[Rdf]

  val bblfish = "http://bblfish.net/people/henry/card#me"
  val name = "Henry Story"

  val typ = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"

  def ntparser(ntstring: String, skip: Boolean=false) =
    new NTriplesParser[Rdf](new StringReader(ntstring),skip)

  import NTriplesParser.toGraph
  "Ntriplets writer " should  {
    "write one triplet" in {

      val g = Graph(Triple(URI(bblfish),rdf.`type`,foaf.Person))
      val str = writer.asString(g,base = "http://example").get
      val graphTry = toGraph(ntparser(str))
      assert( graphTry.get isIsomorphicWith g)
    }

    "write more triplets" in {
      //TODO: rewrite with random triplets generators in future
      val g = Graph(
        Triple(URI(bblfish), foaf.name, Literal(name)),
        Triple(URI(bblfish), foaf.knows, BNode("betehess")),
        Triple(BNode("betehess"), foaf.homepage, URI("http://bertails.org/"))
      )
      val str = writer.asString(g,base = "http://example").get
      val graphTry = toGraph(ntparser(str))
      assert( graphTry.get isIsomorphicWith g)
    }

  }

}

/**
 *
 */
class NTriplesReaderTestSuite[Rdf <: RDF]()(
  implicit ops: RDFOps[Rdf], reader: RDFReader[Rdf, Try, NTriples]
  ) extends WordSpec with Matchers {

  import ops._
  import NTriplesParser.toGraph

  val foaf = FOAFPrefix[Rdf]

  val bblfish = "http://bblfish.net/people/henry/card#me"
  val name = "Henry Story"

  val typ = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"

  def foafstr(n: String) = "http://xmlns.com/foaf/0.1/" + n

  def rd(nt: String): Try[Rdf#Graph] = reader.read(new StringReader(nt),"")
  def st(node: Rdf#Node) =
    node.fold(
      uri => s"<${uri.getString}>",
      bn => s"_:${fromBNode(bn)}",
      {
        case Literal(lexical, Literal.xsdString, None) => s""""$lexical""""
        case Literal(lexical, tp, None) => s""""$lexical"^^<$tp>"""
        case Literal(lexical, Literal.xsdString, Some(lang)) => s""""$lexical"@$lang"""
      })


  def ntparser(ntstring: String, skip: Boolean=false) =
    new NTriplesParser[Rdf](new StringReader(ntstring),skip)

  "test that the parser can parse single components. Parser " should  {

    "parse a URL" in {
      val iri = ntparser(bblfish+">").parseIRI()
      iri should equal (URI(bblfish))
    }

    "parse a URL with an encoded character \\u" in {
      val iri = ntparser("""http://example/\u0053>""").parseIRI()
      iri should equal (URI("http://example/S"))
    }

    "parse a URL with an encoded character \\U" in {
      val iri = ntparser("""http://example/\U00000053>""").parseIRI()
      iri should equal (URI("http://example/S"))
    }


    "parse a plain Literal" in {
       val lit = ntparser(name + "\"").parsePlainLiteral()
       lit should equal(name)
    }

    "not parse a plain Literal that does not close" in {
      val nt = ntparser(name)
      val lit = Try(nt.parseLiteral())
      lit should be a 'failure
    }

    "parse a PlainLiteral"  in {
      val lit = ntparser(name + "\" ").parseLiteral()
      lit should equal(Literal(name))
    }

    "parse a LangLiteral" in {
      val lit = ntparser(name + "\"@en ").parseLiteral()
      lit should equal(Literal.tagged(name,Lang("en")))

      val lit2 = ntparser(name + "\"@en-us ").parseLiteral()
      lit2 should equal(Literal.tagged(name,Lang("en-us")))

    }

    "parse an TypedLiteral" in {
      val litstr = s"""123"^^<${xsd.integer.getString}> """
      val lit = ntparser(litstr).parseLiteral()
      lit should equal(Literal("123",xsd.integer))
    }

    "parse a Bnode" in {
      val bn = Try (ntparser(":123 ").parseBNode())
      bn should equal(Success(BNode("123")))
    }
    
    "not parse an illegal BNode" in {
      val bn = Try (ntparser(":-123 ").parseBNode())
      bn should be a 'failure
    }

  }


  "test that parser can parse one Triple. The parser" should {

    "not fail on a triple containing only URIs" in {
      val str = s"$bblfish> <$typ> <${foafstr("Person")}> ."
      val p = ntparser(str).parseTriple('<')
      p should be (Success(Triple(URI(bblfish),rdf.`type`,foaf.Person)))
    }

    "not fail on a triple containing a Literal" in {
      val str = s"""$bblfish> <${foafstr("name")}> "$name"."""
      val p = ntparser(str).parseTriple('<')
      p should be (Success(Triple(URI(bblfish),foaf.name,Literal(name))))
    }

    "not fail on a triple containing a Literal and a bnode" in {
      val str = s""":nolate <${foafstr("name")}> "$name"@en."""
      val p = ntparser(str).parseTriple('_')
      p should be (Success(Triple(BNode("nolate"),foaf.name,Literal.tagged(name,Lang("en")))))
    }

    "not fail on a triple containing two bnodes" in {
      val str = s""":jane <${foafstr("knows")}> _:tarzan ."""
      val p = ntparser(str).parseTriple('_')
      p should be (Success(Triple(BNode("jane"),foaf.knows,BNode("tarzan"))))
    }

  }




  "Test that parser can parse a document containing one triple. The parser " should {

    "not fail with one triple" in {
      val str = s"<$bblfish> <$typ> <${foafstr("Person")}> ."
      val i = ntparser(str)
      i.hasNext should be(true)
      i.next() should be(Success(Triple(URI(bblfish), rdf.`type`, foaf.Person)))
      val end = i.next()
      end.isFailure should be(true)
      end.failed.get.asInstanceOf[ParseException].character should be (-1)
      i.hasNext should be(false)
    }

    "not fail when parsing a document with one triple" in {
      val str = s"""<$bblfish>     <${foafstr("name")}>      "$name"@de      ."""
      val graphTry = toGraph(ntparser(str))
      assert(graphTry.get isIsomorphicWith Graph(Triple(URI(bblfish), foaf.name, Literal.tagged(name, Lang("de")))))
    }

    "not fail when parsing a document with one triple and whitespace" in {
      val str =
        s"""
         # a document with a comment
           <$bblfish>     <${foafstr("knows")}>      _:anton      .  # and some whitespace

           """
      val graphTry = toGraph(ntparser(str))
      assert( graphTry.get isIsomorphicWith Graph(Triple(URI(bblfish), foaf.knows, BNode("anton"))))
    }


  }


  "Test that the parser can parse a document containing more triples. The parser " should {
      "parse a document with 5 triples and a comment" in {
        val str =
          s"""
            <$bblfish> <${foafstr("name")}> "Henry Story"@en .
            <$bblfish> <${foafstr("knows")}> _:anton .
            <$bblfish> <${foafstr("knows")}> _:betehess .
        # Anton info
        _:anton <${foafstr("name")}> "Anton".
        _:betehess <${foafstr("homepage")}> <http://bertails.org/> .
          """
        val graphTry = toGraph(ntparser(str))
        assert (graphTry.get isIsomorphicWith Graph(
          Triple(URI(bblfish), foaf.name, Literal.tagged(name, Lang("en"))),
          Triple(URI(bblfish), foaf.knows, BNode("anton")),
          Triple(URI(bblfish), foaf.knows, BNode("betehess")),
          Triple(BNode("anton"), foaf.name, Literal("Anton")),
          Triple(BNode("betehess"), foaf.homepage, URI("http://bertails.org/"))
        ))

      }

    "parse a document with 5 triples ( skipping two which do not parse ) and a comment" in {
      val str =
        s"""
            <$bblfish> <${foafstr("name")}> "Henry Story" .
            <$bblfish> <${foafstr("knows")}> _|:anton .
            <$bblfish> <${foafstr("knows")}> _:betehess .
        # Anton info
        _:anton <${foafstr("name")}> "Anton"
        _:betehess <${foafstr("homepage")}> <http://bertails.org/> .
          """
      val graphTry = toGraph(ntparser(str,skip=true))
      graphTry.get.size should be (3)
      assert (graphTry.get isIsomorphicWith Graph(
        Triple(URI(bblfish), foaf.name, Literal(name)),
        Triple(URI(bblfish), foaf.knows, BNode("betehess")),
        Triple(BNode("betehess"), foaf.homepage, URI("http://bertails.org/"))
      ))

    }

  }


  def ntparse(string: String): Try[Rdf#Graph] = toGraph(ntparser(string))



  "w3c tests of type rdft:TestNTriplesPositiveSyntax ( from http://www.w3.org/2013/N-TriplesTests/ ) " should {
    def test(s: String, size: Int)(implicit test: Rdf#Graph => Boolean = _ => true) = {
      val parseAttempt = ntparse(s)
      assert(test(parseAttempt.get))
      parseAttempt should be a 'success
      parseAttempt.get.size should be(size)
    }

    "verify that empty files parse with success" in {
      test("", 0)
      test("#Empty file", 0)

      test(
        """#One comment, one empty line
          |
        """.stripMargin, 0)
    }

    "verify that triples with IRIs parse with success" in {
      test( """<http://example/s> <http://example/p> <http://example/o> .""", 1)
      test(
        """# x53 is capital S
          |<http://example/\u0053> <http://example/p> <http://example/o> .""".stripMargin, 1) { graph =>
        graph.triples.head == Triple(URI("http://example/S"), URI("http://example/p"), URI("http://example/o"))
      }

      test(
        """# x53 is capital S
          |<http://example/\U00000053> <http://example/p> <http://example/o> .""".stripMargin, 1) { graph =>
        graph.triples.head == Triple(URI("http://example/S"), URI("http://example/p"), URI("http://example/o"))
      }

      test( """# IRI with all chars in it.
              |<http://example/s> <http://example/p> <scheme:!$%25&'()*+,-./0123456789:/@ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~?#> .
              | """.stripMargin,
        1
      )
    }


    "verify that Literals parse with Success" in {

      test( """<http://example/s> <http://example/p> "string" .""", 1)
      test( """<http://example/s> <http://example/p> "string"@en .""", 1)
      test( """<http://example/s> <http://example/p> "string"@en-uk .""", 1) { graph =>
        graph isIsomorphicWith Graph(Triple(URI("http://example/s"), URI("http://example/p"),
          Literal.tagged("string", Lang("en-uk"))))
      }
    }

    "verify that Literals containing a string escape parse successfully" in {
      test( """<http://example/s> <http://example/p> "a\n" .""", 1) { graph =>
        val Triple(_, _, o) = graph.triples.head
        o == Literal("a\n")
      }
      test( """<http://example/s> <http://example/p> "a\u0020b" .""", 1) { graph =>
        val Triple(_, _, o) = graph.triples.head
        o == Literal("a b")
      }
      test( """<http://example/s> <http://example/p> "a\U00000020b" .""", 1) { graph =>
        val Triple(_, _, o) = graph.triples.head
        o == Literal("a b")
      }
    }

    "verify that bnodes parse successfully" in {
      test( """_:a  <http://example/p> <http://example/o> .""", 1)
      test( """<http://example/s> <http://example/p> _:a .
      _:a  <http://example/p> <http://example/o> .""", 2)

      test( """<http://example/s> <http://example/p> _:1a .
      _:1a  <http://example/p> <http://example/o> .""", 2) { graph => graph.contains(Triple(BNode("1a"), URI("http://example/p"), URI("http://example/o")))
      }

    }

    "verify that datatypes parse successfully" in {
         test("""<http://example/s> <http://example/p> "123"^^<http://www.w3.org/2001/XMLSchema#byte> .
            |<http://example/s> <http://example/p> "123"^^<http://www.w3.org/2001/XMLSchema#string> .""".stripMargin,2)
    }

    "verify a large chunk of NTriples" in {

      val doc =  """#
          |# Copyright World Wide Web Consortium, (Massachusetts Institute of
          |# Technology, Institut National de Recherche en Informatique et en
          |# Automatique, Keio University).
          |#
          |# All Rights Reserved.
          |#
          |# Please see the full Copyright clause at
          |# <http://www.w3.org/Consortium/Legal/copyright-software.html>
          |#
          |# Test file with a variety of legal N-Triples
          |#
          |# Dave Beckett - http://purl.org/net/dajobe/
          |#
          |# $Id: test.nt,v 1.7 2003/10/06 15:52:19 dbeckett2 Exp $
          |#
          |#####################################################################
          |
          |# comment lines
          |  	  	   # comment line after whitespace
          |# empty blank line, then one with spaces and tabs
          |
          |
          |<http://example.org/resource1> <http://example.org/property> <http://example.org/resource2> .
          |_:anon <http://example.org/property> <http://example.org/resource2> .
          |<http://example.org/resource2> <http://example.org/property> _:anon .
          |# spaces and tabs throughout:
          | 	 <http://example.org/resource3> 	 <http://example.org/property>	 <http://example.org/resource2> 	.
          |
          |# line ending with CR NL (ASCII 13, ASCII 10)
          |<http://example.org/resource4> <http://example.org/property> <http://example.org/resource2> .
          |
          |# 2 statement lines separated by single CR (ASCII 10)
          |<http://example.org/resource5> <http://example.org/property> <http://example.org/resource2> .
          |<http://example.org/resource6> <http://example.org/property> <http://example.org/resource2> .
          |
          |
          |# All literal escapes
          |<http://example.org/resource7> <http://example.org/property> "simple literal" .
          |<http://example.org/resource8> <http://example.org/property> "backslash:\\" .
          |<http://example.org/resource9> <http://example.org/property> "dquote:\"" .
          |<http://example.org/resource10> <http://example.org/property> "newline:\n" .
          |<http://example.org/resource11> <http://example.org/property> "return\r" .
          |<http://example.org/resource12> <http://example.org/property> "tab:\t" .
          |
          |# Space is optional before final .
          |<http://example.org/resource13> <http://example.org/property> <http://example.org/resource2>.
          |<http://example.org/resource14> <http://example.org/property> "x".
          |<http://example.org/resource15> <http://example.org/property> _:anon.
          |
          |# \\u and \\U escapes
          |# latin small letter e with acute symbol \u00E9 - 3 UTF-8 bytes #xC3 #A9
          |<http://example.org/resource16> <http://example.org/property> "\u00E9" .
          |# Euro symbol \u20ac  - 3 UTF-8 bytes #xE2 #x82 #xAC
          |<http://example.org/resource17> <http://example.org/property> "\u20AC" .
          |# resource18 test removed
          |# resource19 test removed
          |# resource20 test removed
          |
          |# XML Literals as Datatyped Literals
          |<http://example.org/resource21> <http://example.org/property> ""^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
          |<http://example.org/resource22> <http://example.org/property> " "^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
          |<http://example.org/resource23> <http://example.org/property> "x"^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
          |<http://example.org/resource23> <http://example.org/property> "\""^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
          |<http://example.org/resource24> <http://example.org/property> "<a></a>"^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
          |<http://example.org/resource25> <http://example.org/property> "a <b></b>"^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
          |<http://example.org/resource26> <http://example.org/property> "a <b></b> c"^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
          |<http://example.org/resource26> <http://example.org/property> "a\n<b></b>\nc"^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
          |<http://example.org/resource27> <http://example.org/property> "chat"^^<http://www.w3.org/2000/01/rdf-schema#XMLLiteral> .
          |# resource28 test removed 2003-08-03
          |# resource29 test removed 2003-08-03
          |
          |# Plain literals with languages
          |<http://example.org/resource30> <http://example.org/property> "chat"@fr .
          |<http://example.org/resource31> <http://example.org/property> "chat"@en .
          |
          |# Typed Literals
          |<http://example.org/resource32> <http://example.org/property> "abc"^^<http://example.org/datatype1> .
          |# resource33 test removed 2003-08-03
          | """.stripMargin
      test(doc, 30)
    }

    "comment following triple" in {
      test("""<http://example/s> <http://example/p> <http://example/o> . # comment
        |<http://example/s> <http://example/p> _:o . # comment
        |<http://example/s> <http://example/p> "o" . # comment
        |<http://example/s> <http://example/p> "o"^^<http://example/dt> . # comment
        |<http://example/s> <http://example/p> "o"@en . # comment""".stripMargin,5)
    }

    "literal ascii boundary" in {
      //note we are using Scala encoding of chars here
      test(
        """<http://a.example/s> <http://a.example/p> "\u0000\u0009\u000b\u000c\u000e\u0026\u0028\u005b\u005d\u007f".""",
        1){graph=>
       val Triple(_,_,o) = graph.triples.head
        foldNode(o)(_=>false,_=>false,lit=>lit.lexicalForm.length ==10)
      }
    }

    "literal with UTF-8 boundary" in {
      test("<http://a.example/s> <http://a.example/p> \"" +
       """\uc280\udfbf\ue0a0\u80e0\ubfbf\ue180\u80ec\ubfbf\ued80\u80ed\u9fbf\uee80\u80ef\ubfbd\uf090\u8080\uf0bf\ubfbd\uf180\u8080\uf3bf\ubfbd\uf480\u8080\uf48f\ubfbd" . """,1) { graph =>
        val Triple(_,_,o) = graph.triples.head
        foldNode(o)(_=>false,_=>false,lit=>{
          val lexical = lit.lexicalForm
          lexical.size == 26 &&
            lexical.contains("龿")
        } )
      }
    }

    "literal all controls" in {
      val lit="""<http://a.example/s> <http://a.example/p> "\\u0000\\u0001\\u0002\\u0003\\u0004\\u0005\\u0006\\u0007\\u0008\t""" +
        """\\u000B\\u000C\\u000E\\u000F\\u0010\\u0011\\u0012\\u0013\\u0014\\u0015\\u0016\\u0017\\u0018\\u0019\\u001A\\u001B\\u001C""" +
        """\\u001D\\u001E\\u001F" ."""
      test(lit,1)
    }

    "literal all punctuation" in {
      test("""<http://a.example/s> <http://a.example/p> " !\"#$%&():;<=>?@[]^_`{|}~" .""",1)
    }

    "literal with single quote" in {
      test("""<http://a.example/s> <http://a.example/p> "x'y" .""",1)
    }


    "literal_with_2_squotes" in {
      test( """<http://a.example/s> <http://a.example/p> "x''y" .""", 1){g=>
        g.triples.head.objectt.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="x''y")
      }
    }

    "literal_with_dquote" in {
      test( """<http://a.example/s> <http://a.example/p> "x''y" .""", 1){g=>
        g.triples.head.objectt.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="x''y")
      }
    }
    "literal_with_2_dquotes" in {
      test( """<http://a.example/s> <http://a.example/p> "x\"\"y" .""", 1){g=>
        g.triples.head.objectt.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="""x""y""")
      }
    }
    "literal_with_REVERSE_SOLIDUS2" in {
      test( """<http://example.org/ns#s> <http://example.org/ns#p1> "test-\\" .""", 1){g=>
        g.triples.head.objectt.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="""test-\""")
      }
    }
    "literal_with_CHARACTER_TABULATION" in {
      test( "<http://a.example/s> <http://a.example/p> \"\\t\" .", 1){g=>
        g.triples.head.objectt.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="\t")
      }
    }
    "literal_with_BACKSPACE" in {
      test( "<http://a.example/s> <http://a.example/p> \"\\b\" .", 1){g=>
        g.triples.head.objectt.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="\b")
      }
    }
    "literal_with_LINE_FEED" in {
      test( "<http://a.example/s> <http://a.example/p> \"\\n\" .", 1){g=>
        g.triples.head.objectt.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="\n")
      }
    }
    "literal_with_CARRIAGE_RETURN" in {
      test( """<http://a.example/s> <http://a.example/p> "\r" .""", 1){g=>
        g.triples.head.objectt.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="\r")
      }
    }
    "literal_with_FORM_FEED" in {
      test( """<http://a.example/s> <http://a.example/p> "\f" .""", 1){g=>
        g.triples.head.objectt.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="\f")
      }
    }
    "literal_with_REVERSE_SOLIDUS" in {
      test( """<http://a.example/s> <http://a.example/p> "\\" .""", 1){g=>
        g.triples.head.objectt.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="""\""")
      }
    }
    "literal_with_numeric_escape4" in {
      test( """<http://a.example/s> <http://a.example/p> "\u006F" .""", 1){g=>
        g.triples.head.objectt.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="\u006F")
      }
    }
    "literal_with_numeric_escape8" in {
      test( """<http://a.example/s> <http://a.example/p> "\U0000006F" .""", 1){g=>
        g.triples.head.objectt.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="\u006F")
      }
    }
    "langtagged_string" in {
      test( """<http://a.example/s> <http://a.example/p> "chat"@en .""", 1){g=>
        val o = g.triples.head.objectt
        o.fold(uri=>false,bn=>false,lit=>lit.lexicalForm=="chat" && lit.lang == Some(Lang("en")))
      }
    }
    "lantag_with_subtag" in {
      test( """<http://example.org/ex#a> <http://example.org/ex#b> "Cheers"@en-UK .""", 1){g=>
        val o = g.triples.head.objectt
        o.fold(uri=>false,
               bn=>false,
               lit=> lit.lexicalForm=="Cheers" && lit.lang == Some(Lang("en-UK")))
      }
    }
    "minimal_whitespace" in {
      test( """<http://example/s><http://example/p><http://example/o>.
              |<http://example/s><http://example/p>"Alice".
              |<http://example/s><http://example/p>_:o.
              |_:s<http://example/p><http://example/o>.
              |_:s<http://example/p>"Alice".
              |_:s<http://example/p>_:bnode1.""".stripMargin, 6)
    }

  }

  "w3c tests of type rdft:TestNTriplesNegativeSyntax" should {

    def fail(s: String,erros: Int, test: List[Try[Rdf#Triple]] => Boolean = _ => true) = {
      val parseIterator = ntparser(s,true)
      val resultList = parseIterator.toList
      assert(test(resultList))
      assert(resultList.filter{
        case Failure(ParseException(_,-1,_))=>false
        case _ => true
      }.size == erros)
    }

    "nt-syntax-bad-uri-01" in {
      fail("""# Bad IRI : space.
           |<http://example/ space> <http://example/p> <http://example/o> .""".stripMargin,1)
    }
    "nt-syntax-bad-uri-02" in {
      fail("""# Bad IRI : bad escape
        |<http://example/\\u00ZZ11> <http://example/p> <http://example/o> .""".stripMargin,1)
    }
    "nt-syntax-bad-uri-03" in {
      fail("""# Bad IRI : bad escape
             |<http://example/\\U00ZZ1111> <http://example/p> <http://example/o> .""".stripMargin,1)
    }
    "nt-syntax-bad-uri-04" in {
      fail("""# Bad IRI : character escapes not allowed.
             |<http://example/\\n> <http://example/p> <http://example/o> .""".stripMargin,1)
    }
    "nt-syntax-bad-uri-05" in {
      fail("""# Bad IRI : character escapes not allowed.
             |<http://example/\\/> <http://example/p> <http://example/o> .""".stripMargin,1)
    }
    "nt-syntax-bad-uri-06" in {
      fail("""# No relative IRIs in N-Triples
        |<s> <http://example/p> <http://example/o> .""".stripMargin,1)
    }
    "nt-syntax-bad-uri-07" in {
      fail("""# No relative IRIs in N-Triples
             |<http://example/s> <p> <http://example/o> .""".stripMargin,1)
    }
    "nt-syntax-bad-uri-08" in {
      fail("""# No relative IRIs in N-Triples
             |<http://example/s> <http://example/p> <o> .""".stripMargin,1)
    }
    "nt-syntax-bad-uri-09" in {
      fail("""# No relative IRIs in N-Triples
             |<http://example/s> <http://example/p> "foo"^^<dt> .""".stripMargin,1)
    }
    "nt-syntax-bad-prefix-01" in {
      fail("""@prefix : <http://example/> .""".stripMargin,1)
    }
    "nt-syntax-bad-base-01" in {
      fail("""@base <http://example/> .""".stripMargin,1)
    }
    "nt-syntax-bad-struct-01" in {
      fail("""<http://example/s> <http://example/p> <http://example/o>, <http://example/o2> .""".stripMargin,1)
    }
    "nt-syntax-bad-struct-02" in {
      fail("""<http://example/s> <http://example/p> <http://example/o>; <http://example/p2>, <http://example/o2> .""".stripMargin,
        1)
    }
    "nt-syntax-bad-lang-01" in {
      fail("""# Bad lang tag
             |<http://example/s> <http://example/p> "string"@1 .""".stripMargin,1)
    }
    "nt-syntax-bad-esc-01" in {
      fail("""# Bad string escape
       |<http://example/s> <http://example/p> "a\zb" .""".stripMargin,1)
    }
    "nt-syntax-bad-esc-02" in {
      fail("""# Bad string escape
             |<http://example/s> <http://example/p> "\\uWXYZ" .""".stripMargin,1)
    }
    "nt-syntax-bad-esc-03" in {
       fail("""# Bad string escape
              |<http://example/s> <http://example/p> "\\U0000WXYZ" .""".stripMargin,1)
    }
    "nt-syntax-bad-string-01" in {
       fail("""<http://example/s> <http://example/p> "abc' .""".stripMargin,0) //we get an eof before the end of the string
    }
    "nt-syntax-bad-string-02" in {
      fail("""<http://example/s> <http://example/p> 1.0 .""".stripMargin,1)
    }
    "nt-syntax-bad-string-03" in {
      fail("""<http://example/s> <http://example/p> 1.0e1 .""".stripMargin,1)
    }
    "nt-syntax-bad-string-04" in {
      fail("""<http://example/s> <http://example/p> '''abc''' .""".stripMargin,1)
    }
    "nt-syntax-bad-string-05" in {
      fail("""<http://example/s> <http://example/p> ""\"abc\""\" .""".stripMargin,1)
    }
    "nt-syntax-bad-string-06" in {
      fail("""<http://example/s> <http://example/p> "abc .""".stripMargin,0) // we get an eof before the end of the string
    }
    "nt-syntax-bad-string-07" in {
      fail("""<http://example/s> <http://example/p> abc" .""".stripMargin,1)
    }
    "nt-syntax-bad-num-01" in {
      fail("""<http://example/s> <http://example/p> 1 .""".stripMargin,1)
    }
    "nt-syntax-bad-num-02" in {
      fail("""<http://example/s> <http://example/p> 1.0 .""".stripMargin,1)
    }
    "nt-syntax-bad-num-03" in {
      fail("""<http://example/s> <http://example/p> 1.0e0 .""".stripMargin,1)
    }
  }

  /**
   * Usefull method  for parsing large files to do speed tests
   * see: <a href="http://www.w3.org/wiki/DataSetRDFDumps">Data Set RDF Dumps</a>
   * @param args path_to_NTriplesFile [encoding]
   */
  def main(args: Array[String]): Unit = {
    import java.io._
    val encoding = if (args.length > 2) args(1) else "UTF-8"
    val ntp = new NTriplesParser[Rdf](
        new InputStreamReader(
          new FileInputStream(
            new File(args(0))), encoding), true)
    val t1 = System.currentTimeMillis()
    var x = 0
    var failures = 0
    while (ntp.hasNext) {
      val t = ntp.next
      x = x + 1
      if (t.isFailure) {
        println(s"\r\ntriple=$t")
        failures = failures + 1
      }
    }
    val t2 = System.currentTimeMillis()
    println(s"time to parse $x triples was ${t2 - t1} milliseconds. Found $failures failures. ")
  }


}

