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

package org.w3.banana.rdflib.facade

import org.scalablytyped.runtime.StObject
import FormulaOpts.FormulaOpts
import run.cosy.rdfjs.model.{BlankNode, DataFactory, Quad, Term}

import scala.scalajs.js
import scala.scalajs.js.annotation.*

object formulaMod:

   def apply(quads: js.Array[Quad], opts: FormulaOpts): Formula =
     new default(quads, js.Array(), js.Array(), js.Array(), opts)

   /** Initializes this formula
     *
     * @constructor
     * @param statements
     *   \- Initial array of statements
     * @param constraints
     *   \- initial array of constraints
     * @param initBindings
     *   \- initial bindings used in Query
     * @param optional
     *   \- optional
     * @param opts
     * @param opts
     *   .rdfFactory - The rdf factory that should be used by the store
     */
   @JSImport("rdflib/lib/formula", JSImport.Default)
   @js.native
   open class default() extends Formula:
      def this(
          statements: js.Array[Quad],
          constraints: js.Array[js.Any],
          initBindings: js.Array[js.Any],
          optional: js.Array[js.Any],
          opts: FormulaOpts
      ) = this()

//	type BooleanMap = StringDictionary[Boolean]

   @js.native
   trait Formula extends StObject:
//		extends nodeInternalMod.default {

      /** Transform a collection of NTriple URIs into their URI strings
        *
        * @param t
        *   \- Some iterable collection of NTriple URI strings
        * @return
        *   A collection of the URIs as strings todo: explain why it is important to go through NT
        */
//		def NTtoURI(t: js.Any): js.Object = js.native

//		def add(
//			subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
//			predicate: Unit,
//			`object`: Unit,
//			graph: QuadGraph
//		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

      // this implementatino returns a number (but subimplementation returns a this, so we have to have both!!
      def add(subject: Quad): this.type | Double = js.native

//		def add(
//			subject: QuadSubject,
//			predicate: QuadPredicate,
//			`object`: String): Double = js.native
//
//		def add(
//			subject: QuadSubject,
//			predicate: QuadPredicate,
//			`object`: String,
//			graph: QuadGraph):  Double = js.native
//
//		def add(
//			subject: QuadSubject,
//			predicate: QuadPredicate,
//			`object`: Term[?]): Double = js.native
//
//		def add(
//			subject: QuadSubject,
//			predicate: QuadPredicate,
//			`object`: Term[?],
//			graph: QuadGraph): Double = js.native

//		def add(
//			subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
//			predicate: Unit,
//			`object`: String
//		):  Double = js.native

      /** Adds all the statements to this formula
        *
        * @param statements
        *   \- A collection of statements
        */
      def addAll(statements: js.Array[Quad]): Unit = js.native

      /** Add a statement object
        *
        * @param {Statement}
        *   statement - An existing constructed statement to add
        * @returns
        *   a new added quad or null if already in the db.
        *
        * banana simplification: rdflib.js converts any given quad to their term. We just add the
        * given quad. Null is returned if the quad was already given, to not diverge too far from
        * the API.
        */
      def addStatement(statement: Quad): Quad | Null = js.native

      /** Follow link from one node, using one wildcard, looking for one
        *
        * For example, any(me, knows, null, profile) - a person I know accoring to my profile .
        * any(me, knows, null, null) - a person I know accoring to anything in store . any(null,
        * knows, me, null) - a person who know me accoring to anything in store .
        *
        * @param s
        *   \- A node to search for as subject, or if null, a wildcard
        * @param p
        *   \- A node to search for as predicate, or if null, a wildcard
        * @param o
        *   \- A node to search for as object, or if null, a wildcard
        * @param g
        *   \- A node to search for as graph, or if null, a wildcard
        * @returns
        *   A node which match the wildcard position, or null
        */
//		def any(
//			s: js.UndefOr[QuadSubject | Null],
//			p: js.UndefOr[QuadPredicate | Null],
//			o: js.UndefOr[QuadObject | Null],
//			g: js.UndefOr[QuadGraph | Null]
//		): types.rdflib.nodeInternalMod.default | Null = js.native

      /** Gets the first JavaScript object equivalent to a node based on the specified pattern
        *
        * @param s
        *   The subject
        * @param p
        *   The predicate
        * @param o
        *   The object
        * @param g
        *   The graph that contains the statement
        */
//		def anyJS(
//			s: js.UndefOr[QuadSubject | Null],
//			p: js.UndefOr[QuadPredicate | Null],
//			o: js.UndefOr[QuadObject | Null],
//			g: js.UndefOr[QuadGraph | Null]
//		): js.Any = js.native

      /** Gets the first statement that matches the specified pattern
        */
