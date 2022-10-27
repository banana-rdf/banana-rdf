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

package org.w3.banana.jena

import org.apache.jena.datatypes.{BaseDatatype, RDFDatatype, TypeMapper}
import org.apache.jena.graph.{GraphUtil, Node_URI}
import org.apache.jena.graph.Node.ANY as JenaANY
import org.apache.jena.query.DatasetFactory
import org.apache.jena.sparql.core.DatasetGraphFactory
import org.apache.jena.sparql.graph.GraphReadOnly
import org.apache.jena.tdb.{TDB, TDBFactory}
import org.apache.jena.util.iterator.ExtendedIterator
import org.w3.banana.operations.{Quad, StoreFactory, rGraph}
import org.w3.banana.{Ops, RDF, operations}

import scala.annotation.targetName
import scala.reflect.TypeTest
import scala.util.Using.Releasable
import scala.util.{Try, Using}

object JenaRdf extends org.w3.banana.RDF:
   import org.apache.jena.graph as jenaTp
   import org.apache.jena.graph.{Factory, NodeFactory}

   override type Top = java.lang.Object

   // we hack jena.{Graph, Triple, URI} to allow relative URLs.
   override opaque type rGraph <: Top  = jenaTp.Graph
   override opaque type rTriple <: Top = jenaTp.Triple
   override opaque type rNode <: Top   = jenaTp.Node
   override opaque type rURI <: rNode  = jenaTp.Node_URI

   override opaque type Graph <: rGraph = jenaTp.Graph
   // todo: Quad missing
   override opaque type Triple <: rTriple  = jenaTp.Triple
   override opaque type Quad <: Top        = org.apache.jena.sparql.core.Quad
   override opaque type Node <: rNode      = jenaTp.Node
   override opaque type URI <: Node & rURI = jenaTp.Node_URI
   override opaque type BNode <: Node      = jenaTp.Node_Blank
   override opaque type Literal <: Node    = jenaTp.Node_Literal
   override opaque type Lang <: Top        = String
   override opaque type DefaultGraphNode   = org.apache.jena.sparql.core.Quad.defaultGraphIRI.type

   override opaque type NodeAny = Null
   override opaque type Store =
     org.apache.jena.sparql.core.DatasetGraph // a mutable dataset

   given [T]: Releasable[ExtendedIterator[T]] with
      def release(resource: ExtendedIterator[T]): Unit = resource.close()

   import RDF.Statement as St

   /** Here we build up the methods functions allowing RDF.Graph[R] notation to be used.
     *
     * This will be the same code in every singleton implementation of RDF. I did not succeed in
     * removing the duplication, as there are Match Type compilation problems. It does not work to
     * place here the implementations of rdf which can be placed above, as the RDF.Graph[R] type
     * hides the implementation type (of `graph` field for example) *
     */
   given ops: Ops[R] with

      val `*` : RDF.NodeAny[R] = null
      private val defaultGraph: RDF.URI[R] =
        org.apache.jena.sparql.core.Quad.defaultGraphIRI.asInstanceOf[URI]

      given basicStoreFactory: StoreFactory[R] with
         override def makeStore(): RDF.Store[R] = DatasetGraphFactory.createGeneral().nn

      given Store: operations.Store[R](using ops) with
         import scala.jdk.CollectionConverters.given
         // todo: need to integrate locking functionality
         extension (store: RDF.Store[R])
            override def add(qs: RDF.Quad[R]*): store.type =
               for q <- qs do store.add(q)
               store

            override def remove(qs: RDF.Quad[R]*): store.type =
               for q <- qs do store.delete(q)
               store

            override def find(
                s: St.Subject[R] | RDF.NodeAny[R],
                p: St.Relation[R] | RDF.NodeAny[R],
                o: St.Object[R] | RDF.NodeAny[R],
                g: St.Graph[R] | RDF.NodeAny[R]
            ): Iterator[RDF.Quad[R]] = store.find(g, s, p, o).nn.asScala

            override def remove(
                s: St.Subject[R] | RDF.NodeAny[R],
                p: St.Relation[R] | RDF.NodeAny[R],
                o: St.Object[R] | RDF.NodeAny[R],
                g: St.Graph[R] | RDF.NodeAny[R]
            ): store.type =
               store.deleteAny(g, s, p, o)
               store

            override def default: St.Graph[R] = defaultGraph
      end Store

      given Graph: operations.Graph[R](using ops) with
         import RDF.Statement as St
         def empty: RDF.Graph[R] = Factory.empty().nn

         def apply(triples: Iterable[RDF.Triple[R]]): RDF.Graph[R] =
            val graph: Graph = Factory.createDefaultGraph.nn
            triples.foreach { triple =>
              graph.add(triple)
            }
            new GraphReadOnly(graph)

         // note: how should one pass on the information that the Iterable is closeable?
         // https://stackoverflow.com/questions/69153609/is-there-a-cross-platform-autocloseable-iterable-
         override protected def triplesIn(graph: RDF.Graph[R]): Iterable[RDF.Triple[R]] =
            import collection.JavaConverters.asScalaIteratorConverter
            graph.find(JenaANY, JenaANY, JenaANY).nn.asScala.to(Iterable)

         override protected def graphSize(graph: RDF.Graph[R]): Int = graph.size()

         override protected def gunion(graphs: Seq[RDF.Graph[R]]): RDF.Graph[R] =
            val g = Factory.createDefaultGraph.nn
            graphs.foreach { graph =>
              Using.resource(graph.find(JenaANY, JenaANY, JenaANY).nn) { it =>
                while it.hasNext do g.add(it.next)
              }
            }
            new GraphReadOnly(g)

         override protected def difference(g1: RDF.Graph[R], g2: RDF.Graph[R]): RDF.Graph[R] =
            val g = Factory.createDefaultGraph.nn
            GraphUtil.addInto(g, g1)
            GraphUtil.delete(g, g2.find(JenaANY, JenaANY, JenaANY))
            GraphReadOnly(g)

         override protected def isomorphism(left: RDF.Graph[R], right: RDF.Graph[R]): Boolean =
           left.isIsomorphicWith(right)

         override protected def findTriples(
             graph: RDF.Graph[R],
             s: St.Subject[R] | RDF.NodeAny[R],
             p: St.Relation[R] | RDF.NodeAny[R],
             o: St.Object[R] | RDF.NodeAny[R]
         ): Iterator[RDF.Triple[R]] =
            import scala.jdk.CollectionConverters.*
            graph.find(s, p, o).nn.asScala
      end Graph

