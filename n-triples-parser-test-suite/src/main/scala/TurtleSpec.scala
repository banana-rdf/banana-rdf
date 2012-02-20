package org.w3.rdf

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

/**
 * @author bblfish
 * @created 20/02/2012
 */
class TurtleSpec [M <: Module](val m: M)  extends Properties("Turtle") {
  import m._

  val gen = new SpecTurtleGenerator[m.type](m)
  import gen._

  val P: TurtleParser[M, String, TreeError, Position, Listener] = new TurtleParser(m,
    Parsers(Monotypic.String, Errors.tree[Char], Accumulators.position[Listener](4)))

  implicit def U: Listener = new Listener()

  property("good prefix type test") = secure {
    val res = for (pref <- goodPrefixes) yield {
      System.out.println("good="+pref+"-")
      val result = P.PNAME_NS(pref)
      ("prefix in = '"+pref+"' result = '"+result+"'") |: all (
         result.isSuccess //todo: find a way to compare input and compted value precisely
      )
    }
    all(res :_*)
  }

  property("bad prefix type test") = secure {
    val res = for (pref <- badPrefixes) yield {
      System.out.println("bad="+pref+"-")
      val res = P.PNAME_NS(pref)
      ("prefix in = '"+pref+"' result = '"+res+"'") |: all (
        res.isFailure
      )
    }
    all(res :_*)

  }

  property("good prefix line tests") = secure {
    val results = for (prefix <- goodPrefixes;
               iri <- uris) yield {
      System.out.println("prefix="+prefix)
      System.out.println("iri="+iri)
      val pre = "@prefix" + genSpaceOrComment.sample.get + prefix + genSpaceOrComment.sample.get + "<" + iri + ">"
      System.out.println(pre)
      val res = P.prefixID(pre)

      ("prefix line in='" + pre + "' result = '" + res + "'") |: all(
        res.isSuccess &&
          (res.get._2 == iri)
      )
    }
    all(results :_*)
  }

//  property("fixed bad prefix tests") {
//
//  }


}
