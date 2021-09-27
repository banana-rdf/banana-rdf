package types.rdflib

import org.scalablytyped.runtime.NumberDictionary
import types.rdflib.anon.Flags
import types.rdflib.blankNodeMod.default
import types.rdflib.fetcherMod.Handler
import types.rdflib.fetcherMod.Options
import types.rdflib.formulaMod.FormulaOpts
import types.rdflib.parseMod.CallbackFunc
import types.rdflib.rdflibStrings._Colon
import types.rdflib.storeMod.FeaturesType
import types.rdflib.tfTypesMod.DefaultGraph
import types.rdflib.tfTypesMod.RdfJsDataFactory
import types.rdflib.tfTypesMod.Term
import types.rdflib.typesMod.ContentType
import types.rdflib.typesMod.FromValueReturns
import types.rdflib.typesMod.GraphType
import types.rdflib.typesMod.IRDFlibDataFactory
import types.rdflib.typesMod.ObjectType
import types.rdflib.typesMod.PredicateType
import types.rdflib.typesMod.SubjectType
import types.rdflib.typesMod.ValueType
import types.std.Number
import types.std.Record
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object mod {
  
  @JSImport("rdflib", JSImport.Namespace)
  @js.native
  val ^ : js.Any = js.native
  
  @JSImport("rdflib", "BlankNode")
  @js.native
  /**
    * Initializes this node
    * @param [id] The identifier for the blank node
    */
  class BlankNode () extends default {
    def this(id: String) = this()
    def this(id: js.Any) = this()
  }
  /* static members */
  object BlankNode {
    
    @JSImport("rdflib", "BlankNode")
    @js.native
    val ^ : js.Any = js.native
    
    @JSImport("rdflib", "BlankNode.NTAnonymousNodePrefix")
    @js.native
    def NTAnonymousNodePrefix: _Colon = js.native
    inline def NTAnonymousNodePrefix_=(x: _Colon): Unit = ^.asInstanceOf[js.Dynamic].updateDynamic("NTAnonymousNodePrefix")(x.asInstanceOf[js.Any])
    
    @JSImport("rdflib", "BlankNode.getId")
    @js.native
    def getId: js.Any = js.native
    inline def getId_=(x: js.Any): Unit = ^.asInstanceOf[js.Dynamic].updateDynamic("getId")(x.asInstanceOf[js.Any])
    
    /**
      * The next unique identifier for blank nodes
      */
    @JSImport("rdflib", "BlankNode.nextId")
    @js.native
    def nextId: Double = js.native
    inline def nextId_=(x: Double): Unit = ^.asInstanceOf[js.Dynamic].updateDynamic("nextId")(x.asInstanceOf[js.Any])
  }
  
  @JSImport("rdflib", "Collection")
  @js.native
  class Collection[T /* <: types.rdflib.nodeInternalMod.default */] ()
    extends types.rdflib.collectionMod.default[T] {
    def this(initial: js.Array[ValueType]) = this()
  }
  /* static members */
  object Collection {
    
    @JSImport("rdflib", "Collection")
    @js.native
    val ^ : js.Any = js.native
    
    @JSImport("rdflib", "Collection.termType")
    @js.native
    def termType: types.rdflib.rdflibStrings.Collection = js.native
    inline def termType_=(x: types.rdflib.rdflibStrings.Collection): Unit = ^.asInstanceOf[js.Dynamic].updateDynamic("termType")(x.asInstanceOf[js.Any])
    
    inline def toNT(collection: js.Any): String = ^.asInstanceOf[js.Dynamic].applyDynamic("toNT")(collection.asInstanceOf[js.Any]).asInstanceOf[String]
  }
  
  /** Full RDFLib.js Data Factory */
  @JSImport("rdflib", "DataFactory")
  @js.native
  val DataFactory: IRDFlibDataFactory = js.native
  
  @JSImport("rdflib", "Empty")
  @js.native
  class Empty ()
    extends types.rdflib.emptyMod.default
  
  @JSImport("rdflib", "Fetcher")
  @js.native
  class Fetcher_ protected ()
    extends types.rdflib.fetcherMod.default {
    def this(store: types.rdflib.storeMod.default) = this()
    def this(store: types.rdflib.storeMod.default, options: Options) = this()
  }
  /* static members */
  object Fetcher_ {
    
    @JSImport("rdflib", "Fetcher")
    @js.native
    val ^ : js.Any = js.native
    
    @JSImport("rdflib", "Fetcher.CONTENT_TYPE_BY_EXT")
    @js.native
    def CONTENT_TYPE_BY_EXT: Record[String, String] = js.native
    inline def CONTENT_TYPE_BY_EXT_=(x: Record[String, String]): Unit = ^.asInstanceOf[js.Dynamic].updateDynamic("CONTENT_TYPE_BY_EXT")(x.asInstanceOf[js.Any])
    
    @JSImport("rdflib", "Fetcher.HANDLERS")
    @js.native
    def HANDLERS: NumberDictionary[Handler] = js.native
    inline def HANDLERS_=(x: NumberDictionary[Handler]): Unit = ^.asInstanceOf[js.Dynamic].updateDynamic("HANDLERS")(x.asInstanceOf[js.Any])
    
    inline def crossSiteProxy(uri: String): js.UndefOr[js.Any] = ^.asInstanceOf[js.Dynamic].applyDynamic("crossSiteProxy")(uri.asInstanceOf[js.Any]).asInstanceOf[js.UndefOr[js.Any]]
    
    @JSImport("rdflib", "Fetcher.crossSiteProxyTemplate")
    @js.native
    def crossSiteProxyTemplate: js.Any = js.native
    inline def crossSiteProxyTemplate_=(x: js.Any): Unit = ^.asInstanceOf[js.Dynamic].updateDynamic("crossSiteProxyTemplate")(x.asInstanceOf[js.Any])
    
    inline def offlineOverride(uri: String): String = ^.asInstanceOf[js.Dynamic].applyDynamic("offlineOverride")(uri.asInstanceOf[js.Any]).asInstanceOf[String]
    
    inline def proxyIfNecessary(uri: String): js.Any = ^.asInstanceOf[js.Dynamic].applyDynamic("proxyIfNecessary")(uri.asInstanceOf[js.Any]).asInstanceOf[js.Any]
    
    /** Decide on credentials using old XXHR api or new fetch()  one
      * @param requestedURI
      * @param options
      */
    inline def setCredentials(requestedURI: String): Unit = ^.asInstanceOf[js.Dynamic].applyDynamic("setCredentials")(requestedURI.asInstanceOf[js.Any]).asInstanceOf[Unit]
    inline def setCredentials(requestedURI: String, options: Options): Unit = (^.asInstanceOf[js.Dynamic].applyDynamic("setCredentials")(requestedURI.asInstanceOf[js.Any], options.asInstanceOf[js.Any])).asInstanceOf[Unit]
    
    /**
      * Tests whether the uri's protocol is supported by the Fetcher.
      * @param uri
      */
    inline def unsupportedProtocol(uri: String): Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("unsupportedProtocol")(uri.asInstanceOf[js.Any]).asInstanceOf[Boolean]
  }
  
  @JSImport("rdflib", "Formula")
  @js.native
  /**
    * Initializes this formula
    * @constructor
    * @param statements - Initial array of statements
    * @param constraints - initial array of constraints
    * @param initBindings - initial bindings used in Query
    * @param optional - optional
    * @param opts
    * @param opts.rdfFactory - The rdf factory that should be used by the store
    */
  class Formula ()
    extends types.rdflib.formulaMod.default {
    def this(statements: js.Array[
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
  
  @JSImport("rdflib", "IndexedFormula")
  @js.native
  /**
    * Creates a new formula
    * @param features - What sort of automatic processing to do? Array of string
    * @param features.sameAs - Smush together A and B nodes whenever { A sameAs B }
    * @param opts
    * @param [opts.rdfFactory] - The data factory that should be used by the store
    * @param [opts.rdfArrayRemove] - Function which removes statements from the store
    * @param [opts.dataCallback] - Callback when a statement is added to the store, will not trigger when adding duplicates
    */
  class IndexedFormula ()
    extends types.rdflib.storeMod.default {
    def this(features: FeaturesType) = this()
    def this(features: Unit, opts: FormulaOpts) = this()
    def this(features: FeaturesType, opts: FormulaOpts) = this()
  }
  /* static members */
  object IndexedFormula {
    
    @JSImport("rdflib", "IndexedFormula")
    @js.native
    val ^ : js.Any = js.native
    
    @JSImport("rdflib", "IndexedFormula.handleRDFType")
    @js.native
    def handleRDFType: js.Function = js.native
    inline def handleRDFType_=(x: js.Function): Unit = ^.asInstanceOf[js.Dynamic].updateDynamic("handleRDFType")(x.asInstanceOf[js.Any])
  }
  
  @JSImport("rdflib", "Literal")
  @js.native
  class Literal protected ()
    extends types.rdflib.literalMod.default {
    /**
      * Initializes a literal
      * @param value - The literal's lexical value
      * @param language - The language for the literal. Defaults to ''.
      * @param datatype - The literal's datatype as a named node. Defaults to xsd:string.
      */
    def this(value: String) = this()
    def this(value: String, language: String) = this()
    def this(value: String, language: String, datatype: js.Any) = this()
    def this(value: String, language: Null, datatype: js.Any) = this()
    def this(value: String, language: Unit, datatype: js.Any) = this()
  }
  /* static members */
  object Literal {
    
    @JSImport("rdflib", "Literal")
    @js.native
    val ^ : js.Any = js.native
    
    /**
      * Builds a literal node from a boolean value
      * @param value - The value
      */
    inline def fromBoolean(value: Boolean): types.rdflib.literalMod.Literal = ^.asInstanceOf[js.Dynamic].applyDynamic("fromBoolean")(value.asInstanceOf[js.Any]).asInstanceOf[types.rdflib.literalMod.Literal]
    
    /**
      * Builds a literal node from a date value
      * @param value The value
      */
    inline def fromDate(value: js.Date): types.rdflib.literalMod.Literal = ^.asInstanceOf[js.Dynamic].applyDynamic("fromDate")(value.asInstanceOf[js.Any]).asInstanceOf[types.rdflib.literalMod.Literal]
    
    /**
      * Builds a literal node from a number value
      * @param value - The value
      */
    inline def fromNumber(value: Double): types.rdflib.literalMod.Literal = ^.asInstanceOf[js.Dynamic].applyDynamic("fromNumber")(value.asInstanceOf[js.Any]).asInstanceOf[types.rdflib.literalMod.Literal]
    
    /**
      * Builds a literal node from an input value
      * @param value - The input value
      */
    inline def fromValue[T /* <: FromValueReturns[js.Any] */](value: ValueType): T = ^.asInstanceOf[js.Dynamic].applyDynamic("fromValue")(value.asInstanceOf[js.Any]).asInstanceOf[T]
    
    /** Serializes a literal to an N-Triples string */
    inline def toNT(literal: types.rdflib.literalMod.Literal): String = ^.asInstanceOf[js.Dynamic].applyDynamic("toNT")(literal.asInstanceOf[js.Any]).asInstanceOf[String]
  }
  
  @JSImport("rdflib", "NamedNode")
  @js.native
  class NamedNode protected ()
    extends types.rdflib.namedNodeMod.default {
    /**
      * Create a named (IRI) RDF Node
      * @constructor
      * @param iri - The IRI for this node
      */
    def this(iri: String) = this()
  }
  /* static members */
  object NamedNode {
    
    @JSImport("rdflib", "NamedNode")
    @js.native
    val ^ : js.Any = js.native
    
    /**
      * Creates a named node from the specified input value
      * @param value - An input value
      */
    inline def fromValue(value: js.Any): js.Any = ^.asInstanceOf[js.Dynamic].applyDynamic("fromValue")(value.asInstanceOf[js.Any]).asInstanceOf[js.Any]
  }
  
  inline def Namespace(nsuri: String): js.Function1[/* ln */ String, types.rdflib.tfTypesMod.NamedNode] = ^.asInstanceOf[js.Dynamic].applyDynamic("Namespace")(nsuri.asInstanceOf[js.Any]).asInstanceOf[js.Function1[/* ln */ String, types.rdflib.tfTypesMod.NamedNode]]
  inline def Namespace(nsuri: String, factory: RdfJsDataFactory): js.Function1[/* ln */ String, types.rdflib.tfTypesMod.NamedNode] = (^.asInstanceOf[js.Dynamic].applyDynamic("Namespace")(nsuri.asInstanceOf[js.Any], factory.asInstanceOf[js.Any])).asInstanceOf[js.Function1[/* ln */ String, types.rdflib.tfTypesMod.NamedNode]]
  
  @JSImport("rdflib", "NextId")
  @js.native
  val NextId: Double = js.native
  
  @JSImport("rdflib", "Node")
  @js.native
  abstract class Node protected ()
    extends types.rdflib.nodeMod.default {
    /* protected */ def this(value: String) = this()
  }
  /* static members */
  object Node {
    
    @JSImport("rdflib", "Node")
    @js.native
    val ^ : js.Any = js.native
    
    inline def fromValue[T /* <: FromValueReturns[js.Any] */](value: ValueType): T = ^.asInstanceOf[js.Dynamic].applyDynamic("fromValue")(value.asInstanceOf[js.Any]).asInstanceOf[T]
    
    inline def toJS(term: js.Any): js.Date | Number | String | Boolean | js.Object | (js.Array[js.Date | Number | String | Boolean | js.Object]) = ^.asInstanceOf[js.Dynamic].applyDynamic("toJS")(term.asInstanceOf[js.Any]).asInstanceOf[js.Date | Number | String | Boolean | js.Object | (js.Array[js.Date | Number | String | Boolean | js.Object])]
  }
  
  @JSImport("rdflib", "Statement")
  @js.native
  class Statement[S /* <: SubjectType */, P /* <: PredicateType */, O /* <: ObjectType */, G /* <: GraphType */] protected ()
    extends types.rdflib.statementMod.default[S, P, O, G] {
    /**
      * Construct a new statement
      *
      * @param subject - The subject of the triple.  What the fact is about
      * @param predicate - The relationship which is asserted between the subject and object
      * @param object - The thing or data value which is asserted to be related to the subject
      * @param {NamedNode} graph - The document where the triple is or was or will be stored on the web.
      *
      * The graph param is a named node of the document in which the triple when it is stored
      *  on the web. It exists because when you have read data from various places in the web,
      *  the “graph” tells you _why_ you have the triple. (At the moment, it is just the
      *  document, in future it could be an inference step)
      *
      * When you do UpdateManager.update() then the graph’s of all the statements must be the same,
      *  and give the document you are patching. In future, we may have a more
      *  powerful update() which can update more than one document.
      */
    def this(subject: S, predicate: P, `object`: O) = this()
    def this(subject: S, predicate: P, `object`: O, graph: G) = this()
    def this(subject: S, predicate: P, `object`: O, graph: DefaultGraph) = this()
  }
  
  @JSImport("rdflib", "Store")
  @js.native
  /**
    * Creates a new formula
    * @param features - What sort of automatic processing to do? Array of string
    * @param features.sameAs - Smush together A and B nodes whenever { A sameAs B }
    * @param opts
    * @param [opts.rdfFactory] - The data factory that should be used by the store
    * @param [opts.rdfArrayRemove] - Function which removes statements from the store
    * @param [opts.dataCallback] - Callback when a statement is added to the store, will not trigger when adding duplicates
    */
  class Store ()
    extends types.rdflib.storeMod.default {
    def this(features: FeaturesType) = this()
    def this(features: Unit, opts: FormulaOpts) = this()
    def this(features: FeaturesType, opts: FormulaOpts) = this()
  }
  /* static members */
  object Store {
    
    @JSImport("rdflib", "Store")
    @js.native
    val ^ : js.Any = js.native
    
    @JSImport("rdflib", "Store.handleRDFType")
    @js.native
    def handleRDFType: js.Function = js.native
    inline def handleRDFType_=(x: js.Function): Unit = ^.asInstanceOf[js.Dynamic].updateDynamic("handleRDFType")(x.asInstanceOf[js.Any])
  }
  
  @JSImport("rdflib", "UpdateManager")
  @js.native
  /**
    * @param  store - The quadstore to store data and metadata. Created if not passed.
    */
  class UpdateManager ()
    extends types.rdflib.updateManagerMod.default {
    def this(store: types.rdflib.storeMod.default) = this()
  }
  
  @JSImport("rdflib", "Variable")
  @js.native
  /**
    * Initializes this variable
    * @param name The variable's name
    */
  class Variable ()
    extends types.rdflib.variableMod.default {
    def this(name: String) = this()
  }
  /* static members */
  object Variable {
    
    @JSImport("rdflib", "Variable")
    @js.native
    val ^ : js.Any = js.native
    
    inline def toString(variable: js.Any): String = ^.asInstanceOf[js.Dynamic].applyDynamic("toString")(variable.asInstanceOf[js.Any]).asInstanceOf[String]
  }
  
  /* import warning: parser.TsParser#tsDeclVar Dropped IArray(graph, lit, st, namedNode, variable, blankNode, defaultGraph, literal, quad, triple) */ inline def fetcher(store: types.rdflib.storeMod.default, options: js.Any): types.rdflib.fetcherMod.default = (^.asInstanceOf[js.Dynamic].applyDynamic("fetcher")(store.asInstanceOf[js.Any], options.asInstanceOf[js.Any])).asInstanceOf[types.rdflib.fetcherMod.default]
  
  inline def fromNT(str: js.Any): js.Any = ^.asInstanceOf[js.Dynamic].applyDynamic("fromNT")(str.asInstanceOf[js.Any]).asInstanceOf[js.Any]
  
  inline def isBlankNode(obj: js.Any): /* is rdflib.rdflib/lib/tf-types.BlankNode */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isBlankNode")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/tf-types.BlankNode */ Boolean]
  
  inline def isCollection(obj: js.Any): /* is rdflib.rdflib/lib/collection.default<any> */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isCollection")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/collection.default<any> */ Boolean]
  
  inline def isGraph(obj: js.Any): /* is rdflib.rdflib/lib/tf-types.Quad_Graph */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isGraph")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/tf-types.Quad_Graph */ Boolean]
  
  inline def isLiteral(value: js.Any): /* is rdflib.rdflib/lib/tf-types.Literal */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isLiteral")(value.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/tf-types.Literal */ Boolean]
  
  inline def isNamedNode(obj: js.Any): /* is rdflib.rdflib/lib/tf-types.NamedNode */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isNamedNode")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/tf-types.NamedNode */ Boolean]
  
  inline def isPredicate(obj: js.Any): /* is rdflib.rdflib/lib/tf-types.Quad_Predicate */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isPredicate")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/tf-types.Quad_Predicate */ Boolean]
  
  inline def isQuad(obj: js.Any): /* is rdflib.rdflib/lib/tf-types.Quad<any, any, any, any> */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isQuad")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/tf-types.Quad<any, any, any, any> */ Boolean]
  
  inline def isRDFObject(obj: js.Any): /* is rdflib.rdflib/lib/tf-types.Quad_Object */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isRDFObject")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/tf-types.Quad_Object */ Boolean]
  
  inline def isRDFlibObject(obj: js.Any): /* is rdflib.rdflib/lib/types.ObjectType */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isRDFlibObject")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/types.ObjectType */ Boolean]
  
  inline def isStatement(obj: js.Any): /* is rdflib.rdflib/lib/statement.default<rdflib.rdflib/lib/types.SubjectType, rdflib.rdflib/lib/types.PredicateType, rdflib.rdflib/lib/types.ObjectType, rdflib.rdflib/lib/types.GraphType> */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isStatement")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/statement.default<rdflib.rdflib/lib/types.SubjectType, rdflib.rdflib/lib/types.PredicateType, rdflib.rdflib/lib/types.ObjectType, rdflib.rdflib/lib/types.GraphType> */ Boolean]
  
  inline def isStore(obj: js.Any): /* is rdflib.rdflib/lib/store.default */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isStore")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/store.default */ Boolean]
  
  inline def isSubject(obj: js.Any): /* is rdflib.rdflib/lib/tf-types.Quad_Subject */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isSubject")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/tf-types.Quad_Subject */ Boolean]
  
  inline def isTerm(obj: js.Any): /* is rdflib.rdflib/lib/tf-types.Term */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isTerm")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/tf-types.Term */ Boolean]
  
  inline def isVariable(obj: js.Any): /* is rdflib.rdflib/lib/tf-types.Variable */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isVariable")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/tf-types.Variable */ Boolean]
  
  inline def parse(str: String, kb: types.rdflib.formulaMod.default, base: String): Unit = (^.asInstanceOf[js.Dynamic].applyDynamic("parse")(str.asInstanceOf[js.Any], kb.asInstanceOf[js.Any], base.asInstanceOf[js.Any])).asInstanceOf[Unit]
  inline def parse(str: String, kb: types.rdflib.formulaMod.default, base: String, contentType: String): Unit = (^.asInstanceOf[js.Dynamic].applyDynamic("parse")(str.asInstanceOf[js.Any], kb.asInstanceOf[js.Any], base.asInstanceOf[js.Any], contentType.asInstanceOf[js.Any])).asInstanceOf[Unit]
  inline def parse(
    str: String,
    kb: types.rdflib.formulaMod.default,
    base: String,
    contentType: String,
    callback: CallbackFunc
  ): Unit = (^.asInstanceOf[js.Dynamic].applyDynamic("parse")(str.asInstanceOf[js.Any], kb.asInstanceOf[js.Any], base.asInstanceOf[js.Any], contentType.asInstanceOf[js.Any], callback.asInstanceOf[js.Any])).asInstanceOf[Unit]
  inline def parse(
    str: String,
    kb: types.rdflib.formulaMod.default,
    base: String,
    contentType: Unit,
    callback: CallbackFunc
  ): Unit = (^.asInstanceOf[js.Dynamic].applyDynamic("parse")(str.asInstanceOf[js.Any], kb.asInstanceOf[js.Any], base.asInstanceOf[js.Any], contentType.asInstanceOf[js.Any], callback.asInstanceOf[js.Any])).asInstanceOf[Unit]
  inline def parse(str: String, kb: types.rdflib.formulaMod.default, base: String, contentType: ContentType): Unit = (^.asInstanceOf[js.Dynamic].applyDynamic("parse")(str.asInstanceOf[js.Any], kb.asInstanceOf[js.Any], base.asInstanceOf[js.Any], contentType.asInstanceOf[js.Any])).asInstanceOf[Unit]
  inline def parse(
    str: String,
    kb: types.rdflib.formulaMod.default,
    base: String,
    contentType: ContentType,
    callback: CallbackFunc
  ): Unit = (^.asInstanceOf[js.Dynamic].applyDynamic("parse")(str.asInstanceOf[js.Any], kb.asInstanceOf[js.Any], base.asInstanceOf[js.Any], contentType.asInstanceOf[js.Any], callback.asInstanceOf[js.Any])).asInstanceOf[Unit]
  
  inline def serialize(
    /** The graph or nodes that should be serialized */
  target: types.rdflib.formulaMod.default | types.rdflib.tfTypesMod.NamedNode | types.rdflib.tfTypesMod.BlankNode,
    /** The store */
  kb: js.UndefOr[types.rdflib.storeMod.default],
    base: js.UndefOr[js.Any],
    /**
    * The mime type.
    * Defaults to Turtle.
    */
  contentType: js.UndefOr[String | ContentType],
    callback: js.UndefOr[
      js.Function2[
        /* err */ js.UndefOr[js.Error | Null], 
        /* result */ js.UndefOr[String | Null], 
        js.Any
      ]
    ],
    options: js.UndefOr[Flags]
  ): js.UndefOr[String] = (^.asInstanceOf[js.Dynamic].applyDynamic("serialize")(target.asInstanceOf[js.Any], kb.asInstanceOf[js.Any], base.asInstanceOf[js.Any], contentType.asInstanceOf[js.Any], callback.asInstanceOf[js.Any], options.asInstanceOf[js.Any])).asInstanceOf[js.UndefOr[String]]
  
  inline def term[T /* <: FromValueReturns[js.Any] */](value: ValueType): T = ^.asInstanceOf[js.Dynamic].applyDynamic("term")(value.asInstanceOf[js.Any]).asInstanceOf[T]
  
  inline def termValue(node: String): String = ^.asInstanceOf[js.Dynamic].applyDynamic("termValue")(node.asInstanceOf[js.Any]).asInstanceOf[String]
  inline def termValue(node: Term): String = ^.asInstanceOf[js.Dynamic].applyDynamic("termValue")(node.asInstanceOf[js.Any]).asInstanceOf[String]
  
  object uri {
    
    @JSImport("rdflib", "uri")
    @js.native
    val ^ : js.Any = js.native
    
    inline def docpart(uri: String): String = ^.asInstanceOf[js.Dynamic].applyDynamic("docpart")(uri.asInstanceOf[js.Any]).asInstanceOf[String]
    
    inline def document(x: String): types.rdflib.namedNodeMod.default = ^.asInstanceOf[js.Dynamic].applyDynamic("document")(x.asInstanceOf[js.Any]).asInstanceOf[types.rdflib.namedNodeMod.default]
    
    inline def hostpart(u: String): String = ^.asInstanceOf[js.Dynamic].applyDynamic("hostpart")(u.asInstanceOf[js.Any]).asInstanceOf[String]
    
    inline def join(`given`: String, base: String): String = (^.asInstanceOf[js.Dynamic].applyDynamic("join")(`given`.asInstanceOf[js.Any], base.asInstanceOf[js.Any])).asInstanceOf[String]
    
    inline def protocol(uri: String): String | Null = ^.asInstanceOf[js.Dynamic].applyDynamic("protocol")(uri.asInstanceOf[js.Any]).asInstanceOf[String | Null]
    
    inline def refTo(base: String, uri: String): String = (^.asInstanceOf[js.Dynamic].applyDynamic("refTo")(base.asInstanceOf[js.Any], uri.asInstanceOf[js.Any])).asInstanceOf[String]
  }
}
