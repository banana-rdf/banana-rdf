/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.rdf.n3

import org.scalacheck._
import Prop._

import _root_.nomo.Errors._
import collection.immutable.NumericRange
import collection.mutable.HashSet
import _root_.nomo.{Accumulators, Errors, Parsers, Monotypic}
import _root_.nomo.Accumulators.Position
import scala.collection.mutable
import org.w3.rdf._

/**
 * @author bblfish
 * @created 03/02/2012
 */


class NTriplesSpec[Rdf <: RDF](val ops: RDFOperations[Rdf]) extends Properties("NTriples") {
  
  val serializer = new Serializer[Rdf](ops)
  
  import ops._
  import serializer._
  
  val gen = new SpecTriplesGenerator[Rdf](ops)
  import gen._
  
  implicit def U : Listener[Rdf] = new Listener(ops)

  val P: NTriplesParser[Rdf, String, TreeMsg, Position, Listener[Rdf]] =
    new NTriplesParser(
      ops,
      Parsers(Monotypic.String, Errors.tree[Char], Accumulators.position[Listener[Rdf]](4)))

  
   property("lang") = forAll(genLangStr){ lang =>
     P.lang(lang).isSuccess
   }

  property("literal") = forAll(genUnicodeStr) { str=>
    val literal = NTriplesParser.toAsciiLiteral(str)
    val res= P.literal(literal)
    ("input literal="+literal+"evidence = '"+res+"'") |: all (
     res.isSuccess &&
     res.get.mkString == str )
  }


   property("Plainliteral") = forAll(genPlainLiteral) { case literal @ TypedLiteral(lit, tpe) =>
     val literalStr = '"'+NTriplesParser.toAsciiLiteral(lit)+'"'
     val res= P.fullLiteral(literalStr)
     ("literal in='"+literalStr+"' result = '"+res+"'") |: all(
      res.isSuccess &&
        res.get == literal
     )
   }

   property("langLiteral") = forAll(genLangLiteral) { case tst @ LangLiteral(lit, Lang(lang)) =>
     import NTriplesParser._
     val testStr = "\"" + toAsciiLiteral(lit) + "\"@" + lang
     val res = P.fullLiteral(testStr)
     ("input:" + testStr + "\nresult = '" + res + "'") |: all(
         ( res.isSuccess :| "res was failure" ) &&
         ({val LangLiteral(litres, x) = res.get;
           litres == lit} :| "literal string does not match") &&
         ({val LangLiteral(_,Lang(langRes)) =res.get
           langRes == lang} :| "literal is not a lang literal") &&
         ( { res.get == tst } :| "the final equality fails")
     )
   }


   property("uris") = secure { 
     val res =for ((uri,encoded) <- uriPairs) yield {
       var uriref = "<" + encoded + ">"
       val parsedUri = P.uriRef(uriref)
       ("result = " + parsedUri) |: all(
         (parsedUri.isSuccess :| "failure to parse") &&
           (parsedUri.get.isInstanceOf[IRI] :| "not an IRI") &&
           ((parsedUri.get == IRI(uri)) :| "iris don't match input")
       )
     }
     all(res :_*)
   }

  property("dataTypedLiteral") = forAll(genTypedLiteral) { case lit @ TypedLiteral(str, IRI(uri)) =>
    val literal = '"'+NTriplesParser.toAsciiLiteral(str)+"\"^^<"+NTriplesParser.toIRI(uri)+">"
    val res = P.fullLiteral(literal)
    val TypedLiteral(lit, IRI(dt_iri)) = res.get
    ("literal="+literal+"\nresult = "+res) |: all (
       res.isSuccess  &&
       lit == str &&
       dt_iri == uri
    )
  }
  
  property("statement") = forAll(genRelation){ case tr @ Triple(sub, rel, obj) =>
    val statement = tripleAsN3(tr)
    val res = P.nTriple(statement)
    ("statement to parse="+statement+" result ="+ res) |: all  (
      ( res.isSuccess :| "failed test") &&
      ( (res.get == tr) :| "parse produced a different result")
    )
  }
  
//  property("graph") = forAll(genGraph) {
//    triples =>
//      val graph = Graph(triples)
//      val doc = asN3(graph)
//      val res = P.ntriples(doc)
//
//      ("inputdoc="+doc+"\n----result ="+res) |: all (
//        res.isSuccess &&
//        ( (Graph(res.get.seq) == graph) :| "the parsed graph does not contain the same relations as the original")
//      )
//  }

 property("messyGraph") = forAll(genGraph) {
    graph =>
      val doc = generateDoc(graph)
      val res = P.nTriplesList(doc)
      val set = HashSet(graph)

      ("inputdoc="+doc+"\n----result ="+res) |: all (
        res.isSuccess &&
        ( (HashSet(res.get) == set) :| "the parsed graph does not contain the same relations as the original")
      )
  }