//		def anyStatementMatching(
//			s: js.UndefOr[QuadSubject | Null],
//			p: js.UndefOr[QuadPredicate | Null],
//			o: js.UndefOr[QuadObject | Null],
//			g: js.UndefOr[QuadGraph | Null]
//		): js.UndefOr[
//			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
//		] = js.native

      /** Gets the value of a node that matches the specified pattern
        *
        * @param s
        *   The subject
        * @param p
        *   The predicate
        * @param o
        *   The object
        * @param g
        *   The graph that contains the statement
        */
//		def anyValue(
//			s: js.UndefOr[QuadSubject | Null],
//			p: js.UndefOr[QuadPredicate | Null],
//			o: js.UndefOr[QuadObject | Null],
//			g: js.UndefOr[QuadGraph | Null]
//		): String | Unit = js.native

      /** Shortcut for adding blankNodes
        *
        * @param [id]
        */
      def bnode(): BlankNode = js.native

      def bnode(id: String): BlankNode = js.native

      /** Finds the types in the list which have no *stored* subtypes These are a set of classes
        * which provide by themselves complete information -- the other classes are redundant for
        * those who know the class DAG.
        *
        * @param types
        *   A map of the types
        */
//		def bottomTypeURIs(types_ : js.Any): js.Any = js.native

      /** Creates a new collection */
//		def collection(): types.rdflib.collectionMod.default[
//			types.rdflib.nodeInternalMod.default | types.rdflib.blankNodeMod.default | Collection[js.Any] | types.rdflib.literalMod.default | types.rdflib.variableMod.default
//		] = js.native

      /** Trace statements which connect directly, or through bnodes
        *
        * @param subject
        *   \- The node to start looking for statments
        * @param doc
        *   \- The document to be searched, or null to search all documents
        * @returns
        *   an array of statements, duplicate statements are suppresssed.
        */
//		def connectedStatements(subject: QuadSubject, doc: QuadGraph): js.Array[
//			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
//		] = js.native

//		def connectedStatements(subject: QuadSubject, doc: QuadGraph, excludePredicateURIs: js.Array[String]): js.Array[
//			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
//		] = js.native

//		var constraints: js.Array[js.Any] = js.native

      /** Follow links from one node, using one wildcard.
        *
        * For example, each(me, knows, null, profile) - people I know accoring to my profile .
        * each(me, knows, null, null) - people I know accoring to anything in store . each(null,
        * knows, me, null) - people who know me accoring to anything in store .
        *
        * @param s
        *   \- A node to search for as subject, or if null, a wildcard
        * @param p
        *   \- A node to search for as predicate, or if null, a wildcard
        * @param o
        *   \- A node to search for as object, or if null, a wildcard
        * @param g
        *   \- A node to search for as graph, or if null, a wildcard
        * @returns
        *   {Array<Node>} - An array of nodes which match the wildcard position
        */
      def each(
          s: js.UndefOr[Quad.Subject],
          p: js.UndefOr[Quad.Predicate],
          o: js.UndefOr[Quad.Object],
          g: js.UndefOr[Quad.Graph]
      ): js.Array[Quad] = js.native

      /** Test whether this formula is equals to {other}
        *
        * @param other
        *   \- The other formula
        */
      def equals(other: Formula): Boolean = js.native

      /** The accompanying fetcher instance.
        *
        * Is set by the fetcher when initialized.
        */
//		var fetcher: js.UndefOr[types.rdflib.fetcherMod.default] = js.native

      /** For thisClass or any subclass, anything which has it is its type or is the object of
        * something which has the type as its range, or subject of something which has the type as
        * its domain We don't bother doing subproperty (yet?)as it doesn't seeem to be used much.
        * Get all the Classes of which we can RDFS-infer the subject is a member
        *
        * @param subject
        *   \- A named node
        */
//		def findMemberURIs(subject: types.rdflib.nodeInternalMod.default): UriMap = js.native

      /** For thisClass or any subclass, anything which has it is its type or is the object of
        * something which has the type as its range, or subject of something which has the type as
        * its domain We don't bother doing subproperty (yet?)as it doesn't seeem to be used much.
        * Get all the Classes of which we can RDFS-infer the subject is a member
        *
        * @return
        *   a hash of URIs
        */
//		def findMembersNT(thisClass: js.Any): MembersMap = js.native

      /** Get all the Classes of which we can RDFS-infer the subject is a superclass Returns a hash
        * table where key is NT of type and value is statement why we think so. Does NOT return
        * terms, returns URI strings. We use NT representations in this version because they handle
        * blank nodes.
        */
