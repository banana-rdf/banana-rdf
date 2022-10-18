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

import org.w3.banana.RDF.NodeAny

import scala.annotation.targetName
import scala.reflect.TypeTest
import scala.util.Try

/** Main RDF types. Implementations will mostly use opaque types, so we need to provide the
  * operations too. todo: how can one modularise this, while taking into account that
  * implementations will be using opaque types?
  *
  * rURI, rTriple, rGraph are all possibly relative versions of respectively a URI, Triple and
  * Graph. Relative Graphs have fewer applicable methods: they cannot be unioned with another graph
  * for example. If one had a type RelURI which was definitely a relative URI, then type rURI =
  * RelURI | URI It would be too costly with the current implementations to have a RelURI type, as
  * none of the major frameworks have such a type, and so testing for it would require parsing each
  * URL, in order in most of those frameworks to immediately loose that information. We are just
  * concerned here to stop certain operations being possible (such as graph union) so as to avoid a
  * whole space of programming errors.
  */
trait RDF:
   rdf =>
   type R = rdf.type

   type Top <: Matchable

   type rGraph <: Top  // graphs  with triples that can contain relative URLs
   type rTriple <: Top // triples that can contain relative URLs
   type rNode <: Top   // relative node
   type rURI <: rNode  // possibly relative URIs (see rUri trait for details)

   /** we may want to add the following Url and relUrl types
     * {{{
     * type Url <: URI // absolute URL
     * type relUrl <: rURI // relative URL.
     * }}}
     * where we would have something like
     * {{{
     *   type rURL = relUrl | Url
     *   type rURI = rURL | URN
     * }}}
     * It would require extending the URI classes of RDF types (because of the `<:` ) Would that be
     * possible?
     *   - Jena: it is possible to extend Jena' Node_URI
     *   - rdf4j: there are classes and interfaces that could be extended
     *   - rdflibJS: is built on Named Node class from
     *     [[https://rdf.js.org/data-model-spec/ rdf/js dataModel]], which makes no requirements on
     *     the node (Though the way it is used, we infer it has to correspond to absolute URLs, or
     *     names in a closed environment). It could be extended potentially with termSubType.
     *
     * But even when the framework (Jena,...) had a URI that was a Url, it would not make
     * downcasting automatic, as the parsers and tools of those frameworks have not idea of the Url
     * structure and so would not construct urls and abstract them. This is something
     * [[https://www.optics.dev/Monocle/docs/optics/prism Prisms]] could do. But perhaps also they
     * could simply have the same `.equals`...
     */
   type Graph <: rGraph // ordinary RDF graphs with no triples with relative URLs
   type Quad <: Top
   type Triple <: rTriple // triples with no relative URLs
   type Node <: rNode
   type URI <: Node & rURI
   type BNode <: Node
   type Literal <: Node
   // type GraphNode <- todo Jena has a GraphNode for formulas
   type Lang <: Matchable
   type DefaultGraphNode

   type MGraph // a mutable graph
   type DataSet

   // Stores are complicated enough  that it is not clear that they
   // need to be represented here. They tend to be very heavy objects,
   // so that wrapping them in another object is quasi free.
   type Store // a mutable dataset

   // types for the graph traversal API
   type NodeAny

   given ops: Ops[R]

end RDF

/** The idea of using match types by @neko-kai https://github.com/lampepfl/dotty/issues/13416
  */
object RDF:

   type rTriple[R <: RDF] = R match
      case GetRelTriple[t] => t

   type Triple[R <: RDF] <: Matchable = R match
      case GetTriple[t] => t

   // Quad is a good short name for Statement, but does not give a good understaning of it
   type Quad[R <: RDF] <: Matchable = R match
      case GetQuad[t] => t

   type rNode[R <: RDF] <: Matchable = // rURI[R] | BNode[R] | Literal[R]
     R match
        case GetRelNode[n] => n

   type Node[R <: RDF] = // URI[R] | BNode[R] | Literal[R]
     R match
        case GetNode[n] => n

   type BNode[R <: RDF] <: Node[R] = R match
      case GetBNode[R, bn] => bn

   type DefaultGraphNode[R <: RDF] = R match
      case GetDefaultGraphNode[n] => n

   type rURI[R <: RDF] <: rNode[R] = R match
      case GetRelURI[R, ru] => ru

   type URI[R <: RDF] <: (Node[R] & rURI[R]) = R match
      case GetURI[R, u] => u

   type rGraph[R <: RDF] = R match
      case GetRelGraph[g] => g

   type Graph[R <: RDF] = R match
      case GetGraph[g] => g

   type Store[R <: RDF] = R match
      case GetStore[s] => s

   type Literal[R <: RDF] <: Node[R] = R match
      case GetLiteral[R, l] => l

   type Lang[R <: RDF] <: Matchable = R match
      case GetLang[l] => l

   type NodeAny[R <: RDF] = R match
      case GetNodeAny[m] => m

   private type GetRelURI[R <: RDF, U <: rNode[R]]         = RDF { type rURI = U }
   private type GetURI[R <: RDF, U <: (Node[R] & rURI[R])] = RDF { type URI = U }
   private type GetRelNode[N <: Matchable]                 = RDF { type rNode = N }
   private type GetNode[N]                                 = RDF { type Node = N }
   private type GetBNode[R <: RDF, N <: Node[R]]           = RDF { type BNode = N }
   private type GetLiteral[R <: RDF, L <: Node[R]]         = RDF { type Literal = L }
   private type GetDefaultGraphNode[N <: Matchable]        = RDF { type DefaultGraphNode = N }
   private type GetLang[L <: Matchable]                    = RDF { type Lang = L }
   private type GetRelTriple[T]                            = RDF { type rTriple = T }
   private type GetTriple[T <: Matchable]                  = RDF { type Triple = T }
   private type GetQuad[T <: Matchable]                    = RDF { type Quad = T }
   private type GetRelGraph[G]                             = RDF { type rGraph = G }
   private type GetGraph[G <: Matchable]                   = RDF { type Graph = G }
   private type GetStore[S]                                = RDF { type Store = S }
   private type GetNodeAny[M]                              = RDF { type NodeAny = M }

   /** these associate a type to the positions in statements (triples or quads) These are not agreed
     * to by all frameworks, so it would be useful to find a way to parametrise them. Essentially
     * some (Jena?) allow a literal in Subject position (which is useful for reasoning and later n3)
     * and others are stricter, which makes them map better to many syntaxes, except N3.
     *
     * For the moment I will try the strict mode.
     */
   object Statement:
      type DT[A, B, C, R <: RDF, Object[R]] = Object[R] match
         case URI[R]     => A
         case BNode[R]   => B
         case Literal[R] => C

      type Subject[R <: RDF]  = URI[R] | BNode[R]
      type Relation[R <: RDF] = URI[R]
      type Object[R <: RDF]   = URI[R] | BNode[R] | Literal[R]
      type Graph[R <: RDF]    = URI[R] | BNode[R] | DefaultGraphNode[R]
   end Statement

   // relative Statements
   object rStatement:
      type Subject[R <: RDF]  = rURI[R] | BNode[R]
      type Relation[R <: RDF] = rURI[R]
      type Object[R <: RDF]   = rURI[R] | BNode[R] | Literal[R]
      type Graph[R <: RDF]    = rURI[R] | BNode[R] | DefaultGraphNode[R]
   end rStatement

end RDF
