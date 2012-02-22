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
import org.w3.rdf.RDFModule
import java.nio.charset.Charset
import java.io.{BufferedReader, StringReader}

/**
 * @author bblfish
 * @created 20/02/2012
 */
class TurtleSpec [M <: RDFModule](val m: M)  extends Properties("Turtle") {
  import m._

  val gen = new SpecTurtleGenerator[m.type](m)
  import gen._

  val P: TurtleParser[M, String, TreeError, Position, Listener] = new TurtleParser(m,
    Parsers(Monotypic.String, Errors.tree[Char], Accumulators.position[Listener](4)))

  implicit def U: Listener = new Listener()

  property("good prefix type test") = secure {
    val res = for (pref <- goodPrefixes) yield {
      val result = P.PNAME_NS(pref)
      ("prefix in = '"+pref+"' result = '"+result+"'") |: all (
         result.isSuccess //todo: find a way to compare input and compted value precisely
      )
    }
    all(res :_*)
  }

  property("bad prefix type test") = secure {
    val res = for (pref <- badPrefixes) yield {
      val res = P.PNAME_NS(pref)
      ("prefix in = '"+pref+"' result = '"+res+"'") |: all (
        res.isFailure
      )
    }
    all(res :_*)
  }

  property("good IRIs") = secure {
    val results = for (iri <- uris) yield {
      val iriRef = "<" + iri + ">"
      val res = P.IRI_REF(iriRef)

      ("prefix line in='" + iriRef + "' result = '" + res + "'") |: all(
        res.isSuccess &&
          (res.get == iri)
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
    val results = for (prefix <- goodPrefixes) yield {
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
    val results = for (prefix <- goodPrefixes;
               iri <- uris) yield {
      val space1 = genSpaceOrComment.sample.get
      val space2 = genSpaceOrComment.sample.get
      val pre = "@prefix" + space1 + prefix + space2 + "<" + iri + ">"
      try {
      val res = P.prefixID(pre)
      ("prefix line in='" + pre + "' result = '" + res + "'") |: all(
        res.isSuccess &&
          (res.get._2 == iri)
      )
      } catch {
        case e => { e.printStackTrace(); throw e }
      }
    }
    all(results :_*)
  }

//  property("fixed bad prefix tests") {
//
//  }


}


class SpecTurtleGenerator[M <: RDFModule](override val m: M)  extends SpecTriplesGenerator[M](m){

  val goodPrefixes= List[String](":","cert:","foaf:","foaf.new:","a\\u2764:","䷀:","Í\\u2318-\\u262f:",
    "\\u002e:","e\\u0eff\\u0045:","e\\t:")
  val badPrefixes= List[String]("cert.:","2oaf:",".new:","❤:","⌘-☯:","","cert","foaf")

  def genSpaceOrComment = Gen.frequency(
    (1,genSpace),
    (1,genComment)
  )

}
