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

package org.w3.banana.diesel

import org.w3.banana.*
import org.w3.banana.RDF.*
import scala.Seq

// for arrows going backwards (so we can have a literal pointer)
case class PredicatePointedRGraph[R <: RDF](
    pred: rURI[R],
    objectPG: PointedRGraph[R] // was objectPG -<- pred -- ???
)(using ops: Ops[R]):
   import ops.given

   infix def --(s: RDF.rStatement.Subject[R]): PointedRGraph[R] =
      import objectPG.{graph as acc, pointer as o}
      val graph = acc + rTriple(s, pred, o)
      PointedRGraph(o, graph)

   infix def --(pointedSubject: PointedSubjRGraph[R]): PointedRGraph[R] =
      import objectPG.graph as acc
//, pointer as o}
//      import pointedSubject.{graph as graphObject, pointer as s}
      val graphObject: RDF.rGraph[R] = pointedSubject.graph
      val s: rStatement.Subject[R]   = pointedSubject.pointer
      val o: rStatement.Object[R]    = objectPG.pointer

      // todo: check efficiency...
      val trs: Seq[RDF.rTriple[R]]    = graphObject.triples.toSeq
      val newTriple: RDF.rTriple[R]   = ops.rTriple(s, pred, o)
      val newTrs: Seq[RDF.rTriple[R]] = newTriple +: trs
      val graph                       = acc ++ newTrs
      PointedRGraph(o, graph)

end PredicatePointedRGraph