//		def tdbGraphStore(dir: String) = new operations.StoreFactory[R]:
//			override def makeStore(q: operations.Quad*): RDF.Store[R] =
//				val dataset = TDBFactory.createDataset(dir)
//				dataset.getContext().set(TDB.symUnionDefaultGraph, false)
//				dataset

      given rGraph: operations.rGraph[R] with
         def empty: RDF.rGraph[R] = Graph.empty
         def apply(triples: Iterable[RDF.rTriple[R]]): RDF.rGraph[R] =
           Graph(triples)
         extension (graph: RDF.rGraph[R])
            override def triples: Iterable[RDF.rTriple[R]] =
               import collection.JavaConverters.asScalaIteratorConverter
               graph.find(JenaANY, JenaANY, JenaANY).nn.asScala.to(Iterable)
            override def size: Int =
              graph.size()
            // note: the reason why we may want rGraphs to be based on Seq rather than graphs is
            // to be seen here. Most libs are mutable, and doing addition on these graphs
            // requiring as it does the copy of all triples is likely to be inefficient
            override infix def ++(triples: Seq[RDF.rTriple[R]]): RDF.rGraph[R] =
               val g = Factory.createDefaultGraph.nn
               Using.resource(graph.find(JenaANY, JenaANY, JenaANY).nn) { it =>
                 while it.hasNext do g.add(it.next)
               }
               triples.foreach(tr => g.add(tr))
               g
            override infix def isomorphic(other: RDF.rGraph[R]): Boolean =
              graph.isIsomorphicWith(other)
      end rGraph

      given rTriple: operations.rTriple[R](using ops) with
         import RDF.rStatement as rSt
         def apply(s: rSt.Subject[R], p: rSt.Relation[R], o: rSt.Object[R]): RDF.rTriple[R] =
           jenaTp.Triple.create(s, p, o).nn
         def untuple(t: RDF.Triple[R]): rTripleI =
           (subjectOf(t), relationOf(t), objectOf(t))
         def subjectOf(t: RDF.rTriple[R]): rSt.Subject[R] =
           t.getSubject().asInstanceOf[rSt.Subject[R]].nn
         def relationOf(t: RDF.rTriple[R]): rSt.Relation[R] =
           t.getPredicate().asInstanceOf[URI].nn
         def objectOf(t: RDF.rTriple[R]): rSt.Object[R] =
           t.getObject().asInstanceOf[rSt.Object[R]].nn
      end rTriple

      given Subject: operations.Subject[R] with
         extension (subj: RDF.Statement.Subject[R])
           def foldSubj[A](uriFnct: RDF.URI[R] => A, bnFcnt: RDF.BNode[R] => A): A =
             if subj.isBlank then
                bnFcnt(subj.asInstanceOf[jenaTp.Node_Blank])
             else
                uriFnct(subj.asInstanceOf[jenaTp.Node_URI])
      end Subject

      given Triple: operations.Triple[R] with
         import RDF.Statement as St
         def apply(s: St.Subject[R], p: St.Relation[R], o: St.Object[R]): RDF.Triple[R] =
           jenaTp.Triple.create(s, p, o).nn
         def subjectOf(t: RDF.Triple[R]): St.Subject[R] =
           t.getSubject().asInstanceOf[St.Subject[R]].nn
         def relationOf(t: RDF.Triple[R]): St.Relation[R] =
           t.getPredicate.asInstanceOf[St.Relation[R]].nn
         def objectOf(t: RDF.Triple[R]): St.Object[R] =
           t.getObject().asInstanceOf[St.Object[R]].nn

      override val Quad = new operations.Quad[R](this):
         import org.apache.jena.sparql.core.Quad as JQuad
         inline def defaultGraph: RDF.DefaultGraphNode[R] =
           org.apache.jena.sparql.core.Quad.defaultGraphIRI
         inline def apply(s: St.Subject[R], p: St.Relation[R], o: St.Object[R]): RDF.Quad[R] =
           new JQuad(defaultGraph, s, p, o)
         inline def apply(
             s: St.Subject[R],
             p: St.Relation[R],
             o: St.Object[R],
             where: St.Graph[R]
         ): RDF.Quad[R] = new JQuad(where, s, p, o)
         inline protected def subjectOf(s: RDF.Quad[R]): St.Subject[R] =
           s.getSubject().asInstanceOf[St.Subject[R]].nn
         inline protected def relationOf(s: RDF.Quad[R]): St.Relation[R] =
           s.getPredicate.asInstanceOf[St.Relation[R]].nn
         inline protected def objectOf(s: RDF.Quad[R]): St.Object[R] =
           s.getObject().asInstanceOf[St.Object[R]].nn
         inline protected def graphOf(s: RDF.Quad[R]): St.Graph[R] =
           s.getGraph().asInstanceOf[St.Graph[R]].nn
      end Quad

      given rNode: operations.rNode[R] with
         private def jn(node: RDF.rNode[R]) = node.asInstanceOf[org.apache.jena.graph.Node]

         extension (rnode: RDF.rNode[R])
            def isURI: Boolean     = jn(rnode).isURI
            def isBNode: Boolean   = jn(rnode).isBlank
            def isLiteral: Boolean = jn(rnode).isLiteral
      end rNode

      given Node: operations.Node[R] with
         private def jn(node: RDF.Node[R]) = node.asInstanceOf[org.apache.jena.graph.Node]
         extension (node: RDF.Node[R])
            def isURI: Boolean     = jn(node).isURI
            def isBNode: Boolean   = jn(node).isBlank
            def isLiteral: Boolean = jn(node).isLiteral
      end Node

      given BNode: operations.BNode[R] with
         def apply(label: String): RDF.BNode[R] =
            val id = jenaTp.BlankNodeId.create(label).nn
            NodeFactory.createBlankNode(id).asInstanceOf[jenaTp.Node_Blank]
         def apply(): RDF.BNode[R] =
           NodeFactory.createBlankNode().asInstanceOf[jenaTp.Node_Blank]

         extension (bn: RDF.BNode[R])
           def label: String = bn.getBlankNodeLabel().nn
      end BNode

      given bnodeTT: TypeTest[Matchable, RDF.BNode[R]] with
         def unapply(s: Matchable): Option[s.type & RDF.BNode[R]] =
           s match
              // note: this does not compile if we use URI instead of jenaTp.Node_URI
              case x: (s.type & jenaTp.Node_Blank) => Some(x)
              case _                               => None
      end bnodeTT

      val Literal = new operations.Literal[R]:
         import org.w3.banana.operations.URI.*
         private val xsdString: RDFDatatype     = mapper.getTypeByName(xsdStr).nn
         private val xsdLangString: RDFDatatype = mapper.getTypeByName(xsdLangStr).nn
         // todo? are we missing a Datatype Type? (check other frameworks)

         def jenaDatatype(datatype: RDF.URI[R]): RDFDatatype =
            val iriString: String       = datatype.getURI.nn
            val typ: RDFDatatype | Null = mapper.getTypeByName(iriString)
            if typ == null then
               val datatype = new BaseDatatype(iriString)
               mapper.registerDatatype(datatype)
               datatype
            else typ

         lazy val mapper: TypeMapper = TypeMapper.getInstance.nn

         override def apply(plain: String): RDF.Literal[R] =
           NodeFactory.createLiteral(plain).nn.asInstanceOf[Literal]

         override def apply(lit: LiteralI): RDF.Literal[R] = lit match
            case LiteralI.Plain(text) => NodeFactory.createLiteral(text).nn.asInstanceOf[Literal]
            case LiteralI.`@`(text, lang) => Literal(text, lang)
            case LiteralI.`^^`(text, tp)  => Literal(text, tp)

         @targetName("langLit") override def apply(lex: String, lang: RDF.Lang[R]): RDF.Literal[R] =
           NodeFactory.createLiteral(lex, lang).nn.asInstanceOf[Literal]

         @targetName("dataTypeLit") override def apply(
             lex: String,
             dataTp: RDF.URI[R]
         ): RDF.Literal[R] =
           NodeFactory.createLiteral(lex, jenaDatatype(dataTp)).nn.asInstanceOf[Literal]

         def unapply(x: Matchable): Option[LiteralI] =
           x match
              case lit: Literal =>
                val lex: String            = lit.getLiteralLexicalForm.nn
                val dt: RDFDatatype | Null = lit.getLiteralDatatype
                val lang: String | Null    = lit.getLiteralLanguage
                if (lang == null || lang.isEmpty) then
                   if dt == null || dt == xsdString then Some(LiteralI.Plain(lex))
                   else Some(LiteralI.^^(lex, URI(dt.getURI.nn)))
                else if dt == null || dt == xsdLangString then
                   Some(LiteralI.`@`(lex, Lang(lang)))
                else None
              case _ => None

         extension (lit: RDF.Literal[R])
           def text: String = lit.getLiteralLexicalForm.nn
      end Literal

      given literalTT: TypeTest[Matchable, RDF.Literal[R]] with
         override def unapply(s: Matchable): Option[s.type & jenaTp.Node_Literal] =
           s match
              // note: this does not compile if we use URI instead of jenaTp.Node_URI
              case x: (s.type & jenaTp.Node_Literal) => Some(x)
              case _                                 => None

      given Lang: operations.Lang[R] with
         def apply(lang: String): RDF.Lang[R] = lang
         extension (lang: RDF.Lang[R])
           def label: String = lang
      end Lang

      given rURI: operations.rURI[R] with
         import java.net.URI as jURI

         override protected def mkUriUnsafe(uriStr: String): RDF.rURI[R] =
           NodeFactory.createURI(uriStr).nn.asInstanceOf[Node_URI]

         override def apply(uriStr: String): RDF.rURI[R] = mkUriUnsafe(uriStr)

         override protected def stringVal(uri: RDF.rURI[R]): String = uri.getURI().nn
      end rURI

      given rUriTT: reflect.TypeTest[Matchable, org.w3.banana.RDF.rURI[R]] with
         def unapply(s: Matchable): Option[s.type & RDF.rURI[R]] =
           s match
              // note: this does not compile if we use URI instead of jenaTp.Node_URI
              case x: (s.type & jenaTp.Node_URI) => Some(x)
              case _                             => None

      given URI: operations.URI[R] with
         import java.net.URI as jURI
         override protected def stringVal(uri: RDF.URI[R]): String =
           uri.getURI().nn
         override def mkUriUnsafe(iriStr: String): RDF.URI[R] =
           NodeFactory.createURI(iriStr).asInstanceOf[URI]
      end URI

      given subjToURITT: TypeTest[RDF.Statement.Subject[R], RDF.URI[R]] with
         override def unapply(s: RDF.Statement.Subject[R]): Option[s.type & jenaTp.Node_URI] =
           s match
              case x: (s.type & jenaTp.Node_URI) => Some(x)
              case _                             => None

      given rSubjToURITT: TypeTest[RDF.rStatement.Subject[R], RDF.rURI[R]] with
         override def unapply(s: RDF.Statement.Subject[R]): Option[s.type & jenaTp.Node_URI] =
           s match
              case x: (s.type & jenaTp.Node_URI) => Some(x)
              case _                             => None

      given objToURITT: TypeTest[RDF.Statement.Object[R], RDF.URI[R]] with
         override def unapply(s: RDF.Statement.Object[R]): Option[s.type & jenaTp.Node_URI] =
           s match
              case x: (s.type & jenaTp.Node_URI) => Some(x)
              case _                             => None

      given rObjToURITT: TypeTest[RDF.rStatement.Object[R], RDF.rURI[R]] with
         override def unapply(s: RDF.rStatement.Object[R]): Option[s.type & jenaTp.Node_URI] =
           s match
              case x: (s.type & jenaTp.Node_URI) => Some(x)
              case _                             => None

end JenaRdf