//		def findSubClassesNT(subject: types.rdflib.nodeInternalMod.default): StringDictionary[Boolean] = js.native

      /** Get all the Classes of which we can RDFS-infer the subject is a subclass
        *
        * @param {RDFlibNamedNode}
        *   subject - The thing whose classes are to be found
        * @returns
        *   a hash table where key is NT of type and value is statement why we think so. Does NOT
        *   return terms, returns URI strings. We use NT representations in this version because
        *   they handle blank nodes.
        */
//		def findSuperClassesNT(subject: types.rdflib.nodeInternalMod.default): StringDictionary[Boolean] = js.native

      /** Get all the Classes of which we can RDFS-infer the subject is a member todo: This will
        * loop is there is a class subclass loop (Sublass loops are not illegal) Returns a hash
        * table where key is NT of type and value is statement why we think so. Does NOT return
        * terms, returns URI strings. We use NT representations in this version because they handle
        * blank nodes.
        *
        * @param subject
        *   \- A subject node
        */
//		def findTypeURIs(subject: QuadSubject): UriMap = js.native

      /** Get all the Classes of which we can RDFS-infer the subject is a member todo: This will
        * loop is there is a class subclass loop (Sublass loops are not illegal)
        *
        * @param {RDFlibNamedNode}
        *   subject - The thing whose classes are to be found
        * @returns
        *   a hash table where key is NT of type and value is statement why we think so. Does NOT
        *   return terms, returns URI strings. We use NT representations in this version because
        *   they handle blank nodes.
        */
//		def findTypesNT(subject: js.Any): StringDictionary[Boolean] = js.native

      /** Creates a new empty formula
        *
        * @param _features
        *   \- Not applicable, but necessary for typing to pass
        */
//		def formula(): Formula = js.native

//		def formula(_features: js.Array[String]): Formula = js.native

      /** Transforms an NTriples string format into a Node. The blank node bit should not be used on
        * program-external values; designed for internal work such as storing a blank node id in an
        * HTML attribute. This will only parse the strings generated by the various toNT() methods.
        */
//		def fromNT(str: js.Any): js.Any = js.native

      /** Returns true if this formula holds the specified statement(s) */
//		def holds(s: js.Any): Boolean = js.native
//
//		def holds(s: js.Any, p: js.Any): Boolean = js.native
//
//		def holds(s: js.Any, p: js.Any, o: js.Any): Boolean = js.native
//
//		def holds(s: js.Any, p: js.Any, o: js.Any, g: js.Any): Boolean = js.native
//
//		def holds(s: js.Any, p: js.Any, o: Unit, g: js.Any): Boolean = js.native
//
//		def holds(s: js.Any, p: Unit, o: js.Any): Boolean = js.native
//
//		def holds(s: js.Any, p: Unit, o: js.Any, g: js.Any): Boolean = js.native
//
//		def holds(s: js.Any, p: Unit, o: Unit, g: js.Any): Boolean = js.native
//
//		def holds(s: js.Array[js.Any]): Boolean = js.native
//
//		def holds(s: js.Array[js.Any], p: js.Any): Boolean = js.native
//
//		def holds(s: js.Array[js.Any], p: js.Any, o: js.Any): Boolean = js.native
//
//		def holds(s: js.Array[js.Any], p: js.Any, o: js.Any, g: js.Any): Boolean = js.native
//
//		def holds(s: js.Array[js.Any], p: js.Any, o: Unit, g: js.Any): Boolean = js.native
//
//		def holds(s: js.Array[js.Any], p: Unit, o: js.Any): Boolean = js.native
//
//		def holds(s: js.Array[js.Any], p: Unit, o: js.Any, g: js.Any): Boolean = js.native
//
//		def holds(s: js.Array[js.Any], p: Unit, o: Unit, g: js.Any): Boolean = js.native

      /** Returns true if this formula holds the specified {statement}
        */
      def holdsStatement(statement: Quad): Boolean = js.native

      type Indexable = Long | String

      /** Returns a unique index-safe identifier for the given term. Note: rdflib.js returns an
        * Indexable, but we are working with strings so we narrow it down. It would actually be
        * better to work with numbers as comparisons there can be made in one step. todo: later move
        * over to using numbers
        */
      def id(term: Term[?]): String = js.native
//
//		var initBindings: js.Array[js.Any] = js.native
//
//		var isVar: Double = js.native

      /** Used by the n3parser to generate list elements
        *
        * @param values
        *   \- The values of the collection
        * @param context
        *   \- The store
        * @return
        *   {BlankNode|Collection} - The term for the statement
        */
      def list(values: js.Any, context: js.Any): js.Any = js.native

      /** A namespace for the specified namespace's URI
        *
        * @param nsuri
        *   The URI for the namespace
        */
