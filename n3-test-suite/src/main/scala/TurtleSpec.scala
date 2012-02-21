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

  property("good IRIs") = secure {
    val results = for (iri <- uris) yield {
      System.out.println("iri="+iri)
      val iriRef = "<" + iri + ">"
      System.out.println(iriRef)
      val res = P.IRI_REF(iriRef)

      ("prefix line in='" + iriRef + "' result = '" + res + "'") |: all(
        res.isSuccess &&
          (res.get == iri)
      )
    }
    all(results :_*)
 }

  property("test comment generator") = forAll( genComment ) {
    comm =>
      System.out.println("comment test:"+comm+"---")
      ("comment is =[" + comm + "]") |: all(
        comm.startsWith("#") &&
          (comm.endsWith("\n") || comm.endsWith("\r"))
      )
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
