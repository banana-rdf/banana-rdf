/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.rdf.n3

/*
* Copyright (c) 2012 Henry Story
* under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
*/

import org.scalacheck._
import Prop._
import nomo.Errors.TreeError
import nomo.Accumulators.Position
import nomo.Parsers._
import nomo.{Parsers, Accumulators, Errors, Monotypic}
import org.w3.rdf._
import java.nio.charset.Charset
import java.io.{BufferedReader, StringReader}

/**
 * @author bblfish
 * @created 20/02/2012
 */
class TurtleSpec[Rdf <: RDF](val ops: RDFOperations[Rdf],
                                              val isomorphism: GraphIsomorphism[Rdf]) extends Properties("Turtle") {
  import ops._
  import isomorphism._
  
  import System.out

  val gen = new SpecTurtleGenerator[Rdf](ops)
  import gen._

  val serializer = new Serializer(ops)


  val P = new TurtleParser(
      ops,
      Parsers(Monotypic.String, Errors.tree[Char], Accumulators.position[Listener[Rdf]](4)))

  implicit def U: Listener[Rdf] = new Listener(ops)

  property("good prefix type test") = secure {
    val res = for ((orig,write) <- zipPfx) yield {
      val result = P.PNAME_NS(write)
      ("prefix in = '"+write+"' result = '"+result+"'") |: all (
         result.isSuccess &&
         result.get == orig
      )
    }
    all(res :_*)
  }

  property("bad prefix type test") = secure {
    val res = for (pref <- bdPfx) yield {
      val res = P.PNAME_NS(pref)
      ("prefix in = '"+pref+"' result = '"+res+"'") |: all (
        res.isFailure
      )
    }
    all(res :_*)
  }

  property("good IRIs") = secure {
    val results = for ((pure,encoded) <- uriPairs) yield {
      val iriRef = "<" + encoded + ">"
      val res = P.IRI_REF(iriRef)

      ("prefix line in='" + iriRef + "' result = '" + res + "'") |: all(
        res.isSuccess &&
          (res.get == IRI(pure))
      )
    }
    all(results :_*)
 }

  val commentStart = "^[ \t]*#".r

  property("test comment generator") = forAll( genComment ) {
    comm: String =>
      ("comment is =[" + comm + "]") |: all(
        commentStart.findFirstIn(comm) != None &&
          (comm.endsWith("\n") || comm.endsWith("\r"))
      )
  }


  property("test comment parser") = forAll { (str: String) =>
      val line = str.split("[\n\r]")(0)
      val comm = "#"+ line +"\r\n"
      val res = P.comment(comm)
      ("comment is =[" + comm + "] result="+res) |: all(
        res.isSuccess &&
          res.get.toSeq.mkString == line
      )
  }

  property("test space generator") = forAll ( genSpace ) {
    space: String =>
      ("space is =["+space+"]") |: all  (
        !space.exists(c => c != '\t' && c != ' ')
      )
  }
  def encoder = Charset.forName("UTF-8").newEncoder

  property("simple good first half of @prefix (no weird whitepace or comments") = secure {
    val results = for (prefix <- gdPfx) yield {
      val pre = "@prefix " + prefix
      try {
        val res = P.PREFIX_Part1(pre)
        ("prefix line in='" + pre + "' result = '" + res + "'") |: all(
          res.isSuccess
        )
      } catch {
        case e => { e.printStackTrace(); throw e }
      }
    }
    all(results :_*)
  }

  property("good prefix line tests") = secure {
    val results = for ((origPfx,encodedPfx) <- zipPfx;
                       (pureIRI,encodedIRI) <- uriPairs) yield {
      try {
        val user = U
        val space1 = genSpaceOrComment.sample.get
        val space2 = genSpaceOrComment.sample.get
        val preStr = "@prefix" + space1 + encodedPfx + space2 + "<" + encodedIRI + ">"
        val res = P.prefixID(preStr)(user)
        val (parsedPre,parsedIRI) = res.get
        val prefixes = user.prefixes

        ("prefix line in='" + preStr + "' result = " +res+ "user prefixes="+prefixes) |: all(
          res.isSuccess &&
            ((origPfx == parsedPre) :| "original and parsed prefixes don't match") &&
            ((IRI(pureIRI) == parsedIRI) :| "original and parsed IRI don't match ") &&
            ((prefixes.get(parsedPre)  == Some(parsedIRI)) :| "parsed prefixes did not end up in user") &&
            ((prefixes.get(origPfx)  == Some(IRI(pureIRI))) :|
              "userPrefixHash["+origPfx+"] did not return the "+
              " original iri "+pureIRI)
        )
      } catch {
        case e => {
          e.printStackTrace(); throw e
        }
      }
    }
    all(results :_*)
  }

  property("good base tests") = secure {
    val results = for ((pure,encoded) <- uriPairs) yield {
      try {
        val user = U
        val space1 = genSpaceOrComment.sample.get
        val space2 = genSpaceOrComment.sample.get
        val preStr = "@base" + space1 + "<" + encoded + ">"+space2
        val res = P.base(preStr)(user)
        val base = res.get
        val prefixes = user.prefixes

        ("prefix line in='" + preStr + "' result = " +res) |: all(
          res.isSuccess &&
            (( base == IRI(pure)) :| "the decoded base differs from the original one") &&
            ((prefixes.get("")  == Some(IRI(pure))) :| "base did not end up in user state")
        )
      } catch {
        case e => {
          e.printStackTrace(); throw e
        }
      }
    }
    all(results :_*)
  }

  property("test Prefix Name non empty local part") = secure {
    val results = for (
      (origLcl,wLcl) <- zipPrefLocal
      if (origLcl != "")
    ) yield try {
        val res = P.PN_LOCAL(wLcl)
        ("name=["+wLcl+"] result="+res) |: all (
          res.isSuccess &&
            ((res.get == origLcl) :| "original and parsed data don't match!"  )
        )
      } catch {
        case e => {
          e.printStackTrace();
          throw e
        }
      }
    all(results :_*)

  }

  property("test namespaced names") = secure {
     val results = for (
       (origPfx,wPfx) <- zipPfx;
       (origLcl,wLcl) <- zipPrefLocal
     ) yield try {
       val name =wPfx+wLcl+genSpace.sample.get
       val res = P.PrefixedName(name)
       ("name=["+name+"] result="+res) |: all (
        res.isSuccess &&
        ((res.get == PName(origPfx,origLcl)) :| "original and parsed data don't match!"  )
       )
     } catch {
         case e => {
           e.printStackTrace();
           throw e
         }
     }
    all(results :_*)
  }

  lazy val simple_sentences = Array(
    "<http://bblfish.net/#hjs> <http://xmlns.com/foaf/0.1/knows> <http://www.w3.org/People/Berners-Lee/card#i> .",
    ":me foaf:knows bl:tim",
    ":me a foaf:Person"
  )

  property("test NTriples simple sentences") = secure {
      val t=Triple(IRI("http://bblfish.net/#hjs"),IRI("http://xmlns.com/foaf/0.1/knows"), IRI("http://www.w3.org/People/Berners-Lee/card#i"))
      val res = P.triples( "<http://bblfish.net/#hjs> <http://xmlns.com/foaf/0.1/knows> <http://www.w3.org/People/Berners-Lee/card#i> .")
    ("Initial Triple="+t+" res="+res+" res.user.queue="+res.user.queue) |: all (
      res.isSuccess  &&
      res.user.queue.size == 1 &&
      res.user.queue.head == t
    )
  }
  val f_knows = IRI("http://xmlns.com/foaf/0.1/knows")
  val f_mbox = IRI("http://xmlns.com/foaf/0.1/mbox")
  val f_name = IRI("http://xmlns.com/foaf/0.1/name")
  val hjs=IRI("http://bblfish.net/#hjs")
  val timbl = IRI("http://www.w3.org/People/Berners-Lee/card#i")
  val presbrey = IRI("http://presbrey.mit.edu/foaf#presbrey")
  val t=Triple(hjs,f_knows, timbl)
  val t2=Triple(hjs,f_knows, presbrey)
  val t3=Triple(hjs,f_mbox, IRI("mailto:henry.story@bblfish.net"))
  val t4=Triple(hjs,f_name, LangLiteral("Henry Story",Lang("en")))
  val t5=Triple(hjs,f_name, TypedLiteral("bblfish"))

  property("test multiple Object sentence") = secure {
    val g= Graph(t,t2)
    val res = P.triples( """<http://bblfish.net/#hjs> <http://xmlns.com/foaf/0.1/knows> <http://www.w3.org/People/Berners-Lee/card#i>,
    <http://presbrey.mit.edu/foaf#presbrey> .""")

    ("result="+res +" res.user.queue="+res.user.queue) |: all (
      res.isSuccess  &&
        res.user.queue.size == 2 &&
        Graph(res.user.queue.toIterable) == g
    )
  }

  property("test multiple objects and predicates") = secure {
    val g= Graph(t,t2,t3)
    val res = P.triples( """<http://bblfish.net/#hjs> <http://xmlns.com/foaf/0.1/knows> <http://www.w3.org/People/Berners-Lee/card#i>,
    <http://presbrey.mit.edu/foaf#presbrey>;
        <http://xmlns.com/foaf/0.1/mbox> <mailto:henry.story@bblfish.net>""")

    ("result="+res +" res.user.queue="+res.user.queue) |: all (
      res.isSuccess  &&
        res.user.queue.size == 3 &&
        Graph(res.user.queue.toIterable) == g
    )
  }

  property("test multiple objects and literal predicates") = secure {
    val g= Graph(t,t2,t3,t4,t5)
    val res = P.triples( """<http://bblfish.net/#hjs> <http://xmlns.com/foaf/0.1/knows> <http://www.w3.org/People/Berners-Lee/card#i>,
    <http://presbrey.mit.edu/foaf#presbrey>;
        <http://xmlns.com/foaf/0.1/mbox> <mailto:henry.story@bblfish.net> ;
        <http://xmlns.com/foaf/0.1/name> "Henry Story"@en, 'bblfish' """)

    ("result="+res +" res.user.queue="+res.user.queue) |: all (
      res.isSuccess  &&
        (( res.user.queue.size == 5 ) :| "the two graphs are not the same size" ) &&
        ((Graph(res.user.queue.toIterable) ==  g) :| "the two graphs are not equal")
    )
  }

  property("test multiple objects and literal predicates with prefixes") = secure {
    val g= Graph(t,t2,t3,t4,t5)
    val res = P.turtleDoc( """@prefix foaf: <http://xmlns.com/foaf/0.1/> .
    <http://bblfish.net/#hjs> foaf:knows <http://www.w3.org/People/Berners-Lee/card#i> ,
                                         <http://presbrey.mit.edu/foaf#presbrey>;
        foaf:mbox <mailto:henry.story@bblfish.net> ;
        foaf:name "Henry Story"@en, 'bblfish'. """)

    ("result="+res +" res.user.queue="+res.user.queue+ " res.user.prefixes"+res.user.prefixes) |: all (
      res.isSuccess  &&
        (( res.user.queue.size == 5 ) :| "the two graphs are not the same size" ) &&
        ((Graph(res.user.queue.toIterable) ==  g) :| "the two graphs are not equal")
    )
  }

  val lit1 = """
     Darkness at the break of noon
     Shadows even the silver spoon
     The handmade blade, the child's balloon
     Eclipses both the sun and moon
     To understand you know too soon
     There is no sense in trying.

     Pointed threats, they bluff with scorn
     Suicide remarks are torn
     From the fool's gold mouthpiece
     The hollow horn plays wasted words
     Proves to warn
     That he not busy being born
     Is busy dying.
"""
  val bobDylan=IRI("http://dbpedia.org/resource/Bob_Dylan")
  val t6= Triple(bobDylan,IRI("http://purl.org/dc/elements/1.1/created"),LangLiteral(lit1,Lang("en-us-poetic2")))
  val t7= Triple(bobDylan,f_name,LangLiteral("Bob Dylan",Lang("en")))

  property("test prefixes long literals and comments") = secure {
    val g= Graph(t6,t7)
    val doc = """
    @prefix dc: <http://purl.org/dc/elements/1.1/>.  #dot close to iri
    @prefix db:<http://dbpedia.org/resource/> #dot on next line to see
    . @prefix foaf: <http://xmlns.com/foaf/0.1/>  .#comment touching dot

    db:Bob_Dylan dc:created  #this is a long literal, so it starts on the next line
    '''%s'''@en-us-poetic2;  #comment after semicolon
        #can an name have a quote in it?
        foaf:name "Bob Dylan"@en.
    """.format(lit1)
    val res = P.turtleDoc(doc )
    ("result="+res +" res.user.queue="+res.user.queue+ " res.user.prefixes"+res.user.prefixes) |: all (
      res.isSuccess  &&
        (( res.user.queue.size == 2) :| "the two graphs are not the same size" ) &&
        ((Graph(res.user.queue.toIterable) ==  g) :| "the two graphs are not equal")
    )
  }


  property("test prefixes long literals and comments") = secure {
    val g= Graph(t6,t7)
    val doc = ("""#start with a commment is always good
    @prefix dc:<http://purl.org/dc/elements/1.1/>.  #dot and iri close together
    @prefix db:
         <http://dbpedia.org/resource/> #dot on next line to see
    .@prefix foaf: <http://xmlns.com/foaf/0.1/>  .#comment touching dot and @touching it too

    db:Bob_Dylan #comment after subject
        dc:created #comment after verb
    """+"\"\"\"%s\"\"\"" +"""@en-us-poetic2  #comment before semicolon
     ; #semicolon all alone

        foaf:name"Bob Dylan"@en.  #name touches literal
    """).format(lit1)
    val res = P.turtleDoc(doc )
    ("result="+res +" res.user.queue="+res.user.queue+ " res.user.prefixes"+res.user.prefixes) |: all (
      res.isSuccess  &&
        (( res.user.queue.size == 2) :| "the two graphs are not the same size" ) &&
        ((Graph(res.user.queue.toIterable) ==  g) :| "the two graphs are not equal")
    )
  }

  val hasCats = IRI("http://cats.edu/ont/has")

  val t8 = Triple(hjs,hasCats,TypedLiteral("2",xsdInteger))
  val t8bis = Triple(hjs,hasCats,TypedLiteral("3.2",xsdDecimal))
  val t9 = Triple(timbl,hasCats,TypedLiteral(".5e-42",xsdDouble))
  val t10 = Triple(presbrey,hasCats,TypedLiteral("3.14",xsdDecimal))

  property("test numbers") = secure {
    import serializer._
      val g = Graph(t8,t8bis,t9,t10)
    val doc = """
    @prefix cats: <http://cats.edu/ont/has>  .
         %s cats: 2, 3.2. #that last dot is an end of sentence
         %s cats: .5e-42 . #a homeopathic amount
         %s cats: 3.14.
      """.format(iriAsN3(hjs),iriAsN3(timbl),iriAsN3(presbrey))
    val res = P.turtleDoc(doc )
    ("result="+res +" res.user.queue="+res.user.queue+ " res.user.prefixes"+res.user.prefixes) |: all (
      res.isSuccess  &&
        (( res.user.queue.size == 4) :| "the two graphs are not the same size" ) &&
        ((Graph(res.user.queue.toIterable) ==  g) :| "the two graphs are not equal")
    )
  }

  //this does not fail but it does produce any results either. Not sure what to think of this
  property("test broken number doc ") = secure {
    import serializer._
    val doc = """
    @prefix cats: <http://cats.edu/ont/has>  .
         %s cats: e42 . #no number before the e
      """.format(iriAsN3(hjs))
    val res = P.turtleDoc(doc )
    ("result="+res +" res.user.queue="+res.user.queue+ " res.user.prefixes"+res.user.prefixes) |: all (
      res.isFailure
    )
  }

  property("test numbers") = secure {
  val nums = Map[TypedLiteral,Boolean](
     ("2"^^xsdInteger) -> true,
     ("23423.123"^^xsdDecimal) -> true,
     ("23423123123456789"^^xsdInteger) -> true,
     (".232e34"^^xsdDouble) -> true,
     (".123"^^xsdDecimal) -> true,
     ("23423.123"^^xsdDecimal) -> true,
     (".123"^^xsdDecimal) -> true,
     (".e34"^^xsdDouble) -> false,
     ("12.00123123e34"^^xsdDouble) -> true,
     ("12e34"^^xsdDouble) -> true,
     (""^^xsdDouble) -> false,
     ("-"^^xsdDouble) -> false,
     ("+"^^xsdDouble) -> false,
     ("+e32"^^xsdDouble) -> false,
     ("+2345.123"^^xsdDecimal) -> true,
     ("-34523.1978123"^^xsdDecimal) -> true,
     ("-2342312349853123123123123"^^xsdInteger) -> true,
     ("+2342139023"^^xsdInteger) -> true,
     ("+.4334e034"^^xsdDouble) -> true,
     (".123"^^xsdDecimal) -> true,
     ("23423.123"^^xsdDecimal) -> true,
     (".123"^^xsdDecimal) -> true,
     (".123"^^xsdDecimal) -> true,
     ("091.999"^^xsdDecimal) -> true,
     (".123"^^xsdDecimal) -> true
  )
       val res = for ((lit,valid) <- nums) yield {
         val TypedLiteral(str,tp) = lit
         val parsed = P.NumericLiteral(str)
         ( "input='"+lit+"' result="+parsed ) |: all (
           parsed.isSuccess == valid  &&
             (( if (parsed.isSuccess) parsed.get==lit else true) :| "the input and output literal don't match" )
         )
       }
      all(res.toSeq: _*)
  }

  property("test blank node subject") = secure {
    val t1 = Triple(BNode("_:n22"),f_name, "Alexandre" lang "fr" )
    val t2 = Triple(BNode(),f_name,"Henry")
    val t3 = Triple(BNode("_:n22"),f_knows,BNode("_:n22"))
    val g = Graph(t1,t2,t3)
    val doc = """
    @prefix foaf: <http://xmlns.com/foaf/0.1/> .
    _:n22 foaf:name "Alexandre"@fr . #simple bnode subject
    [] foaf:name "Henry" .
    _:n22 foaf:knows _:n22 .
    """
    out.println(doc)
    val res = P.turtleDoc(doc)
    try {
    ("result="+res +" res.user.queue="+res.user.queue+ " res.user.prefixes"+res.user.prefixes) |: all (
      res.isSuccess  &&
        (( res.user.queue.size == 3) :| "the two graphs are not the same size" ) &&
        ((Graph(res.user.queue.toIterable) isIsomorphicWith  g ) :| "the two graphs are not isomorphic")
    )
    } catch {
      case e => e.printStackTrace(); throw e
    }

  }

}


