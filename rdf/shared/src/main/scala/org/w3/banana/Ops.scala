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
import org.w3.banana.prefix.{RDFPrefix, XSD}

import scala.annotation.targetName
import scala.reflect.TypeTest
import scala.util.Try

trait Ops[Rdf <: RDF]:
   ops =>
   import RDF.*
   import RDF.Statement as St

   // interpretation types to help consistent pattern matching across implementations
   val `*`: RDF.NodeAny[Rdf]

   lazy val rdf: RDFPrefix[Rdf] = prefix.RDFPrefix[Rdf](using ops)
   lazy val xsd: XSD[Rdf]       = prefix.XSD[Rdf](using ops)

   given Graph: operations.Graph[Rdf]

   given basicStoreFactory: StoreFactory[Rdf]
   given Store: operations.Store[Rdf]

   given rGraph: operations.rGraph[Rdf]

   given Subject: operations.Subject[Rdf]

//	given tripleTT: TypeTest[Matchable, Triple[Rdf]]
   val Quad: operations.Quad[Rdf]
   given operations.Quad[Rdf] = Quad

//	extension (obj: Statement.Object[Rdf])
//		def fold[A](bnFcnt: BNode[Rdf] => A, uriFnct: URI[Rdf] => A, litFnc: Literal[Rdf] => A): A =
//			obj match
//			case bn: BNode[Rdf] =>  bnFcnt(bn)
//			case n: URI[Rdf] => uriFnct(n)
//			case lit: Literal[Rdf] => litFnc(lit)

   given Triple: operations.Triple[Rdf]

   given rTriple: operations.rTriple[Rdf]

   given Node: operations.Node[Rdf]
   given rNode: operations.rNode[Rdf]

   // todo? should a BNode be part of a Graph (or DataSet) as per Benjamin Braatz's thesis?
   given BNode: operations.BNode[Rdf]
   given bnodeTT: TypeTest[Matchable, RDF.BNode[Rdf]]

   val Literal: operations.Literal[Rdf]
   export Literal.LiteralI.*

   given operations.Literal[Rdf] = Literal

   given literalTT: TypeTest[Matchable, RDF.Literal[Rdf]]

   given rUriTT: TypeTest[Matchable, RDF.rURI[Rdf]]
   // we could type test for full uri, but that would require parsing

   given subjToURITT: TypeTest[RDF.Statement.Subject[Rdf], RDF.URI[Rdf]]
   given rSubjToURITT: TypeTest[RDF.rStatement.Subject[Rdf], RDF.rURI[Rdf]]
   given objToURITT: TypeTest[RDF.Statement.Object[Rdf], RDF.URI[Rdf]]
   given rObjToURITT: TypeTest[RDF.rStatement.Object[Rdf], RDF.rURI[Rdf]]

   given rURI: operations.rURI[Rdf]

   given URI: operations.URI[Rdf]

   given Lang: operations.Lang[Rdf]

end Ops
