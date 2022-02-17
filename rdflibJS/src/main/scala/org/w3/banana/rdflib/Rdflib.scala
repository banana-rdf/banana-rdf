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

package org.w3.banana.rdflib

import org.w3.banana.operations.StoreFactory
import org.w3.banana.rdflib.facade.FormulaOpts.FormulaOpts
import org.w3.banana.rdflib.facade.storeMod.IndexedFormula
import org.w3.banana.rdflib.facade.*
import org.w3.banana.{Ops, RDF, operations}
import run.cosy.rdfjs.model
import run.cosy.rdfjs.model.DataFactory

import scala.annotation.targetName
import scala.collection.mutable
import scala.util.{Success, Try}
import scala.reflect.TypeTest
import scala.scalajs.js
import scala.scalajs.js.undefined

object Rdflib extends RDF:

   override opaque type rGraph  = storeMod.IndexedFormula
   override opaque type rTriple = model.Quad
   override opaque type rURI    = model.NamedNode

   override opaque type Store                    = storeMod.IndexedFormula
   override opaque type Graph                    = storeMod.IndexedFormula
   override opaque type Triple <: Matchable      = model.Quad
   override opaque type Quad <: Matchable        = model.Quad
   override opaque type Node <: Matchable        = model.ValueTerm[?]
   override opaque type URI <: Node              = model.NamedNode
   override opaque type BNode <: Node            = model.BlankNode
   override opaque type Literal <: Node          = model.Literal
   override opaque type Lang <: Matchable        = String
   override opaque type DefaultGraphNode <: Node = model.DefaultGraph

   override type NodeAny = Null

   override type MGraph = storeMod.IndexedFormula // a mutable graph

   //	given uriTT: TypeTest[Node,URI] with {
//		override def unapply(s: Node): Option[s.type & URI] =
//			s match
//				//note: using rjIRI won't compile
//				case x: (s.type & org.eclipse.rdf4j.model.IRI) => Some(x)
//				case _ => None
//	}

//	given literalTT: TypeTest[Node,Literal] with {
//		override def unapply(s: Node): Option[s.type & Literal] =
//			s match
//				//note: this does not compile if we use URI instead of jena.Node_URI
//				case x: (s.type & org.eclipse.rdf4j.model.Literal) => Some(x)
//				case _ => None
//	}

   /** Here we build up the methods functions allowing RDF.Graph[R] notation to be used.
     *
     * This will be the same code in every singleton implementation of RDF.
     */
   given ops: Ops[R] with
      import js.JSConverters.*
      val df: DataFactory     = model.DataFactory()
      def opts(): FormulaOpts = facade.FormulaOpts().setRdfFactory(df)
      import scala.collection.mutable
      import RDF.Statement as St
      private val init                         = nodeMod.default
      val defaulGraph: RDF.DefaultGraphNode[R] = df.defaultGraph()

      val `*` : RDF.NodeAny[R] = null

      given basicStoreFactory: StoreFactory[R] with
         override def makeStore(): RDF.Store[R] =
            val fopts = org.w3.banana.rdflib.facade.FormulaOpts()
            fopts.setRdfFactory(model.DataFactory())
            org.w3.banana.rdflib.facade.storeMod(fopts)

      given Store: operations.Store[R] with
         import scala.jdk.CollectionConverters.given
         // todo: need to integrate locking functionality
         extension (store: RDF.Store[R])
            override def add(qs: RDF.Quad[R]*): store.type =
               for q <- qs do store.addStatement(q)
               store

            override def remove(qs: RDF.Quad[R]*): store.type =
               store.remove(qs.toJSArray)
               store

            override def find(
                s: St.Subject[R] | RDF.NodeAny[R],
                p: St.Relation[R] | RDF.NodeAny[R],
                o: St.Object[R] | RDF.NodeAny[R],
                g: St.Graph[R] | RDF.NodeAny[R]
            ): Iterator[RDF.Quad[R]] =
               // todo: note, we loose the try exception failure here
               val res: scala.collection.mutable.Seq[RDF.Quad[R]] =
                 store.statementsMatching(s, p, o, g, false)
               res.iterator

            override def remove(
                s: St.Subject[R] | RDF.NodeAny[R],
                p: St.Relation[R] | RDF.NodeAny[R],
                o: St.Object[R] | RDF.NodeAny[R],
                g: St.Graph[R] | RDF.NodeAny[R]
            ): store.type = store.remove(store.statementsMatching(s, p, o, g, false)).nn

            override def default: St.Graph[R] = defaulGraph

      end Store

      given Graph: operations.Graph[R] with
         def empty: RDF.Graph[R] = storeMod(opts())
         def apply(triples: Iterable[RDF.Triple[R]]): RDF.Graph[R] =
            val graph: storeMod.IndexedFormula = empty
            graph.addAll(triples.toJSArray)
            graph
         def triplesIn(graph: RDF.Graph[R]): Iterable[RDF.Triple[R]] =
            val iFrm = graph.asInstanceOf[IndexedFormula]
            iFrm.`match`(undefined, undefined, undefined, undefined).toIterable

         // note the graph size may be bigger as we are using a quad store
         def graphSize(graph: RDF.Graph[R]): Int =
           graph.length.toInt

         // If one modelled Graphs as Named Graphs, then union could just be unioning the names
         // this type of union is very inefficient
         def gunion(graphs: Seq[RDF.Graph[R]]): RDF.Graph[R] =
           graphs match
              case Seq(x) => x
              case _ =>
                val newGraph: IndexedFormula = empty
                graphs.foreach(g => g.statements.foreach(s => newGraph.addStatement(s)))
                newGraph

         def difference(g1: RDF.Graph[R], g2: RDF.Graph[R]): RDF.Graph[R] =
            val newgraph: IndexedFormula = empty
            triplesIn(g1) foreach { triple =>
              if !g2.holdsStatement(triple) then newgraph.add(triple)
            }
            newgraph

         import org.w3.banana.isomorphism.*
         private val mapGen = new SimpleMappingGenerator[R](VerticeCBuilder.simpleHash[R])
         private val iso    = new GraphIsomorphism[R](mapGen)
         def isomorphism(left: RDF.Graph[R], right: RDF.Graph[R]): Boolean =
            val a = iso.findAnswer(left, right)
            a.isSuccess

         def findTriples(
             graph: RDF.Graph[R],
             s: St.Subject[R] | RDF.NodeAny[R],
             p: St.Relation[R] | RDF.NodeAny[R],
             o: St.Object[R] | RDF.NodeAny[R]
         ): Iterator[RDF.Triple[R]] =
            val sm: mutable.Seq[RDF.Triple[R]] =
              graph.statementsMatching(s, p, o, df.defaultGraph(), false)
            sm.iterator
      end Graph

      val rGraph = new operations.rGraph[R]:
         def empty: RDF.rGraph[R] = Graph.empty
         def apply(triples: Iterable[RDF.rTriple[R]]): RDF.rGraph[R] =
           Graph(triples)
         def triplesIn(graph: RDF.rGraph[R]): Iterable[RDF.rTriple[R]] =
           Graph.triplesIn(graph)
         def graphSize(graph: RDF.rGraph[R]): Int =
           Graph.graphSize(graph).toInt
      end rGraph
