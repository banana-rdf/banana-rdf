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

package org.w3.banana.isomorphism

import org.w3.banana.*

trait IsomorphismBNodeTrait[Rdf <: RDF](using ops: Ops[Rdf]):
   import ops.{given, *}
   import RDF.*
   import RDF.Statement as St

   val foaf = prefix.FOAF[Rdf]
   val rdf = prefix.RDFPrefix[Rdf]
   val xsd = prefix.XSD[Rdf]

   val hjs = URI("http://bblfish.net/people/henry/card#me")
   val timbl = URI("http://www.w3.org/People/Berners-Lee/card#i")

   def alex(i: Int): BNode[Rdf] = BNode("alex" + i)

   def antonio(i: Int): BNode[Rdf] = BNode("antonio" + i)

   def groundedGraph = Graph(
     Triple(hjs, foaf.knows, timbl),
     Triple(hjs, foaf.name, Literal("Henry Story"))
   )

   def num(i: Int): Literal[Rdf] = Literal(i.toString, xsd.integer)

   def bn(bnprefix: String)(i: Int) = BNode(bnprefix + i)

   def list(size: Int, bnPrefix: String) =
      val p = bnPrefix
      (1 to size).foldRight(
        Graph(Triple(bn(p)(0), rdf.first, num(0)), Triple(bn(p)(0), rdf.rest, rdf.nil))
      ) {
        case (i, g) =>
          g `union` Graph(
            Triple(bn(p)(i), rdf.first, num(i)),
            Triple(bn(p)(i), rdf.rest, bn(p)(i - 1))
          )
      }

   //  val bnodeGraph = (
   //      toPointedGraphW[Rdf](URI("#me"))
   //        -- foaf.knows ->- toPointedGraphW[Rdf](bnode("alex"))
   //    ).graph union (
   //      toPointedGraphW[Rdf](bnode("alex"))
   //        -- foaf.name ->- "Alexandre Bertails"
   //    ).graph

   def bnAlexRel1Graph(i: Int = 1): Graph[Rdf] = Graph(
     Triple(alex(i), foaf.homepage, URI("http://bertails.org/"))
   )

   def bnAlexRel2Graph(i: Int = 1): Graph[Rdf] = Graph(
     Triple(hjs, foaf.knows, alex(i)),
     Triple(alex(i), foaf.name, Literal("Alexandre Bertails"))
   )

   def bnAntonioRel1Graph(i: Int = 1) =
     Graph(Triple(antonio(i), foaf("homepage"), URI("https://github.com/antoniogarrote/")))

   def bnAntonioRel2Graph(i: Int = 1) = Graph(
     Triple(hjs, foaf.knows, antonio(i)),
     Triple(antonio(i), foaf.name, Literal("Antonio Garrote"))
   )

   def xbn(i: Int) = BNode("x" + i)

   def bnKnowsBN(i: Int, j: Int) = Graph(
     Triple(xbn(i), foaf.knows, xbn(j))
   )

   def symmetricGraph(i: Int, j: Int) = bnKnowsBN(i, j) `union` bnKnowsBN(j, i)

   def owlSameAs(node1: St.Subject[Rdf], node2: St.Object[Rdf]) =
     Graph(Triple(node1, URI("http://www.w3.org/2002/07/owl#sameAs"), node2))
