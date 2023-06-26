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

import org.scalablytyped.runtime.StringDictionary
import FormulaOpts.FormulaOpts
import run.cosy.rdfjs.model.*
import run.cosy.rdfjs.model.Quad.{Graph, Predicate, Subject}

import scala.scalajs.js
import scala.scalajs.js.ThisFunction4
import scala.scalajs.js.annotation.JSImport

type Feature = "sameAs" | "InverseFunctionalProperty" | "FunctionalProperty"
type FeaturesType = js.UndefOr[js.Array[Feature]]

object StoreReplacementMethods:
   import storeMod.IndexedFormula
   type Index = js.Dictionary[js.Array[Quad]]

   val addStatement: js.ThisFunction1[IndexedFormula, Quad, Quad | Null] =
     (thisFrmla: IndexedFormula, quad: Quad) =>
        val predHash = thisFrmla.rdfFactory.id(quad.rel)
        import quad.*
//todo: We need to upgrade the library, so no need to spend too much time getting actions to work now.
//        val actions: js.Array[ThisFunction4[thisFrmla.type, Subject, Predicate, Quad.Object, Graph, Boolean]]  =
//          thisFrmla.propertyActions.get(predHash).getOrElse(js.Array())
//        for act <- actions do
//           act(thisFrmla, subj, rel, obj, graph)
        // note: the implementation there says it is inefficient but there is none using indexes provider
        if thisFrmla.statementsMatching(subj, rel, obj, graph, true).length > 0 then null
        else
           val hash: js.Array[String] = js.Array(
             thisFrmla.id(subj),
             predHash,
             thisFrmla.id(obj),
             thisFrmla.id(graph)
           )
           val indexArr = thisFrmla.index.asInstanceOf[js.Array[Index]]
           hash.zip(indexArr).foreach { case (h, ix) =>
//					println(s"adding quad $h to index $ix)")
             val quads: js.Array[Quad] = ix.getOrElseUpdate(h, js.Array[Quad]())
             quads.push(quad)
           }
           // would be faster with a hash map!!
           thisFrmla.statements.push(quad)
           for cb <- thisFrmla.dataCallbacks.getOrElse(js.Array()) do cb(quad)
           quad

   val removeStatement: js.ThisFunction1[IndexedFormula, Quad, IndexedFormula] =
     (thisFrmla: IndexedFormula, quad: Quad) =>
        import quad.*
        js.Array[Term[?]](subj, rel, obj, graph).zip(thisFrmla.index).foreach { case (t, ix) =>
          ix.get(thisFrmla.id(t)).map(_.subtractOne(quad))
        }
        thisFrmla.statements.subtractOne(quad)
        thisFrmla

   val add: js.ThisFunction4[
     IndexedFormula,
     Quad.Subject | Quad | js.Array[Quad],
     js.UndefOr[Quad.Predicate],
     js.UndefOr[Quad.Object],
     js.UndefOr[Quad.Graph],
     IndexedFormula | Quad | Null
   ] =
     (
         thisArg: IndexedFormula,
         arg1: Quad.Subject | Quad | js.Array[Quad],
         arg2: js.UndefOr[Quad.Predicate],
         arg3: js.UndefOr[Quad.Object],
         arg4: js.UndefOr[Quad.Graph]
     ) =>
       arg1 match
        case qs: js.Array[Quad] =>
          for q <- qs do thisArg.addStatement(q)
          thisArg
        case q: Quad =>
          thisArg.addStatement(q)
          thisArg
        case subj: Quad.Subject if arg2.isDefined && arg3.isDefined =>
          val q = thisArg.rdfFactory.quad(
            subj,
            arg2.get,
            arg3.get,
            arg4.getOrElse(thisArg.rdfFactory.defaultGraph())
          )
          thisArg.addStatement(q)
        case _ => throw new IllegalArgumentException(
            s"IndexedFormula.add($arg1,$arg2,$arg3,$arg4) has wrong arguments"
          )

   // the rdflib code returns a Node which is a Term with extra methods, and also does something looking at redirects
   val canon: js.ThisFunction1[IndexedFormula, js.UndefOr[Term[?]], js.UndefOr[Term[?]]] =
     (ixf: IndexedFormula, term: js.UndefOr[Term[?]]) =>
       // todo: add redirections code
       term

   val statementsMatching: js.ThisFunction5[
     IndexedFormula,
     js.UndefOr[Quad.Subject | Null],
     js.UndefOr[Quad.Predicate | Null],
     js.UndefOr[Quad.Object | Null],
     js.UndefOr[Quad.Graph | Null],
     js.UndefOr[Boolean],
     js.Array[Quad]
   ] = (
       thisArg: IndexedFormula,
       s: js.UndefOr[Quad.Subject | Null],
       p: js.UndefOr[Quad.Predicate | Null],
       o: js.UndefOr[Quad.Object | Null],
       g: js.UndefOr[Quad.Graph | Null],
       justOne: js.UndefOr[Boolean]
   ) =>
      import scalajs.js.JSConverters.*
      val givenIndx: js.Array[(Int, Term[?])] = js.Array(s, p, o, g).zipWithIndex.collect {
        case (undefOr: js.UndefOr[?], i) if undefOr.isDefined =>
          (i, undefOr.get.asInstanceOf[Term[?]])
      }
      val result: js.Array[Quad] =
        if givenIndx.size == 0 then thisArg.statements
        // case 1 => thisArg.index(givenIndx.head._1).get(givenIndx.head._2.toString).getOrElse(js.Array())
        else
           val r: js.Array[js.Array[Quad]] = givenIndx.map { (i, term) =>
             thisArg.index(i).get(term.toString).getOrElse(js.Array())
           }
           val sorted = r.sort((a1, a2) => a1.size - a2.size)
           // todo: clearly having the indexes be Sets would be of great benefit
           sorted.tail.foldRight(sorted.head.toSet) { case (quads, smallestSet) =>
             smallestSet.intersect(quads.toSet)
           }.toJSArray
      if justOne.getOrElse(false) then result.slice(0, 1)
      else result

   val matchFnct: js.ThisFunction4[
     IndexedFormula,
     js.UndefOr[Quad.Subject],
     js.UndefOr[Quad.Predicate],
     js.UndefOr[Quad.Object],
     js.UndefOr[Quad.Graph],
     js.Array[Quad]
   ] = (
       thisObj: IndexedFormula,
       s: js.UndefOr[Quad.Subject],
       p: js.UndefOr[Quad.Predicate],
       o: js.UndefOr[Quad.Object],
       g: js.UndefOr[Quad.Graph]
   ) =>
     thisObj.statementsMatching(
       s.getOrElse(null),
       p.getOrElse(null),
       o.getOrElse(null),
       g.getOrElse(null),
       false
     )
   val id: js.ThisFunction1[IndexedFormula, Term[?], String] =
     (thisArg: IndexedFormula, t: Term[?]) => thisArg.rdfFactory.id(t)