  property("messyGraphWithListener") = forAll(genGraph) {
    graph =>
      val doc = generateDoc(graph)
      val res = P.nTriples(doc)
      val set = HashSet(graph:_*)

      val resSet = res.user.queue.map(_.asInstanceOf[Triple]).toSet

      ("input graph="+graph+"\ninput set="+set+"\ninputdoc="+doc+"\nresult ="+res+"\nuser content="+resSet) |: all (
        res.isSuccess &&
          ( (resSet == set) :| "the parsed graph does not contain the same relations as the original")
      )
  }

  /** Generate an NTriples document from the statements but with very messy whitespacing */
  def generateDoc(graph: List[Triple]) = {
    val b = new StringBuilder
    if (graph != Nil) { //the pattern matching below does not work well enough at present
      for (Triple(s, r, o) <- graph) {
        b.append(genAnySpace.sample.get)
        b.append(nodeAsN3(s))
        b.append(genSpace.sample.get)
        b.append(nodeAsN3(r))
        b.append(genSpace.sample.get)
        b.append(nodeAsN3(o))
        b.append(genSpace.sample.get)
        b.append(".")
        b.append(newline.sample.get)
      }
    }
    b.append(genAnySpace.sample.get)
    b.toString
  }


}

class SpecTriplesGenerator[Rdf <: RDF](val ops: RDFOperations[Rdf]) {
  import ops._
  val uris = List[String]("http://bblfish.net/", "http://www.w3.org/community/webid/",
    "http://www.w3.org/2005/Incubator/webid/team#we", "http://www.ietf.org/rfc/rfc3986.txt",
    "ftp://ftp.is.co.za/rfc/rfc1808.txt", "ldap://[2001:db8::7]/c=GB?objectClass?one",
    "mailto:John.Doe@example.com", "news:comp.infosystems.www.servers.unix",
    "tel:+1-816-555-1212", "telnet://192.0.2.16:80/",
    "foo://example.com:8042/over/there?name=ferret#nose",
    "urn:example:animal:ferret:nose",
    "http://www.w3.org/1999/02/22-rdf-syntax-ns#en",
    "http://www.w3.org/1999/02/22-rdf-syntax-ns#de",
    "http://www.w3.org/1999/02/22-rdf-syntax-ns#fr",
    "http://www.w3.org/2001/XMLSchema#string",
    "http://nasdaq.com/'AAPL",
    "http://lewis.org/ont/modal/▢⇾",   //unicode examples
    "http://xmlns.com/foaf/emotion#♡",
    "http://xmlns.com/foaf/emotion#☺",
    "http://nasdaq.org/\"W3C",
    "http://bush.gov/\r\n/bornagain",
    "http://alcoholics.anonymous/\r\n/startAgain",
    "http://moveon.org/\f/print",
    "http://beyond.com/\t/flexibility",
    "http://über.info/>"
  )

  val encodedURIs = uris.map(u => NTriplesParser.toIRI(u))

  val uriPairs = uris.zip(encodedURIs)


  def ntripleChar = Gen.frequency(
    (1,Gen.oneOf("\\'\"\t\r\n")),
    (2,Gen.oneOf(NumericRange(1,256,1))),
    (1,genSpace),
    (10,unicodeChar)
  )

  def unicodeChar = Arbitrary.arbChar.arbitrary

  def newline: Gen[String] = Gen.oneOf(Array("\n","\r","\r\n"))
  def genSimpleLang = Gen.alphaChar.combine(Gen.alphaNumChar) { (oc,ocn)=> Some(""+oc.get+ocn.get) }

  def genLangStr = Gen.choose(1,3).flatMap {n=>
    for (cs <- Gen.containerOfN[List,String](n, genSimpleLang) ) yield cs.mkString("-")
  }

  def genUnicodeStr: Gen[String] = for(cs <- Gen.listOf(ntripleChar)) yield cs.mkString

  def genIRI = Gen.oneOf(uris).map( u => IRI(u) )

  def genPlainLiteral = for (str <- genUnicodeStr) yield TypedLiteral(str)
  def genTypedLiteral = for (str <- genUnicodeStr;
                             tpe <- genIRI) yield TypedLiteral(str, tpe)
  def genLangLiteral = for (str <- genUnicodeStr;
                            lang <- genLangStr) yield LangLiteral(str, Lang(lang))
  def genBnode = Gen.identifier.map(id => BNode(id))

  def genAnyNode = Gen.oneOf(genPlainLiteral, genTypedLiteral, genLangLiteral, genBnode, genIRI)
  def genSubjNode = Gen.oneOf(genIRI, genBnode)
  def genRelation =
    for {
      subj <- genSubjNode
      rel <- genIRI
      obj <- genAnyNode
    } yield Triple(subj, rel, obj)
  def genGraph = Gen.listOf(genRelation)
  def genSpace: Gen[String] = Gen.listOf1(Gen.oneOf(" \t")).map(_.mkString)
  def genAnySpace = Gen.listOf1(Gen.oneOf(" \t\n\r  ")).map(_.mkString)
  def genComment: Gen[String] = for {
    space <- genSpace
    str <- genUnicodeStr
    eol <- newline
  } yield {
    val lines = str.split("[\r\n]")
    val line = if (lines.size==0) "" else lines(0)
    ""+space + "#" + line + eol
  }

}