class SpecTurtleGenerator[Rdf <: RDF](override val ops: RDFOperations[Rdf])
extends SpecTriplesGenerator[Rdf](ops){

  val gdPfxOrig= List[String](":","cert:","foaf:","foaf.new:","a\u2764:","䷀:","Í\u2318-\u262f:",
    "\u002e:","e\u0eff\u0045:")
  //note: foaf.new does not NEED to have the . encoded as of spec of feb 2012. but it's difficult to deal with.
  //see https://bitbucket.org/pchiusano/nomo/issue/6/complex-ebnf-rule
  val gdPfx= List[String](":","cert:","foaf:","foaf\\u002enew:","a\\u2764:","䷀:","Í\\u2318-\\u262f:",
    "\\u002e:","e\\u0eff\\u0045:")
  val zipPfx = gdPfxOrig.zip(gdPfx)


  val bdPfx= List[String]("cert.:","2oaf:",".new:","❤:","⌘-☯:","","cert","foaf","e\\t:")

  val gdPfxLcl =   List[String]("_\u2071\u2c001.%34","0","00","","\u3800snapple%4e.\u00b7","_\u2764\u262f.\u2318",
    "%29coucou")
  //note:the '.' do not NEED to be encoded as of spec of feb 2012. but it's difficult to deal with.
  //see https://bitbucket.org/pchiusano/nomo/issue/6/complex-ebnf-rule
  val gdPfxLcl_W = List[String]("_\u2071\u2c001\\u002e%34","0","00","","\u3800snapple%4e\\u002e\\u00b7","_\\u2764\\u262f\\u002e\\u2318",
    "%29coucou")

  val zipPrefLocal = gdPfxLcl.zip(gdPfxLcl_W)

  def genSpaceOrComment = Gen.frequency(
    (1,genSpace),
    (1,genComment)
  )

}