//   val handleRDF
end StoreReplacementMethods

object storeMod:
   export Quad.*
   val replace = StoreReplacementMethods
   type Index = js.Dictionary[js.Array[Quad]]

   def apply(opts: FormulaOpts): IndexedFormula =
      val ixf = default(js.Array(), opts)
      ixf.asInstanceOf[js.Dynamic].updateDynamic("id")(replace.id)
      ixf.asInstanceOf[js.Dynamic].updateDynamic("add")(replace.add)
      ixf.asInstanceOf[js.Dynamic].updateDynamic("addStatement")(replace.addStatement)
      ixf.asInstanceOf[js.Dynamic].updateDynamic("canon")(replace.canon)
      ixf.asInstanceOf[js.Dynamic].updateDynamic("match")(replace.matchFnct)
      ixf.asInstanceOf[js.Dynamic].updateDynamic("statementsMatching")(replace.statementsMatching)
      ixf.asInstanceOf[js.Dynamic].updateDynamic("removeStatement")(replace.removeStatement)
      ixf
   end apply

//		def add(
//			subj: QuadSubject | Quad | js.Array[Quad],
//			pred: js.UndefOr[QuadPredicate],
//			obj: js.UndefOr[Term | String],
//			why: js.UndefOr[QuadGraph]
//		): this.type | Double = ???

   /** Creates a new formula
     *
     * @param features
     *   \- What sort of automatic processing to do? Array of string
     * @param features
     *   .sameAs - Smush together A and B nodes whenever { A sameAs B }
     * @param opts
     * @param [opts.rdfFactory]
     *   \- The data factory that should be used by the store
     * @param [opts.rdfArrayRemove]
     *   \- Function which removes statements from the store
     * @param [opts.dataCallback]
     *   \- Callback when a statement is added to the store, will not trigger when adding duplicates
     */
   @JSImport("rdflib/lib/store", JSImport.Default)
   @js.native
   open class default() extends IndexedFormula:
      def this(features: FeaturesType) = this()