//		given tripleTT: TypeTest[Matchable, RDF.Triple[R]] with {
//			override def unapply(s: Matchable): Option[s.type & Triple] =
//				s match
//					//note: this does not compile if we use URI instead of jena.Node_URI
//					case x: (s.type & Triple) => Some(x)
//					case _ => None
//		}

      val Subject = new operations.Subject[R]:
         extension (subj: RDF.Statement.Subject[R])
           def fold[A](uriFnct: RDF.URI[R] => A, bnFcnt: RDF.BNode[R] => A): A =
             subj match
                case nn: model.NamedNode    => uriFnct(nn)
                case blank: model.BlankNode => bnFcnt(blank)

      given Triple: operations.Triple[R] with
         import RDF.Statement as St
         // todo: check whether it really is not legal in rdflib to have a literal as subject
         // warning throws an exception
         def apply(s: St.Subject[R], p: St.Relation[R], o: St.Object[R]): RDF.Triple[R] =
           df.quad(s, p, o, df.defaultGraph())
         def subjectOf(t: RDF.Triple[R]): St.Subject[R]   = t.subj
         def relationOf(t: RDF.Triple[R]): St.Relation[R] = t.rel
         def objectOf(t: RDF.Triple[R]): St.Object[R]     = t.obj
      end Triple

      override val Quad = new operations.Quad[R](this):
         def apply(s: St.Subject[R], p: St.Relation[R], o: St.Object[R]): RDF.Quad[R] =
           df.quad(s, p, o, df.defaultGraph())
         def apply(
             s: St.Subject[R],
             p: St.Relation[R],
             o: St.Object[R],
             where: St.Graph[R]
         ): RDF.Quad[R] = df.quad(s, p, o, where)
         protected def subjectOf(s: RDF.Quad[R]): St.Subject[R]   = s.subj
         protected def relationOf(s: RDF.Quad[R]): St.Relation[R] = s.rel
         protected def objectOf(s: RDF.Quad[R]): St.Object[R]     = s.obj
         protected def graphOf(s: RDF.Quad[R]): St.Graph[R]       = s.graph
      end Quad

      // todo: see whether this really works! It may be that we need to create a new construct
      val rTriple = new operations.rTriple[R]:
         import RDF.rStatement as rSt
         def apply(s: rSt.Subject[R], p: rSt.Relation[R], o: rSt.Object[R]): RDF.rTriple[R] =
           Triple(s, p, o)
         def untuple(t: RDF.rTriple[R]): rTripleI =
           (subjectOf(t), relationOf(t), objectOf(t))
         def subjectOf(t: RDF.rTriple[R]): rSt.Subject[R]   = Triple.subjectOf(t)
         def relationOf(t: RDF.rTriple[R]): rSt.Relation[R] = Triple.relationOf(t)
         def objectOf(t: RDF.rTriple[R]): rSt.Object[R]     = Triple.objectOf(t)
      end rTriple

      given Node: operations.Node[R] with
         private def rl(node: RDF.Node[R]): model.Term[?] = node.asInstanceOf[model.Term[?]]
         extension (node: RDF.Node[R])
            def isURI: Boolean     = rl(node).isInstanceOf[model.NamedNode]
            def isBNode: Boolean   = rl(node).isInstanceOf[model.BlankNode]
            def isLiteral: Boolean = rl(node).isInstanceOf[model.Literal]
            // we override fold, as we can implement it faster with pattern matching
            override def fold[A](
                uriF: RDF.URI[R] => A,
                bnF: RDF.BNode[R] => A,
                litF: RDF.Literal[R] => A
            ): A = node match
               case nn: model.NamedNode    => uriF(nn)
               case blank: model.BlankNode => bnF(blank)
               case lit: model.Literal     => litF(lit)
               case _ => throw IllegalArgumentException(
                   s"node.fold() received `$node` which is neither a BNode, URI or Literal. Please report."
                 )

      given BNode: operations.BNode[R] with
         def apply(s: String): RDF.BNode[R] = df.blankNode(s)
         def apply(): RDF.BNode[R]          = df.blankNode()
         extension (bn: RDF.BNode[R])
           def label: String = bn.value
      end BNode

      given bnodeTT: TypeTest[Matchable, RDF.BNode[R]] with
         def unapply(s: Matchable): Option[s.type & RDF.BNode[R]] =
           s match
              // note: this does not compile if we use URI instead of jena.Node_URI
              case x: (s.type & run.cosy.rdfjs.model.BlankNode) => Some(x)
              case _                                            => None

      given Literal: operations.Literal[R] with
         import org.w3.banana.operations.URI.*
         private val xsdString     = df.namedNode(xsdStr).nn
         private val xsdLangString = df.namedNode(xsdLangStr).nn
         import LiteralI as Lit

         def apply(plain: String): RDF.Literal[R] = df.literal(plain)
         def apply(lit: Lit): RDF.Literal[R] = lit match
            case Lit.Plain(text)     => apply(text)
            case Lit.`@`(text, lang) => df.literal(text, lang)
            case Lit.`^^`(text, tp)  => df.literal(text, tp)

         def unapply(x: Matchable): Option[LiteralI] = x match
            case lit: model.Literal =>
              val lex: String    = lit.value
              val dt: RDF.URI[R] = lit.datatype
              val lang: String   = lit.language
              if (lang.isEmpty) then
                 // todo: this comparison could be costly, check
                 if dt == xsdString then Some(Lit.Plain(lex))
                 else Some(Lit.^^(lex, dt))
              else if dt == xsdLangString then
                 Some(Lit.`@`(lex, Lang(lang)))
              else None
            case _ => None

         @targetName("langLit")
         def apply(lex: String, lang: RDF.Lang[R]): RDF.Literal[R] = df.literal(lex, lang.label)

         @targetName("dataTypeLit")
         def apply(lex: String, dataTp: RDF.URI[R]): RDF.Literal[R] = df.literal(lex, dataTp)

         extension (lit: RDF.Literal[R])
           def text: String = lit.value
      end Literal

      override given literalTT: TypeTest[Matchable, RDF.Literal[R]] with
         override def unapply(s: Matchable): Option[s.type & RDF.Literal[R]] =
           s match
              case x: (s.type & model.Literal) => Some(x)
              case _                           => None

      given Lang: operations.Lang[R] with
         def apply(lang: String): RDF.Lang[R] = lang
         extension (lang: RDF.Lang[R])
           def label: String = lang

      val rURI = new operations.rURI[R]:
         def apply(iriStr: String): RDF.rURI[R] = df.namedNode(iriStr)
         def asString(uri: RDF.rURI[R]): String = uri.toString

      given URI: operations.URI[R] with
         // this does throw an exception on non relative URLs!
         def mkUri(iriStr: String): Try[RDF.URI[R]] =
           Try(df.namedNode(iriStr))
         def asString(uri: RDF.URI[R]): String = uri.value

//			extension(uri: RDF.URI[R])
//				def !=(other: RDF.URI[R]): Boolean = !(uri == other)
      end URI

   // mutable graphs
//	type MGraph = Model
//
//	// types for the graph traversal API
//	type NodeMatch = Value
//	type NodeAny = Null
//	type NodeConcrete = Value
//
//	// types related to Sparql
//	type Query = ParsedQuery
//	type SelectQuery = ParsedTupleQuery
//	type ConstructQuery = ParsedGraphQuery
//	type AskQuery = ParsedBooleanQuery
//
//	//FIXME Can't use ParsedUpdate because of https://openrdf.atlassian.net/browse/SES-1847
//	type UpdateQuery = Rdf4jParseUpdate
//
//	type Solution = BindingSet
//	// instead of TupleQueryResult so that it's eager instead of lazy
//	type Solutions = Vector[BindingSet]
