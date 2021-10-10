package org.w3.banana.rdflib

import scala.scalajs.js
import scala.scalajs.js.annotation.*

object formulaMod {

	@JSImport("rdflib/lib/formula", JSImport.Default)
	@js.native

	/**
	 * Initializes this formula
	 *
	 * @constructor
	 * @param statements   - Initial array of statements
	 * @param constraints  - initial array of constraints
	 * @param initBindings - initial bindings used in Query
	 * @param optional     - optional
	 * @param opts
	 * @param opts         .rdfFactory - The rdf factory that should be used by the store
	 */
	open class default() extends Formula {
		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			]) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: js.Array[js.Any]
		) = this()

		def this(statements: Unit, constraints: js.Array[js.Any]) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: js.Array[js.Any],
			initBindings: js.Array[js.Any]
		) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: Unit,
			initBindings: js.Array[js.Any]
		) = this()

		def this(statements: Unit, constraints: js.Array[js.Any], initBindings: js.Array[js.Any]) = this()

		def this(statements: Unit, constraints: Unit, initBindings: js.Array[js.Any]) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: js.Array[js.Any],
			initBindings: js.Array[js.Any],
			optional: js.Array[js.Any]
		) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: js.Array[js.Any],
			initBindings: Unit,
			optional: js.Array[js.Any]
		) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: Unit,
			initBindings: js.Array[js.Any],
			optional: js.Array[js.Any]
		) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: Unit,
			initBindings: Unit,
			optional: js.Array[js.Any]
		) = this()

		def this(
			statements: Unit,
			constraints: js.Array[js.Any],
			initBindings: js.Array[js.Any],
			optional: js.Array[js.Any]
		) = this()

		def this(statements: Unit, constraints: js.Array[js.Any], initBindings: Unit, optional: js.Array[js.Any]) = this()

		def this(statements: Unit, constraints: Unit, initBindings: js.Array[js.Any], optional: js.Array[js.Any]) = this()

		def this(statements: Unit, constraints: Unit, initBindings: Unit, optional: js.Array[js.Any]) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: js.Array[js.Any],
			initBindings: js.Array[js.Any],
			optional: js.Array[js.Any],
			opts: FormulaOpts
		) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: js.Array[js.Any],
			initBindings: js.Array[js.Any],
			optional: Unit,
			opts: FormulaOpts
		) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: js.Array[js.Any],
			initBindings: Unit,
			optional: js.Array[js.Any],
			opts: FormulaOpts
		) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: js.Array[js.Any],
			initBindings: Unit,
			optional: Unit,
			opts: FormulaOpts
		) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: Unit,
			initBindings: js.Array[js.Any],
			optional: js.Array[js.Any],
			opts: FormulaOpts
		) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: Unit,
			initBindings: js.Array[js.Any],
			optional: Unit,
			opts: FormulaOpts
		) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: Unit,
			initBindings: Unit,
			optional: js.Array[js.Any],
			opts: FormulaOpts
		) = this()

		def this(
			statements: js.Array[
				types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
			],
			constraints: Unit,
			initBindings: Unit,
			optional: Unit,
			opts: FormulaOpts
		) = this()

		def this(
			statements: Unit,
			constraints: js.Array[js.Any],
			initBindings: js.Array[js.Any],
			optional: js.Array[js.Any],
			opts: FormulaOpts
		) = this()

		def this(
			statements: Unit,
			constraints: js.Array[js.Any],
			initBindings: js.Array[js.Any],
			optional: Unit,
			opts: FormulaOpts
		) = this()

		def this(
			statements: Unit,
			constraints: js.Array[js.Any],
			initBindings: Unit,
			optional: js.Array[js.Any],
			opts: FormulaOpts
		) = this()

		def this(
			statements: Unit,
			constraints: js.Array[js.Any],
			initBindings: Unit,
			optional: Unit,
			opts: FormulaOpts
		) = this()

		def this(
			statements: Unit,
			constraints: Unit,
			initBindings: js.Array[js.Any],
			optional: js.Array[js.Any],
			opts: FormulaOpts
		) = this()

		def this(
			statements: Unit,
			constraints: Unit,
			initBindings: js.Array[js.Any],
			optional: Unit,
			opts: FormulaOpts
		) = this()

		def this(
			statements: Unit,
			constraints: Unit,
			initBindings: Unit,
			optional: js.Array[js.Any],
			opts: FormulaOpts
		) = this()

		def this(statements: Unit, constraints: Unit, initBindings: Unit, optional: Unit, opts: FormulaOpts) = this()
	}

	type BooleanMap = StringDictionary[Boolean]

	@js.native
	trait Formula
		extends types.rdflib.nodeInternalMod.default {

		/**
		 * Transform a collection of NTriple URIs into their URI strings
		 *
		 * @param t - Some iterable collection of NTriple URI strings
		 * @return A collection of the URIs as strings
		 *         todo: explain why it is important to go through NT
		 */
		def NTtoURI(t: js.Any): js.Object = js.native

		def add(subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]]): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
			predicate: Unit,
			`object`: String
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
			predicate: Unit,
			`object`: String,
			graph: QuadGraph
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
			predicate: Unit,
			`object`: Unit,
			graph: QuadGraph
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
			predicate: Unit,
			`object`: Term
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
			predicate: Unit,
			`object`: Term,
			graph: QuadGraph
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
			predicate: QuadPredicate
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
			predicate: QuadPredicate,
			`object`: String
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
			predicate: QuadPredicate,
			`object`: String,
			graph: QuadGraph
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
			predicate: QuadPredicate,
			`object`: Unit,
			graph: QuadGraph
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
			predicate: QuadPredicate,
			`object`: Term
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
			predicate: QuadPredicate,
			`object`: Term,
			graph: QuadGraph
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		/** Add a statement from its parts
		 *
		 * @param subject   - the first part of the statement
		 * @param predicate - the second part of the statement
		 * @param object    - the third part of the statement
		 * @param graph     - the last part of the statement
		 */
		def add(subject: QuadSubject): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: QuadSubject,
			predicate: Unit,
			`object`: String): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: QuadSubject,
			predicate: Unit,
			`object`: String,
			graph: QuadGraph): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: QuadSubject,
			predicate: Unit,
			`object`: Unit,
			graph: QuadGraph): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: QuadSubject,
			predicate: Unit,
			`object`: Term): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: QuadSubject,
			predicate: Unit,
			`object`: Term,
			graph: QuadGraph): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: QuadSubject,
			predicate: QuadPredicate): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: QuadSubject,
			predicate: QuadPredicate,
			`object`: String): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: QuadSubject,
			predicate: QuadPredicate,
			`object`: String,
			graph: QuadGraph): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: QuadSubject,
			predicate: QuadPredicate,
			`object`: Unit,
			graph: QuadGraph): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: QuadSubject,
			predicate: QuadPredicate,
			`object`: Term): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: QuadSubject,
			predicate: QuadPredicate,
			`object`: Term,
			graph: QuadGraph): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
			predicate: Unit,
			`object`: String
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
			predicate: Unit,
			`object`: String,
			graph: QuadGraph
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
			predicate: Unit,
			`object`: Unit,
			graph: QuadGraph
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
			predicate: Unit,
			`object`: Term): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
			predicate: Unit,
			`object`: Term,
			graph: QuadGraph
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
			predicate: QuadPredicate): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
			predicate: QuadPredicate,
			`object`: String
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
			predicate: QuadPredicate,
			`object`: String,
			graph: QuadGraph
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
			predicate: QuadPredicate,
			`object`: Unit,
			graph: QuadGraph
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
			predicate: QuadPredicate,
			`object`: Term
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		def add(
			subject: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
			predicate: QuadPredicate,
			`object`: Term,
			graph: QuadGraph
		): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		/**
		 * Adds all the statements to this formula
		 *
		 * @param statements - A collection of statements
		 */
		def addAll(statements: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]]): Unit = js.native

		/** Add a statment object
		 *
		 * @param {Statement} statement - An existing constructed statement to add
		 */
		def addStatement(statement: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]): (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]) | Null | this.type | Double = js.native

		/** Follow link from one node, using one wildcard, looking for one
		 *
		 * For example, any(me, knows, null, profile)  - a person I know accoring to my profile .
		 * any(me, knows, null, null)  - a person I know accoring to anything in store .
		 * any(null, knows, me, null)  - a person who know me accoring to anything in store .
		 *
		 * @param s - A node to search for as subject, or if null, a wildcard
		 * @param p - A node to search for as predicate, or if null, a wildcard
		 * @param o - A node to search for as object, or if null, a wildcard
		 * @param g - A node to search for as graph, or if null, a wildcard
		 * @returns A node which match the wildcard position, or null
		 */
		def any(
			s: js.UndefOr[QuadSubject | Null],
			p: js.UndefOr[QuadPredicate | Null],
			o: js.UndefOr[QuadObject | Null],
			g: js.UndefOr[QuadGraph | Null]
		): types.rdflib.nodeInternalMod.default | Null = js.native

		/**
		 * Gets the first JavaScript object equivalent to a node based on the specified pattern
		 *
		 * @param s The subject
		 * @param p The predicate
		 * @param o The object
		 * @param g The graph that contains the statement
		 */
		def anyJS(
			s: js.UndefOr[QuadSubject | Null],
			p: js.UndefOr[QuadPredicate | Null],
			o: js.UndefOr[QuadObject | Null],
			g: js.UndefOr[QuadGraph | Null]
		): js.Any = js.native

		/**
		 * Gets the first statement that matches the specified pattern
		 */
		def anyStatementMatching(
			s: js.UndefOr[QuadSubject | Null],
			p: js.UndefOr[QuadPredicate | Null],
			o: js.UndefOr[QuadObject | Null],
			g: js.UndefOr[QuadGraph | Null]
		): js.UndefOr[
			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
		] = js.native

		/**
		 * Gets the value of a node that matches the specified pattern
		 *
		 * @param s The subject
		 * @param p The predicate
		 * @param o The object
		 * @param g The graph that contains the statement
		 */
		def anyValue(
			s: js.UndefOr[QuadSubject | Null],
			p: js.UndefOr[QuadPredicate | Null],
			o: js.UndefOr[QuadObject | Null],
			g: js.UndefOr[QuadGraph | Null]
		): String | Unit = js.native

		/**
		 * Shortcut for adding blankNodes
		 *
		 * @param [id]
		 */
		def bnode(): types.rdflib.blankNodeMod.default = js.native

		def bnode(id: String): types.rdflib.blankNodeMod.default = js.native

		/**
		 * Finds the types in the list which have no *stored* subtypes
		 * These are a set of classes which provide by themselves complete
		 * information -- the other classes are redundant for those who
		 * know the class DAG.
		 *
		 * @param types A map of the types
		 */
		def bottomTypeURIs(types_ : js.Any): js.Any = js.native

		/** Creates a new collection */
		def collection(): types.rdflib.collectionMod.default[
			types.rdflib.nodeInternalMod.default | types.rdflib.blankNodeMod.default | Collection[js.Any] | types.rdflib.literalMod.default | types.rdflib.variableMod.default
		] = js.native

		/** Trace statements which connect directly, or through bnodes
		 *
		 * @param subject - The node to start looking for statments
		 * @param doc     - The document to be searched, or null to search all documents
		 * @returns an array of statements, duplicate statements are suppresssed.
		 */
		def connectedStatements(subject: QuadSubject, doc: QuadGraph): js.Array[
			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
		] = js.native

		def connectedStatements(subject: QuadSubject, doc: QuadGraph, excludePredicateURIs: js.Array[String]): js.Array[
			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
		] = js.native

		var constraints: js.Array[js.Any] = js.native

		/** Follow links from one node, using one wildcard.
		 *
		 * For example, each(me, knows, null, profile)  - people I know accoring to my profile .
		 * each(me, knows, null, null)  - people I know accoring to anything in store .
		 * each(null, knows, me, null)  - people who know me accoring to anything in store .
		 *
		 * @param s - A node to search for as subject, or if null, a wildcard
		 * @param p - A node to search for as predicate, or if null, a wildcard
		 * @param o - A node to search for as object, or if null, a wildcard
		 * @param g - A node to search for as graph, or if null, a wildcard
		 * @returns {Array<Node>} - An array of nodes which match the wildcard position
		 */
		def each(
			s: js.UndefOr[QuadSubject | Null],
			p: js.UndefOr[QuadPredicate | Null],
			o: js.UndefOr[QuadObject | Null],
			g: js.UndefOr[QuadGraph | Null]
		): js.Array[types.rdflib.nodeInternalMod.default] = js.native

		/**
		 * Test whether this formula is equals to {other}
		 *
		 * @param other - The other formula
		 */
		def equals(other: Formula): Boolean = js.native

		/**
		 * The accompanying fetcher instance.
		 *
		 * Is set by the fetcher when initialized.
		 */
		var fetcher: js.UndefOr[types.rdflib.fetcherMod.default] = js.native

		/**
		 * For thisClass or any subclass, anything which has it is its type
		 * or is the object of something which has the type as its range, or subject
		 * of something which has the type as its domain
		 * We don't bother doing subproperty (yet?)as it doesn't seeem to be used
		 * much.
		 * Get all the Classes of which we can RDFS-infer the subject is a member
		 *
		 * @param subject - A named node
		 */
		def findMemberURIs(subject: types.rdflib.nodeInternalMod.default): UriMap = js.native

		/**
		 * For thisClass or any subclass, anything which has it is its type
		 * or is the object of something which has the type as its range, or subject
		 * of something which has the type as its domain
		 * We don't bother doing subproperty (yet?)as it doesn't seeem to be used
		 * much.
		 * Get all the Classes of which we can RDFS-infer the subject is a member
		 *
		 * @return a hash of URIs
		 */
		def findMembersNT(thisClass: js.Any): MembersMap = js.native

		/**
		 * Get all the Classes of which we can RDFS-infer the subject is a superclass
		 * Returns a hash table where key is NT of type and value is statement why we
		 * think so.
		 * Does NOT return terms, returns URI strings.
		 * We use NT representations in this version because they handle blank nodes.
		 */
		def findSubClassesNT(subject: types.rdflib.nodeInternalMod.default): StringDictionary[Boolean] = js.native

		/**
		 * Get all the Classes of which we can RDFS-infer the subject is a subclass
		 *
		 * @param {RDFlibNamedNode} subject - The thing whose classes are to be found
		 * @returns a hash table where key is NT of type and value is statement why we
		 *          think so.
		 *          Does NOT return terms, returns URI strings.
		 *          We use NT representations in this version because they handle blank nodes.
		 */
		def findSuperClassesNT(subject: types.rdflib.nodeInternalMod.default): StringDictionary[Boolean] = js.native

		/**
		 * Get all the Classes of which we can RDFS-infer the subject is a member
		 * todo: This will loop is there is a class subclass loop (Sublass loops are
		 * not illegal)
		 * Returns a hash table where key is NT of type and value is statement why we
		 * think so.
		 * Does NOT return terms, returns URI strings.
		 * We use NT representations in this version because they handle blank nodes.
		 *
		 * @param subject - A subject node
		 */
		def findTypeURIs(subject: QuadSubject): UriMap = js.native

		/**
		 * Get all the Classes of which we can RDFS-infer the subject is a member
		 * todo: This will loop is there is a class subclass loop (Sublass loops are
		 * not illegal)
		 *
		 * @param {RDFlibNamedNode} subject - The thing whose classes are to be found
		 * @returns a hash table where key is NT of type and value is statement why we think so.
		 *          Does NOT return terms, returns URI strings.
		 *          We use NT representations in this version because they handle blank nodes.
		 */
		def findTypesNT(subject: js.Any): StringDictionary[Boolean] = js.native

		/**
		 * Creates a new empty formula
		 *
		 * @param _features - Not applicable, but necessary for typing to pass
		 */
		def formula(): Formula = js.native

		def formula(_features: js.Array[String]): Formula = js.native

		/**
		 * Transforms an NTriples string format into a Node.
		 * The blank node bit should not be used on program-external values; designed
		 * for internal work such as storing a blank node id in an HTML attribute.
		 * This will only parse the strings generated by the various toNT() methods.
		 */
		def fromNT(str: js.Any): js.Any = js.native

		/** Returns true if this formula holds the specified statement(s) */
		def holds(s: js.Any): Boolean = js.native

		def holds(s: js.Any, p: js.Any): Boolean = js.native

		def holds(s: js.Any, p: js.Any, o: js.Any): Boolean = js.native

		def holds(s: js.Any, p: js.Any, o: js.Any, g: js.Any): Boolean = js.native

		def holds(s: js.Any, p: js.Any, o: Unit, g: js.Any): Boolean = js.native

		def holds(s: js.Any, p: Unit, o: js.Any): Boolean = js.native

		def holds(s: js.Any, p: Unit, o: js.Any, g: js.Any): Boolean = js.native

		def holds(s: js.Any, p: Unit, o: Unit, g: js.Any): Boolean = js.native

		def holds(s: js.Array[js.Any]): Boolean = js.native

		def holds(s: js.Array[js.Any], p: js.Any): Boolean = js.native

		def holds(s: js.Array[js.Any], p: js.Any, o: js.Any): Boolean = js.native

		def holds(s: js.Array[js.Any], p: js.Any, o: js.Any, g: js.Any): Boolean = js.native

		def holds(s: js.Array[js.Any], p: js.Any, o: Unit, g: js.Any): Boolean = js.native

		def holds(s: js.Array[js.Any], p: Unit, o: js.Any): Boolean = js.native

		def holds(s: js.Array[js.Any], p: Unit, o: js.Any, g: js.Any): Boolean = js.native

		def holds(s: js.Array[js.Any], p: Unit, o: Unit, g: js.Any): Boolean = js.native

		/**
		 * Returns true if this formula holds the specified {statement}
		 */
		def holdsStatement(statement: js.Any): Boolean = js.native

		/**
		 * Returns a unique index-safe identifier for the given term.
		 *
		 * Falls back to the rdflib hashString implementation if the given factory doesn't support id.
		 */
		def id(term: TFIDFactoryTypes): Indexable = js.native

		var initBindings: js.Array[js.Any] = js.native

		var isVar: Double = js.native

		/**
		 * Used by the n3parser to generate list elements
		 *
		 * @param values  - The values of the collection
		 * @param context - The store
		 * @return {BlankNode|Collection} - The term for the statement
		 */
		def list(values: js.Any, context: js.Any): js.Any = js.native

		/**
		 * A namespace for the specified namespace's URI
		 *
		 * @param nsuri The URI for the namespace
		 */
		def ns(nsuri: String): js.Function1[ /* ln */ String, NamedNode] = js.native

		def ns(nsuri: String, factory: RdfJsDataFactory): js.Function1[ /* ln */ String, NamedNode] = js.native

		/**
		 * A namespace for the specified namespace's URI
		 *
		 * @param nsuri The URI for the namespace
		 */
		@JSName("ns")
		var ns_Original: js.Function2[
			/* nsuri */ String,
			/* factory */ js.UndefOr[RdfJsDataFactory],
			js.Function1[ /* ln */ String, NamedNode]
		] = js.native

		var optional: js.Array[js.Any] = js.native

		/** The factory used to generate statements and terms */
		var rdfFactory: js.Any = js.native

		/**
		 * Serializes this formula
		 *
		 * @param base        - The base string
		 * @param contentType - The content type of the syntax to use
		 * @param provenance  - The provenance URI
		 */
		def serialize(base: js.Any, contentType: js.Any, provenance: js.Any): js.Any = js.native

		var statements: js.Array[
			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
		] = js.native

		/**
		 * Search the Store
		 * This is really a teaching method as to do this properly you would use IndexedFormula
		 *
		 * @param s       - A node to search for as subject, or if null, a wildcard
		 * @param p       - A node to search for as predicate, or if null, a wildcard
		 * @param o       - A node to search for as object, or if null, a wildcard
		 * @param g       - A node to search for as graph, or if null, a wildcard
		 * @param justOne - flag - stop when found one rather than get all of them?
		 * @returns {Array<Node>} - An array of nodes which match the wildcard position
		 */
		def statementsMatching[JustOne /* <: Boolean */ ](
			s: js.UndefOr[QuadSubject | Null],
			p: js.UndefOr[QuadPredicate | Null],
			o: js.UndefOr[QuadObject | Null],
			g: js.UndefOr[QuadGraph | Null],
			justOne: js.UndefOr[Boolean]
		): js.Array[
			types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
		] = js.native

		def sym(uri: String): types.rdflib.namedNodeMod.default = js.native

		def sym(uri: String, name: js.Any): types.rdflib.namedNodeMod.default = js.native

		@JSName("termType")
		var termType_Formula: Graph = js.native

		/**
		 * Gets the node matching the specified pattern. Throws when no match could be made.
		 *
		 * @param s - The subject
		 * @param p - The predicate
		 * @param o - The object
		 * @param g - The graph that contains the statement
		 */
		def the(
			s: js.UndefOr[QuadSubject | Null],
			p: js.UndefOr[QuadPredicate | Null],
			o: js.UndefOr[QuadObject | Null],
			g: js.UndefOr[QuadGraph | Null]
		): js.UndefOr[types.rdflib.nodeInternalMod.default | Null] = js.native

		/**
		 * Finds the types in the list which have no *stored* supertypes
		 * We exclude the universal class, owl:Things and rdf:Resource, as it is
		 * information-free.
		 *
		 * @param types - The types
		 */
		def topTypeURIs(types_ : StringDictionary[String | types.rdflib.namedNodeMod.default]): StringDictionary[String | types.rdflib.namedNodeMod.default] = js.native

		/**
		 * RDFS Inference
		 * These are hand-written implementations of a backward-chaining reasoner
		 * over the RDFS axioms.
		 *
		 * @param seeds     - A hash of NTs of classes to start with
		 * @param predicate - The property to trace though
		 * @param inverse   - Trace inverse direction
		 */
		def transitiveClosure(seeds: BooleanMap, predicate: QuadPredicate): StringDictionary[Boolean] = js.native

		def transitiveClosure(
			seeds: BooleanMap,
			predicate: QuadPredicate,
			inverse: Boolean): StringDictionary[Boolean] = js.native

		/**
		 * Gets a new variable
		 *
		 * @param name - The variable's name
		 */
		def variable(name: String): types.rdflib.variableMod.default = js.native

		/**
		 * Gets the number of statements in this formula that matches the specified pattern
		 *
		 * @param s - The subject
		 * @param p - The predicate
		 * @param o - The object
		 * @param g - The graph that contains the statement
		 */
		def whether(
			s: js.UndefOr[QuadSubject | Null],
			p: js.UndefOr[QuadPredicate | Null],
			o: js.UndefOr[QuadObject | Null],
			g: js.UndefOr[QuadGraph | Null]
		): Double = js.native
	}

	trait FormulaOpts extends StObject {

		var dataCallback: js.UndefOr[
			js.Function1[ /* q */ Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph], Unit]
		] = js.undefined

		var rdfArrayRemove: js.UndefOr[
			js.Function2[
				/* arr */ js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
				/* q */ Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph],
				Unit
			]
		] = js.undefined

		var rdfFactory: js.UndefOr[RdfJsDataFactory] = js.undefined
	}

	object FormulaOpts {

		inline def apply(): FormulaOpts = {
			val __obj = js.Dynamic.literal()
			__obj.asInstanceOf[FormulaOpts]
		}

		extension[Self <: FormulaOpts] (x: Self) {

			inline def setDataCallback(value: /* q */ Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph] => Unit): Self = StObject.set(x, "dataCallback", js.Any.fromFunction1(value))

			inline def setDataCallbackUndefined: Self = StObject.set(x, "dataCallback", js.undefined)

			inline def setRdfArrayRemove(
				value: ( /* arr */ js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]], /* q */ Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]) => Unit
			): Self = StObject.set(x, "rdfArrayRemove", js.Any.fromFunction2(value))

			inline def setRdfArrayRemoveUndefined: Self = StObject.set(x, "rdfArrayRemove", js.undefined)

			inline def setRdfFactory(value: RdfJsDataFactory): Self = StObject.set(x, "rdfFactory", value.asInstanceOf[js.Any])

			inline def setRdfFactoryUndefined: Self = StObject.set(x, "rdfFactory", js.undefined)
		}
	}

	type MembersMap = StringDictionary[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]]

	type UriMap = StringDictionary[String]
}