//		def this(features: Unit, opts: FormulaOpts) = this()

      def this(features: FeaturesType, opts: FormulaOpts) = this()

   /* static members */
   object default:

      @JSImport("rdflib/lib/store", JSImport.Default)
      @js.native
      val ^ : js.Any = js.native

      @JSImport("rdflib/lib/store", "default.handleRDFType")
      @js.native
      def handleRDFType: js.Function = js.native

      inline def handleRDFType_=(x: js.Function): Unit =
        ^.asInstanceOf[js.Dynamic].updateDynamic("handleRDFType")(x.asInstanceOf[js.Any])

   @JSImport("rdflib/lib/store", "defaultGraphURI")
   @js.native
   val defaultGraphURI: /* "chrome:theSession" */ String = js.native

   type FeaturesType = js.UndefOr[js.Array[Feature]]

   @js.native
   trait IndexedFormula extends formulaMod.default:

      //		/** Redirections we got from HTTP */
      //		var HTTPRedirects: js.Array[
      //			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
      //		] = js.native
      //
      //		var _existentialVariables: js.UndefOr[js.Array[types.rdflib.blankNodeMod.default]] = js.native
      //
      //		var _universalVariables: js.UndefOr[js.Array[types.rdflib.namedNodeMod.default]] = js.native

      /** Adds a triple (quad) to the store.
        *
        * @param subj
        *   \- The thing about which the fact a relationship is asserted. Also accepts a statement
        *   or an array of Statements.
        * @param pred
        *   \- The relationship which is asserted
        * @param obj
        *   \- The object of the relationship, e.g. another thing or a value. If passed a string,
        *   this will become a literal.
        * @param why
        *   \- The document in which the triple (S,P,O) was or will be stored on the web
        * @returns
        *   The statement added to the store, or the store
        */
      def add(
          subj: Quad.Subject,
          pred: Quad.Predicate,
          obj: Quad.Object | String
      ): Quad = js.native

