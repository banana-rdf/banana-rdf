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
   import scala.language.implicitConversions
   import RDF.*
   import RDF.Statement as St

   // needed to help inferencing
   // todo: this transformation should really be automatically handled by compiler. Report back.
   implicit def lit2Node(lit: Literal[Rdf]): Node[Rdf] = lit.asInstanceOf[Node[Rdf]]
   implicit def uri2Node(uri: URI[Rdf]): Node[Rdf]     = uri.asInstanceOf[Node[Rdf]]
   implicit def bnode2Node(bn: BNode[Rdf]): Node[Rdf]  = bn.asInstanceOf[Node[Rdf]]
   implicit def uri2rUri(uri: URI[Rdf]): rURI[Rdf]     = uri.asInstanceOf[rURI[Rdf]]
   implicit def rUri2rNode(uri: rURI[Rdf]): rNode[Rdf] = uri.asInstanceOf[rNode[Rdf]]

   // conversions for position types
   implicit def obj2Node(obj: St.Object[Rdf]): Node[Rdf]  = obj.asInstanceOf[Node[Rdf]]
   implicit def sub2Node(obj: St.Subject[Rdf]): Node[Rdf] = obj.asInstanceOf[Node[Rdf]]
   // note:  if we use the conversion below, then all the code needs to import scala.language.implicitConversions
   //	given Conversion[St.Object[Rdf],RDF.Node[Rdf]] with
   //		def apply(obj: St.Object[Rdf]): RDF.Node[Rdf] =  obj.asInstanceOf[Node[Rdf]]

   // interpretation types to help consistent pattern matching across implementations
   val `*`: RDF.NodeAny[Rdf]

   given Graph: operations.Graph[Rdf]

   given basicStoreFactory: StoreFactory[Rdf]
   given Store: operations.Store[Rdf]

   val rGraph: operations.rGraph[Rdf]

   val Subject: operations.Subject[Rdf]

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
