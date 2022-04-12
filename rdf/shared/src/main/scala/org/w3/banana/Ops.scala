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

import org.w3.banana.RDF.Graph
import org.w3.banana.RDF.Statement.Subject
import org.w3.banana.operations.StoreFactory

import scala.annotation.targetName
import scala.reflect.TypeTest
import scala.util.Try

trait Ops[Rdf <: RDF]:
   import RDF.*
   import RDF.Statement as St

   // interpretation types to help consistent pattern matching across implementations
   val `*`: RDF.NodeAny[Rdf]

   given Graph: operations.Graph[Rdf]

   given basicStoreFactory: StoreFactory[Rdf]
   given Store: operations.Store[Rdf]

   val rGraph: operations.rGraph[Rdf]

   val Subject: operations.Subject[Rdf]

   val Quad: operations.Quad[Rdf]
   given operations.Quad[Rdf] = Quad

   given Triple: operations.Triple[Rdf]

   val rTriple: operations.rTriple[Rdf]

   given Node: operations.Node[Rdf]

   // todo? should a BNode be part of a Graph (or DataSet) as per Benjamin Braatz's thesis?
   given BNode: operations.BNode[Rdf]
   given bnodeTT: TypeTest[Matchable, RDF.BNode[Rdf]]

   val Literal: operations.Literal[Rdf]
   export Literal.LiteralI.*
   given operations.Literal[Rdf] = Literal

   given literalTT: TypeTest[Matchable, RDF.Literal[Rdf]]

   val rURI: operations.rURI[Rdf]

   given URI: operations.URI[Rdf]

   given Lang: operations.Lang[Rdf]

end Ops