//		def add(
//			subj: QuadSubject,
//			pred: QuadPredicate,
//			obj: QuadObject | String,
//			why: js.UndefOr[QuadGraph]
//		): Quad = js.native

      override def add(quad: Quad): this.type = js.native

      override def addAll(quad: js.Array[Quad]): Unit = js.native

      /** Add a callback which will be triggered after a statement has been added to the store.
        *
        * @param cb
        */
      //		def addDataCallback(cb: js.Function1[/* q */ Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph], Unit]): Unit = js.native

      /** Reverse mapping to redirection: aliases for this */
      //		var aliases: js.Array[js.Any] = js.native

      /** Return all equivalent URIs by which this is known
        *
        * @param x
        *   A named node
        */
      //		def allAliases(x: types.rdflib.namedNodeMod.default): js.Array[types.rdflib.namedNodeMod.default] = js.native

      /** Apply a set of statements to be deleted and to be inserted
        *
        * @param patch
        *   \- The set of statements to be deleted and to be inserted
        * @param target
        *   \- The name of the document to patch
        * @param patchCallback
        *   \- Callback to be called when patching is complete
        */
      //		def applyPatch(
      //			patch: Delete,
      //			target: NamedNode,
      //			patchCallback: js.Function1[/* errorString */ js.UndefOr[String], Unit]
      //		): Unit = js.native

      /** Returns the symbol with canonical URI as smushed
        *
        * @param term
        *   \- An RDF node
        */
      //		def canon(): types.rdflib.nodeMod.default = js.native
      //		def canon(term: Term): types.rdflib.nodeMod.default = js.native

      /** Checks this formula for consistency */
      //		def check(): Unit = js.native

      /** Checks a list of statements for consistency
        *
        * @param sts
        *   \- The list of statements to check
        * @param from
        *   \- An index with the array ['subject', 'predicate', 'object', 'why']
        */
      //		def checkStatementList(sts: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]]): Boolean | Unit = js.native
      //		def checkStatementList(sts: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]], from: Double): Boolean | Unit = js.native

      /** Map of iri predicates to functions to call when adding { s type X } */
      var classActions: StringDictionary[js.Array[js.Function]] = js.native

      /** Closes this formula (and return it) */
      // underlying rdflib.js is a noop
//		def close(): IndexedFormula = js.native

//		def compareTerms(u1: Term, u2: Term): Double = js.native

      /** replaces @template with @target and add appropriate triples removes no triples by default
        * and is a one-direction replication
        *
        * @param template
        *   node to copy
        * @param target
        *   node to copy to
        * @param flags
        *   Whether or not to do a two-directional copy and/or delete triples
        */
      //		def copyTo(template: QuadSubject, target: QuadSubject): Unit = js.native
      //		def copyTo(template: QuadSubject, target: QuadSubject, flags: js.Array[`two-direction` | delete_]): Unit = js.native

      /** Callbacks which are triggered after a statement has been added to the store */
      /* private */
      var dataCallbacks: js.UndefOr[js.Array[js.Function1[Quad, Unit]]] = js.native

      /** N3 allows for declaring blank nodes, this function enables that support
        *
        * @param x
        *   The blank node to be declared, supported in N3
        */
      //		def declareExistential(x: types.rdflib.blankNodeMod.default): types.rdflib.blankNodeMod.default = js.native

      /** Simplify graph in store when we realize two identifiers are equivalent We replace the
        * bigger with the smaller.
        *
        * @param u1in
        *   The first node
        * @param u2in
        *   The second node
        */
