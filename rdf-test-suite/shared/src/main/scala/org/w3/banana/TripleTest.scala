/*
 *  Copyright (c) 2012 , 2021 W3C Members
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  This program and the accompanying materials are made available under
 *  the W3C Software Notice and Document License (2015-05-13) which is available at
 *  https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document.
 *
 *  SPDX-License-Identifier: W3C-20150513
 */

package org.w3.banana

import org.w3.banana.TestConstants.{bbl, bfsh, foafPre, tim}
import org.w3.banana.prefix.{FOAF, XSD}
import org.w3.banana.{Ops, RDF}

import scala.reflect.TypeTest

/** we cover here URI, BNode, Literal, Triple and Quad tests
  */
open class TripleTest[R <: RDF](using ops: Ops[R])
    extends munit.FunSuite:

   import RDF.*
   import ops.{*, given}

   test("BNode test") {
     val bn  = BNode()
     val bn1 = BNode("b1")
     assertEquals(bn1.label, "b1")
     assert(bn.label != bn1.label)
   }

   val xsd: XSD[R]   = XSD[R]
   val foaf: FOAF[R] = FOAF[R]
   val timbl: URI[R] = URI(tim("i"))
   val bblf: URI[R]  = URI(bbl("i"))
   val henry: URI[R] = URI(bfsh("people/henry/card", "me"))

   test("URI Test") {
     assertEquals(timbl.value, tim("i"))
     assertEquals(bblf.value, bbl("i"))
   }

   test("type test on literal") {
     // this gives a warning: "cannot call the type test at runtime"!
     val timNode: RDF.Node[R] = Literal("Tim")
     timNode match
        case t: RDF.Literal[R] =>
          assert(true, "good")
        case _ => fail("could not match literal")
   }

   test("Literal Tests") {
     val timLit: RDF.Literal[R] = Literal("Tim")
     assertEquals(timLit.text, "Tim")
     timLit match
        case t: RDF.Literal[R] =>
          assert(true, "can't fail to get here")
     // assertEquals(t.text, "Tim")

     val hname: RDF.Literal[R] = "Henry" `@` Lang("en")
     assertEquals(hname.text, "Henry")

     hname match
        case Literal(n `@` l) =>
          assertEquals(n, "Henry")
          assertEquals(l, Lang("en"))

     val hnode: RDF.Node[R] = hname
     hnode match
        case Literal(n `@` l) =>
          assertEquals(n, "Henry")
          assertEquals(l, Lang("en"))
        case _ => fail(s"ca not match $hnode as a lang node ")

     val age = "999" ^^ xsd.integer
     assertEquals(age.text, "999")
     age match
        case Literal(y `^^` t) =>
          assertEquals(y, "999")
          assertEquals(t, xsd.integer)
        case _ => fail(s"can not match $age as a datatype ")
     val dtEx = List(timLit, hname, age, "2001-10-26T21:32:52+02:00" ^^ xsd.dateTime)
     val trans = dtEx.map {
       case Literal(li) =>
         li match
            case Plain(text)      => text
            case t `@` l          => t + ":" + l
            case t ^^ xsd.integer => "#" + t
            case _                => "other"
     }
     assertEquals(trans, List("Tim", "Henry:en", "#999", "other"))
     val trans2 = dtEx.map((lit: Literal[R]) =>
       lit.fold[String](
         identity,
         (t, l) => t + ":" + l,
         (t, dt) => if dt == xsd.integer then "#" + t else "other"
       )
     )
     assertEquals(trans2, trans)
   }

   test("Node Tests") {
     val nodes: Seq[Node[R]] = List(
       BNode("b1"),
       bblf,
       timbl,
       foaf.knows,
       "2001-10-26T21:32:52+02:00" ^^ xsd.dateTime,
       Literal("hello"),
       "Tim" `@` Lang("en")
     )
     val nodeStrings = nodes.map(_.fold(
       uri => uri.value,
       bn => bn.label,
       lit =>
         lit.fold(
           identity,
           (t, l) => t + ":" + l,
           (t, dt) => if dt == xsd.integer then "#" + t else t
         )
     ))
     assertEquals(
       nodeStrings,
       List(
         "b1",
         bbl("i"),
         tim("i"),
         foaf.knows.value,
         "2001-10-26T21:32:52+02:00",
         "hello",
         "Tim:en"
       )
     )
   }

   test("triple tests") {
     val bkt = Triple(bblf, foaf.knows, timbl)
     assertEquals[Node[R], Node[R]](bkt.subj, bblf)
     assertEquals[Node[R], Node[R]](bkt.rel, foaf.knows)
     assertEquals[Node[R], Node[R]](bkt.obj, timbl)

     bkt match
        case Triple(t) =>
          t match
             case (b, k, t) =>
               assertEquals[Node[R], Node[R]](t, timbl)
               assertEquals(k, foaf.knows)
               assertEquals[Node[R], Node[R]](b, bblf)
     val tkb = Triple(timbl, foaf.knows, bblf)
     tkb match
        case Triple(t, k, b) =>
          assertEquals[Node[R], Node[R]](t, timbl)
          assertEquals(k, foaf.knows)
          assertEquals[Node[R], Node[R]](b, bblf)
        case _ => fail("failed to match the triple we constructed")
     val tname = Triple(timbl, foaf.name, "Tim" `@` Lang("en"))
     tname match
        case Triple(t, p, Literal(name `@` lang)) =>
          assertEquals[Node[R], Node[R]](t, timbl)
          assertEquals(p, foaf.name)
          assertEquals(name, "Tim")
          assertEquals(lang, Lang("en"))
        case _ => fail(s"could not match $tname")
     val byear  = "1967" ^^ xsd.integer
     val bbyear = Triple(bblf, URI(foafPre("byear")), byear) // note: byear does not exist in foaf
     // this does not work with TypeTests. Try again when we can express Literal[R] <: Node[R]
     bbyear match
        case Triple(s, p, l: Literal[R]) =>
          assertEquals[Node[R], Node[R]](s, bblf)
     bbyear match
        case Triple(s, p, Literal(l)) =>
          assertEquals[Node[R], Node[R]](s, bblf)
          assertEquals(l.text, "1967")
        case _ => fail("pattern did not match")

     bbyear match
        case Triple(s, p, Literal(yearStr ^^ xsd.integer)) =>
          assertEquals[Node[R], Node[R]](s, bblf)
          assertEquals(yearStr, "1967")
        case _ => fail("pattern did not match")
   }

   import _root_.io.lemonlabs.uri as ll
   val card: ll.AbsoluteUrl = ll.AbsoluteUrl.parse("https://bblfish.net/people/henry/card")

   test("resolving relative triples") {
     val tr1 = rTriple(BNode("me"), foaf.name, Literal("Tim Berners-Lee"))
     assertEquals(tr1.resolveAgainst(card), (tr1.asInstanceOf[RDF.Triple[R]], false))
     assertEquals(
       rTriple(rURI("#me"), foaf.name, Literal("Henry Story")).resolveAgainst(card),
       (Triple(henry, foaf.name, Literal("Henry Story")), true)
     )
     val rbbl     = rTriple(rURI("#me"), foaf.knows, rURI("../tini/card#i"))
     val resolved = rbbl.resolveAgainst(card)
     assertEquals(
       resolved,
       (Triple(henry, foaf.knows, URI("https://bblfish.net/people/tini/card#i")), true)
     )

   }

   test("relativizing plain triples") {
     val tr1 = Triple(BNode("me"), foaf.name, Literal("Tim Berners-Lee"))
     assertEquals(tr1.relativizeAgainst(card), (tr1.asInstanceOf[RDF.rTriple[R]], false))
     assertEquals(
       Triple(henry, foaf.name, Literal("Henry Story")).relativizeAgainst(card),
       (rTriple(rURI("#me"), foaf.name, Literal("Henry Story")), true)
     )
     val ppl: ll.AbsoluteUrl = ll.AbsoluteUrl.parse("https://bblfish.net/people/")
     val rbbl                = rTriple(rURI("henry/card#me"), foaf.knows, rURI("tini/card#i"))
     assertEquals(
       Triple(henry, foaf.knows, URI("https://bblfish.net/people/tini/card#i")).relativizeAgainst(
         ppl
       ),
       (rbbl, true)
     )

   }

   test("quad tests") {
     val store: org.w3.banana.RDF.Store[R] = Store()
     val tkb4                              = Quad(timbl, foaf.knows, bblf)
     assertEquals(tkb4.subj, timbl)
     assertEquals(tkb4.rel, foaf.knows)
     assertEquals(tkb4.obj, bblf)
     assertEquals(tkb4.graph, store.default)
     assertEquals(tkb4.triple, Triple(timbl, foaf.knows, bblf))

     val tcard       = URI(tim(""))
     val bcard       = URI(bbl(""))
     val timSaysTkB4 = Quad(timbl, foaf.knows, bblf, tcard)
     // bbl says tkb
     val fishSaysTkB4 = Quad(timbl, foaf.knows, bblf, bcard)
     assertNotEquals(
       timSaysTkB4,
       fishSaysTkB4,
       "The same triple stated by two different docs are not the same statements"
     )

     store.add(tkb4, timSaysTkB4, fishSaysTkB4)

     store.add(tkb4, timSaysTkB4, fishSaysTkB4)
     assertEquals(store.find(`*`, `*`, `*`).toSet, Set(tkb4))
     assertEquals(store.find(`*`, `*`, `*`, `*`).toSet, Set(tkb4, fishSaysTkB4, timSaysTkB4))
     assertEquals(store.find(`*`, `*`, `*`, tcard).toSet, Set(timSaysTkB4))
     assertEquals(store.find(`*`, `*`, `*`, bcard).toSet, Set(fishSaysTkB4))

     // add it all again, we get the same
     store.add(tkb4, timSaysTkB4, fishSaysTkB4)
     assertEquals(store.find(`*`, `*`, `*`).toSet, Set(tkb4))
     assertEquals(store.find(`*`, `*`, `*`, `*`).toSet, Set(tkb4, fishSaysTkB4, timSaysTkB4))
     assertEquals(store.find(`*`, `*`, `*`, `*`).toList.size, 3)
     assertEquals(store.find(`*`, `*`, `*`, tcard).toSet, Set(timSaysTkB4))
     assertEquals(store.find(`*`, `*`, `*`, bcard).toSet, Set(fishSaysTkB4))

     // add graphs
     val timName3      = Triple(timbl, foaf.name, Literal("Tim"))
     val w3c           = URI("https://w3.org/")
     val timWorkPlace3 = Triple(timbl, foaf.workplaceHomepage, w3c)
     val timGr         = Graph(timWorkPlace3, timName3)

     val bblName3 = Triple(bblf, foaf.name, Literal("Henry"))
     val cosy     = URI("https://co-operating.systems/")
     val bblWork3 = Triple(bblf, foaf.workplaceHomepage, cosy)
     val bblGr    = Graph(bblName3, bblWork3)

     // test triple to quad
     val timName4 = timName3.at(tcard)
     assertEquals(timName4, Quad(timName3.subj, timName3.rel, timName3.obj, tcard))
     val timWorkPlace4 = timWorkPlace3.at(tcard)
     assertEquals(timWorkPlace4, Quad(timbl, foaf.workplaceHomepage, w3c, tcard))
     val bblWork4 = bblWork3.at(tcard)
     assertEquals(bblWork4, Quad(bblWork3.subj, bblWork3.rel, bblWork3.obj, tcard))

     // test adding graphs to store
     store.add(timGr, tcard)
     assertEquals(store.find(`*`, `*`, `*`, tcard).toSet, Set(timWorkPlace4, timName4, timSaysTkB4))
     assertEquals(store.find(`*`, `*`, `*`, bcard).toSet, Set(fishSaysTkB4))
     assertEquals(store.find(`*`, `*`, `*`).toSet, Set(tkb4))

     store.set(timGr)
     assertEquals(store.find(`*`, `*`, `*`).map(_.triple).toSet, timGr.triples.toSet)
   }
end TripleTest