//		def ns(nsuri: String): js.Function1[ /* ln */ String, NamedNode] = js.native
//
//		def ns(nsuri: String, factory: RdfJsDataFactory): js.Function1[ /* ln */ String, NamedNode] = js.native

      /** A namespace for the specified namespace's URI
        *
        * @param nsuri
        *   The URI for the namespace
        */
//		@JSName("ns")
//		var ns_Original: js.Function2[
//			/* nsuri */ String,
//			/* factory */ js.UndefOr[RdfJsDataFactory],
//			js.Function1[ /* ln */ String, NamedNode]
//		] = js.native
//
//		var optional: js.Array[js.Any] = js.native

      /** The factory used to generate statements and terms */
      var rdfFactory: DataFactory = js.native

      /** Serializes this formula
        *
        * @param base
        *   \- The base string
        * @param contentType
        *   \- The content type of the syntax to use
        * @param provenance
        *   \- The provenance URI
        */
//		def serialize(base: js.Any, contentType: js.Any, provenance: js.Any): js.Any = js.native

      var statements: js.Array[Quad] = js.native

      /** Search the Store This is really a teaching method as to do this properly you would use
        * IndexedFormula
        *
        * @param s
        *   \- A node to search for as subject, or if null, a wildcard
        * @param p
        *   \- A node to search for as predicate, or if null, a wildcard
        * @param o
        *   \- A node to search for as object, or if null, a wildcard
        * @param g
        *   \- A node to search for as graph, or if null, a wildcard
        * @param justOne
        *   \- flag - stop when found one rather than get all of them?
        * @returns
        *   {Array<Node>} - An array of nodes which match the wildcard position
        */
//		def statementsMatching(
//			s: js.UndefOr[Quad.Subject | Null],
//			p: js.UndefOr[Quad.Predicate | Null],
//			o: js.UndefOr[Quad.Object | Null],
//			g: js.UndefOr[Quad.Graph | Null],
//			justOne: js.UndefOr[Boolean] = false
//		): js.Array[Quad] = js.native

//		def sym(uri: String): types.rdflib.namedNodeMod.default = js.native
//
//		def sym(uri: String, name: js.Any): types.rdflib.namedNodeMod.default = js.native

//		@JSName("termType")
//		var termType_Formula: Graph = js.native

/** Gets the node matching the specified pattern. Throws when no match could be made.
  *
  * @param s
  *   \- The subject
  * @param p
  *   \- The predicate
  * @param o
  *   \- The object
  * @param g
  *   \- The graph that contains the statement
  */
//		def the(
//			s: js.UndefOr[QuadSubject | Null],
//			p: js.UndefOr[QuadPredicate | Null],
//			o: js.UndefOr[QuadObject | Null],
//			g: js.UndefOr[QuadGraph | Null]
//		): js.UndefOr[types.rdflib.nodeInternalMod.default | Null] = js.native

/** Finds the types in the list which have no *stored* supertypes We exclude the universal class,
  * owl:Things and rdf:Resource, as it is information-free.
  *
  * @param types
  *   \- The types
  */
//		def topTypeURIs(types_ : StringDictionary[String | types.rdflib.namedNodeMod.default]): StringDictionary[String | types.rdflib.namedNodeMod.default] = js.native

/** RDFS Inference These are hand-written implementations of a backward-chaining reasoner over the
  * RDFS axioms.
  *
  * @param seeds
  *   \- A hash of NTs of classes to start with
  * @param predicate
  *   \- The property to trace though
  * @param inverse
  *   \- Trace inverse direction
  */
//		def transitiveClosure(seeds: BooleanMap, predicate: QuadPredicate): StringDictionary[Boolean] = js.native

//		def transitiveClosure(
//			seeds: BooleanMap,
//			predicate: QuadPredicate,
//			inverse: Boolean): StringDictionary[Boolean] = js.native

/** Gets a new variable
  *
  * @param name
  *   \- The variable's name
  */
//		def variable(name: String): types.rdflib.variableMod.default = js.native

/** Gets the number of statements in this formula that matches the specified pattern
  *
  * @param s
  *   \- The subject
  * @param p
  *   \- The predicate
  * @param o
  *   \- The object
  * @param g
  *   \- The graph that contains the statement
  */
//		def whether(
//			s: js.UndefOr[QuadSubject | Null],
//			p: js.UndefOr[QuadPredicate | Null],
//			o: js.UndefOr[QuadObject | Null],
//			g: js.UndefOr[QuadGraph | Null]
//		): Double = js.native

//	type MembersMap = StringDictionary[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]]

//	type UriMap = StringDictionary[String]
