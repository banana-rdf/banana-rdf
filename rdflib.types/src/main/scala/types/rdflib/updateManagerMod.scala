package types.rdflib

import org.scalajs.dom.experimental.Response
import types.rdflib.anon.Setobject
import types.rdflib.tfTypesMod.BlankNode
import types.rdflib.tfTypesMod.NamedNode
import types.rdflib.tfTypesMod.Quad
import types.rdflib.tfTypesMod.QuadGraph
import types.rdflib.tfTypesMod.QuadObject
import types.rdflib.tfTypesMod.QuadPredicate
import types.rdflib.tfTypesMod.QuadSubject
import types.rdflib.tfTypesMod.Term
import types.rdflib.typesMod.GraphType
import types.rdflib.typesMod.ObjectType
import types.rdflib.typesMod.PredicateType
import types.rdflib.typesMod.SubjectType
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object updateManagerMod {
  
  @JSImport("rdflib/lib/update-manager", JSImport.Default)
  @js.native
  /**
    * @param  store - The quadstore to store data and metadata. Created if not passed.
    */
  open class default ()
    extends StObject
       with UpdateManager {
    def this(store: types.rdflib.storeMod.default) = this()
  }
  
  type CallBackFunction = js.Function4[
    /* uri */ String, 
    /* ok */ Boolean, 
    /* message */ String, 
    /* response */ js.Error | Response, 
    js.Object | Unit
  ]
  
  @js.native
  trait UpdateManager extends StObject {
    
    def addDownstreamChangeListener(doc: NamedNode, listener: js.Any): Unit = js.native
    
    def anonymize(obj: js.Any): js.Any = js.native
    
    def anonymizeNT(stmt: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]): String = js.native
    
    /**
      * @private
      */
    def bnodeContext(bnodes: js.Any, doc: js.Any): js.Array[scala.Nothing] = js.native
    
    /**
      * Returns the smallest context to bind a given single bnode
      * @private
      */
    def bnodeContext1(x: js.Any, source: js.Any): js.Any = js.native
    
    /**
      * Returns a context to bind a given node, up to a given depth
      * @private
      */
    def bnodeContext2(x: js.Any, source: js.Any, depth: js.Any): js.Any = js.native
    
    /**
      * Makes a cached list of [Inverse-]Functional properties
      * @private
      */
    def cacheIfps(): Unit = js.native
    
    /**
      * We want to start counting websocket notifications
      * to distinguish the ones from others from our own.
      */
    def clearUpstreamCount(doc: NamedNode): Unit = js.native
    
    /**
      * @private
      */
    def contextWhere(context: js.Any): String = js.native
    
    def delete_statement(
      st: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
      callbackFunction: CallBackFunction
    ): Unit = js.native
    def delete_statement(st: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph], callbackFunction: CallBackFunction): Unit = js.native
    
    /**
      * Tests whether a file is editable.
      * If the file has a specific annotation that it is machine written,
      * for safety, it is editable (this doesn't actually check for write access)
      * If the file has wac-allow and accept patch headers, those are respected.
      * and local write access is determined by those headers.
      * This version only looks at past HTTP requests, does not make new ones.
      *
      * @returns The method string SPARQL or DAV or
      *   LOCALFILE or false if known, undefined if not known.
      */
    def editable(uri: String): js.UndefOr[String | Boolean] = js.native
    def editable(uri: String, kb: types.rdflib.storeMod.default): js.UndefOr[String | Boolean] = js.native
    def editable(uri: NamedNode): js.UndefOr[String | Boolean] = js.native
    def editable(uri: NamedNode, kb: types.rdflib.storeMod.default): js.UndefOr[String | Boolean] = js.native
    
    /**
      * @private
      */
    def fire(uri: String, query: String, callbackFunction: CallBackFunction): js.Promise[Unit] = js.native
    
    def getUpdatesVia(doc: NamedNode): String | Null = js.native
    
    def insert_statement(st: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph], callbackFunction: CallBackFunction): Unit = js.native
    
    def isHttpUri(uri: String): Boolean = js.native
    
    /**
      * @private
      */
    def mentioned(x: js.Any): Boolean = js.native
    
    /** Object of namespaces */
    var ns: js.Any = js.native
    
    /** Index of objects for coordinating incoming and outgoing patches */
    var patchControl: js.Array[js.Any] = js.native
    
    def patchControlFor(doc: NamedNode): js.Any = js.native
    
    /**
      * This is suitable for an initial creation of a document.
      */
    def put(
      doc: types.rdflib.namedNodeMod.default,
      data: String,
      contentType: String,
      callback: js.Function4[
          /* uri */ String, 
          /* ok */ Boolean, 
          /* errorMessage */ js.UndefOr[String], 
          /* response */ js.UndefOr[js.Any], 
          Unit
        ]
    ): js.Promise[Unit] = js.native
    def put(
      doc: types.rdflib.namedNodeMod.default,
      data: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
      contentType: String,
      callback: js.Function4[
          /* uri */ String, 
          /* ok */ Boolean, 
          /* errorMessage */ js.UndefOr[String], 
          /* response */ js.UndefOr[js.Any], 
          Unit
        ]
    ): js.Promise[Unit] = js.native
    
    /**
      * Reloads a document.
      *
      * Fast and cheap, no metadata. Measure times for the document.
      * Load it provisionally.
      * Don't delete the statements before the load, or it will leave a broken
      * document in the meantime.
      *
      * @param kb
      * @param doc {RDFlibNamedNode}
      * @param callbackFunction
      */
    def reload(
      kb: types.rdflib.storeMod.default,
      doc: docReloadType,
      callbackFunction: js.Function3[
          /* ok */ Boolean, 
          /* message */ js.UndefOr[String], 
          /* response */ js.UndefOr[js.Error | Response], 
          js.Object | Unit
        ]
    ): Unit = js.native
    
    def reloadAndSync(doc: NamedNode): Unit = js.native
    
    /**
      * Requests a now or future action to refresh changes coming downstream
      * This is designed to allow the system to re-request the server version,
      * when a websocket has pinged to say there are changes.
      * If the websocket, by contrast, has sent a patch, then this may not be necessary.
      *
      * @param doc
      * @param action
      */
    def requestDownstreamAction(doc: NamedNode, action: js.Any): Unit = js.native
    
    /**
      * @throws {Error} On unsupported content type
      *
      * @returns {string}
      */
    def serialize(uri: String, data: String, contentType: String): String = js.native
    def serialize(
      uri: String,
      data: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]],
      contentType: String
    ): String = js.native
    
    /**
      * Sets up websocket to listen on
      *
      * There is coordination between upstream changes and downstream ones
      * so that a reload is not done in the middle of an upstream patch.
      * If you use this API then you get called when a change happens, and you
      * have to reload the file yourself, and then refresh the UI.
      * Alternative is addDownstreamChangeListener(), where you do not
      * have to do the reload yourself. Do mot mix them.
      *
      * kb contains the HTTP  metadata from previous operations
      *
      * @param doc
      * @param handler
      *
      * @returns {boolean}
      */
    def setRefreshHandler(doc: NamedNode, handler: js.Any): Boolean = js.native
    
    /**
      * Returns a list of all bnodes occurring in a list of statements
      * @private
      */
    def statementArrayBnodes(sts: js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]]): js.Array[BlankNode] = js.native
    
    /**
      * Returns a list of all bnodes occurring in a statement
      * @private
      */
    def statementBnodes(st: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]): js.Array[BlankNode] = js.native
    
    /**
      * Returns the best context for a single statement
      * @private
      */
    def statementContext(st: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]): js.Array[scala.Nothing] = js.native
    
    var store: UpdateManagerFormula = js.native
    
    /**
      * This high-level function updates the local store iff the web is changed successfully.
      * Deletions, insertions may be undefined or single statements or lists or formulae (may contain bnodes which can be indirectly identified by a where clause).
      * The `why` property of each statement must be the same and give the web document to be updated.
      * @param deletions - Statement or statements to be deleted.
      * @param insertions - Statement or statements to be inserted.
      * @param callback - called as callbackFunction(uri, success, errorbody)
      *           OR returns a promise
      */
    def update(
      deletions: js.Array[
          types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
        ],
      insertions: js.Array[
          types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
        ]
    ): Unit | js.Promise[Unit] = js.native
    def update(
      deletions: js.Array[
          types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
        ],
      insertions: js.Array[
          types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
        ],
      callback: js.Function4[
          /* uri */ js.UndefOr[String | Null], 
          /* success */ Boolean, 
          /* errorBody */ js.UndefOr[String], 
          /* response */ js.UndefOr[Response | js.Error], 
          Unit
        ]
    ): Unit | js.Promise[Unit] = js.native
    def update(
      deletions: js.Array[
          types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
        ],
      insertions: js.Array[
          types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
        ],
      callback: js.Function4[
          /* uri */ js.UndefOr[String | Null], 
          /* success */ Boolean, 
          /* errorBody */ js.UndefOr[String], 
          /* response */ js.UndefOr[Response | js.Error], 
          Unit
        ],
      secondTry: Boolean
    ): Unit | js.Promise[Unit] = js.native
    def update(
      deletions: js.Array[
          types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
        ],
      insertions: js.Array[
          types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
        ],
      callback: Unit,
      secondTry: Boolean
    ): Unit | js.Promise[Unit] = js.native
    
    def updateDav(doc: QuadSubject, ds: js.Any, is: js.Any, callbackFunction: js.Any): Null | js.Promise[Unit] = js.native
    
    /**
      * Likely deprecated, since this lib no longer deals with browser extension
      *
      * @param doc
      * @param ds
      * @param is
      * @param callbackFunction
      */
    def updateLocalFile(doc: NamedNode, ds: js.Any, is: js.Any, callbackFunction: js.Any): Unit = js.native
    
    /**
      * This high-level function updates the local store iff the web is changed successfully.
      * Deletions, insertions may be undefined or single statements or lists or formulae (may contain bnodes which can be indirectly identified by a where clause).
      * The `why` property of each statement must be the give the web document to be updated.
      * The statements to be deleted and inserted may span more than one web document.
      * @param deletions - Statement or statements to be deleted.
      * @param insertions - Statement or statements to be inserted.
      * @returns a promise
      */
    def updateMany(
      deletions: js.Array[
          types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
        ]
    ): js.Promise[js.Array[Unit]] = js.native
    def updateMany(
      deletions: js.Array[
          types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
        ],
      insertions: js.Array[
          types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
        ]
    ): js.Promise[js.Array[Unit]] = js.native
    
    /** return a statemnet updating function
      *
      * This does NOT update the statement.
      * It returns an object which includes
      *  function which can be used to change the object of the statement.
      */
    def update_statement(statement: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]): js.UndefOr[Setobject] = js.native
  }
  
  @js.native
  trait UpdateManagerFormula
    extends types.rdflib.storeMod.default {
    
    @JSName("fetcher")
    var fetcher_UpdateManagerFormula: types.rdflib.fetcherMod.default = js.native
  }
  
  trait docReloadType
    extends StObject
       with NamedNode {
    
    var reloadTimeCount: js.UndefOr[Double] = js.undefined
    
    var reloadTimeTotal: js.UndefOr[Double] = js.undefined
  }
  object docReloadType {
    
    inline def apply(equals_ : Term => Boolean, value: String): docReloadType = {
      val __obj = js.Dynamic.literal(termType = "NamedNode", value = value.asInstanceOf[js.Any])
      __obj.updateDynamic("equals")(js.Any.fromFunction1(equals_))
      __obj.asInstanceOf[docReloadType]
    }
    
    extension [Self <: docReloadType](x: Self) {
      
      inline def setReloadTimeCount(value: Double): Self = StObject.set(x, "reloadTimeCount", value.asInstanceOf[js.Any])
      
      inline def setReloadTimeCountUndefined: Self = StObject.set(x, "reloadTimeCount", js.undefined)
      
      inline def setReloadTimeTotal(value: Double): Self = StObject.set(x, "reloadTimeTotal", value.asInstanceOf[js.Any])
      
      inline def setReloadTimeTotalUndefined: Self = StObject.set(x, "reloadTimeTotal", js.undefined)
    }
  }
}