//		def equate(u1in: Term, u2in: Term): Boolean = js.native

      var features: FeaturesType = js.native

      /** Creates a new empty indexed formula Only applicable for IndexedFormula, but TypeScript
        * won't allow a subclass to override a property
        *
        * @param features
        *   The list of features
        */
      //		def formula(features: FeaturesType): IndexedFormula = js.native
      // An index maps a string representation of a node to the sets of quads that contain that in that position
      // an Array of Indexes exactly 4 long
      var index: js.Array[Index] = js.native

      /** @param features */
      //		def initPropertyActions(features: FeaturesType): Unit = js.native

      /** Returns the number of statements contained in this IndexedFormula. (Getter proxy to
        * this.statements). Usage:
        * ```
        * var kb = rdf.graph()
        * kb.length  // -> 0
        * ```
        * @returns
        *   {Number}
        */
      def length: Double = js.native

      /** Returns any quads matching the given arguments. Standard RDFJS spec method for Source
        * objects, implemented as an alias to `statementsMatching()`
        *
        * @param subject
        *   The subject
        * @param predicate
        *   The predicate
        * @param object
        *   The object
        * @param graph
        *   The graph that contains the statement
        */
      def `match`(
          subject: js.UndefOr[Quad.Subject],
          predicate: js.UndefOr[Quad.Predicate],
          `object`: js.UndefOr[Quad.Object],
          graph: js.UndefOr[Quad.Graph]
      ): js.Array[Quad] = js.native

      // the superclass has a bug at this point
      def statementsMatching(
          s: js.UndefOr[Quad.Subject | Null],
          p: js.UndefOr[Quad.Predicate | Null],
          o: js.UndefOr[Quad.Object | Null],
          g: js.UndefOr[Quad.Graph | Null],
          justOne: js.UndefOr[Boolean] = false
      ): js.Array[Quad] = js.native

      /** Find out whether a given URI is used as symbol in the formula
        *
        * @param uri
        *   The URI to look for
        */
      //		def mentionsURI(uri: String): Boolean = js.native

      /** Dictionary of namespace prefixes */
      //		var namespaces: StringDictionary[String] = js.native

      /** Existentials are BNodes - something exists without naming
        *
        * @param uri
        *   An URI
        */
      def newExistential(uri: String): Term[?] = js.native

      /** Adds a new property action
        *
        * @param pred
        *   the predicate that the function should be triggered on
        * @param action
        *   the function that should trigger
        */
      //		def newPropertyAction(
      //			pred: QuadPredicate,
      //			action: js.Function4[
      //				/* store */ this.type,
      //				/* subject */ QuadSubject,
      //				/* predicate */ QuadPredicate,
      //				/* object */ QuadObject,
      //				Boolean
      //			]
      //		): Boolean = js.native

      /** Creates a new universal node Universals are Variables
        *
        * @param uri
        *   An URI
        */
      def newUniversal(uri: String): NamedNode = js.native

      /** Find an unused id for a file being edited: return a symbol (Note: Slow iff a lot of them
        * -- could be O(log(k)) )
        *
        * @param doc
        *   A document named node
        */
      def nextSymbol(doc: NamedNode): NamedNode = js.native

      /** Array of statements with this X as object */
      //		var objectIndex: js.Array[
      //			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
      //		] = js.native

      /** Array of statements with this X as predicate */
      //		var predicateIndex: js.Array[
      //			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
      //		] = js.native

      /** Map of iri predicates to functions to call when getting statement with {s X o} */
      var propertyActions: StringDictionary[js.Array[js.ThisFunction4[
        /* store */ this.type,
        /* subject */ Quad.Subject,
        /* predicate */ Quad.Predicate,
        /* object */ Quad.Object,
        /* graph */ Quad.Graph,
        Boolean
      ]]] = js.native

      /** Query this store asynchronously, return bindings in callback
        *
        * @param myQuery
        *   The query to be run
        * @param callback
        *   Function to call when bindings
        * @param Fetcher
        *   \| null If you want the query to do link following
        * @param onDone
        *   OBSOLETE - do not use this // @@ Why not ?? Called when query complete
        */
      //		def query(
      //			myQuery: /* import warning: transforms.QualifyReferences#resolveTypeRef many Couldn't qualify Query */ js.Any,
      //			callback: js.Function1[/* bindings */ Bindings, Unit]
      //		): Unit = js.native
      //		def query(
      //			myQuery: /* import warning: transforms.QualifyReferences#resolveTypeRef many Couldn't qualify Query */ js.Any,
      //			callback: js.Function1[/* bindings */ Bindings, Unit],
      //			fetcher: Null,
      //			onDone: js.Function0[Unit]
      //		): Unit = js.native
      //		def query(
      //			myQuery: /* import warning: transforms.QualifyReferences#resolveTypeRef many Couldn't qualify Query */ js.Any,
      //			callback: js.Function1[/* bindings */ Bindings, Unit],
      //			fetcher: Unit,
      //			onDone: js.Function0[Unit]
      //		): Unit = js.native
      //		def query(
      //			myQuery: /* import warning: transforms.QualifyReferences#resolveTypeRef many Couldn't qualify Query */ js.Any,
      //			callback: js.Function1[/* bindings */ Bindings, Unit],
      //			fetcher: types.rdflib.fetcherMod.default
      //		): Unit = js.native
      //		def query(
      //			myQuery: /* import warning: transforms.QualifyReferences#resolveTypeRef many Couldn't qualify Query */ js.Any,
      //			callback: js.Function1[/* bindings */ Bindings, Unit],
      //			fetcher: types.rdflib.fetcherMod.default,
      //			onDone: js.Function0[Unit]
      //		): Unit = js.native

      /** Query this store synchronously and return bindings
        *
        * @param myQuery
        *   The query to be run
        */
      //		def querySync(
      //			myQuery: /* import warning: transforms.QualifyReferences#resolveTypeRef many Couldn't qualify Query */ js.Any
      //		): js.Array[js.Any] = js.native

      /** Function to remove quads from the store arrays with */
      /* private */
      var rdfArrayRemove: js.Any = js.native

      /** Redirect to lexically smaller equivalent symbol */
      //		var redirections: js.Array[js.Any] = js.native

      def remove(st: js.Array[Quad]): this.type = js.native

      /** Removes one or multiple statement(s) from this formula
        *
        * @param st
        *   \- A Statement or array of Statements to remove
        */
      def remove(st: Quad): this.type = js.native

      /** Removes all statements in a doc
        *
        * @param doc
        *   \- The document / graph
        */
//		def removeDocument(doc: QuadGraph): IndexedFormula = js.native

      /** Remove all statements matching args (within limit) *
        *
        * @param subj
        *   The subject
        * @param pred
        *   The predicate
        * @param obj
        *   The object
        * @param why
        *   The graph that contains the statement
        * @param limit
        *   The number of statements to remove
        */
//		def removeMany(
//			subj: js.UndefOr[QuadSubject | Null],
//			pred: js.UndefOr[QuadPredicate | Null],
//			obj: js.UndefOr[QuadObject | Null],
//			why: js.UndefOr[QuadGraph | Null],
//			limit: js.UndefOr[Double]
//		): Unit = js.native

      /** Remove all matching statements
        *
        * @param subject
        *   The subject
        * @param predicate
        *   The predicate
        * @param object
        *   The object
        * @param graph
        *   The graph that contains the statement
        */
      def removeMatches(
          subject: js.UndefOr[Quad.Subject],
          predicate: js.UndefOr[Quad.Predicate],
          `object`: js.UndefOr[Quad.Object],
          graph: js.UndefOr[Quad.Graph]
      ): IndexedFormula = js.native

      /** Remove a particular statement object from the store
        *
        * @param st
        *   \- a statement which is already in the store and indexed. Make sure you only use this
        *   for these. Otherwise, you should use remove() above.
        */
      def removeStatement(st: Quad): IndexedFormula = js.native

      /** Removes statements
        *
        * @param sts
        *   The statements to remove
        */
      def removeStatements(sts: js.Array[Quad]): IndexedFormula = js.native

      /** Replace big with small, obsoleted with obsoleting. */
      //		def replaceWith(big: QuadSubject, small: QuadSubject): Boolean = js.native

      /** Compare by canonical URI as smushed
        *
        * @param x
        *   A named node
        * @param y
        *   Another named node
        */
      //		def sameThings(x: types.rdflib.namedNodeMod.default, y: types.rdflib.namedNodeMod.default): Boolean = js.native

      def setPrefixForURI(prefix: String, nsuri: String): Unit = js.native

      /** Array of statements with this X as subject */
      //		var subjectIndex: js.Array[
      //			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
      //		] = js.native

      /** An UpdateManager initialised to this store */
      //		var updater: js.UndefOr[types.rdflib.updateManagerMod.default] = js.native

      /** A list of all the URIs by which this thing is known
        *
        * @param term
        */
      //		def uris(term: QuadSubject): js.Array[String] = js.native

      //		/** Array of statements with X as provenance */
      //		var whyIndex: js.Array[
      //			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
      //		] = js.native
