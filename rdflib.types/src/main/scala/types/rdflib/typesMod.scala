package types.rdflib

import org.scalablytyped.runtime.StringDictionary
import types.rdflib.factoryTypesMod.DataFactory
import types.rdflib.factoryTypesMod.Indexable
import types.rdflib.nodeInternalMod.default
import types.rdflib.rdflibStrings.BlankNode
import types.rdflib.rdflibStrings.Collection
import types.rdflib.rdflibStrings.DefaultGraph
import types.rdflib.rdflibStrings.Empty
import types.rdflib.rdflibStrings.Graph
import types.rdflib.rdflibStrings.Literal
import types.rdflib.rdflibStrings.NamedNode
import types.rdflib.rdflibStrings.Variable
import types.rdflib.rdflibStrings.`applicationSlashn-quads`
import types.rdflib.rdflibStrings.`applicationSlashn-triples`
import types.rdflib.rdflibStrings.`applicationSlashsparql-update-single-match`
import types.rdflib.rdflibStrings.`applicationSlashsparql-update`
import types.rdflib.rdflibStrings.`applicationSlashx-turtle`
import types.rdflib.rdflibStrings.applicationSlashldPlussignjson
import types.rdflib.rdflibStrings.applicationSlashn3
import types.rdflib.rdflibStrings.applicationSlashnquads
import types.rdflib.rdflibStrings.applicationSlashrdfPlussignxml
import types.rdflib.rdflibStrings.applicationSlashxhtmlPlussignxml
import types.rdflib.rdflibStrings.textSlashhtml
import types.rdflib.rdflibStrings.textSlashn3
import types.rdflib.rdflibStrings.textSlashturtle
import types.rdflib.tfTypesMod.QuadGraph
import types.rdflib.tfTypesMod.QuadObject
import types.rdflib.tfTypesMod.QuadPredicate
import types.rdflib.tfTypesMod.QuadSubject
import types.rdflib.tfTypesMod.Term
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object typesMod {
  
  @JSImport("rdflib/lib/types", "BlankNodeTermType")
  @js.native
  val BlankNodeTermType: BlankNode = js.native
  
  @JSImport("rdflib/lib/types", "CollectionTermType")
  @js.native
  val CollectionTermType: Collection = js.native
  
  @JSImport("rdflib/lib/types", "DefaultGraphTermType")
  @js.native
  val DefaultGraphTermType: DefaultGraph = js.native
  
  @JSImport("rdflib/lib/types", "EmptyTermType")
  @js.native
  val EmptyTermType: Empty = js.native
  
  @JSImport("rdflib/lib/types", "GraphTermType")
  @js.native
  val GraphTermType: Graph = js.native
  
  @JSImport("rdflib/lib/types", "HTMLContentType")
  @js.native
  val HTMLContentType: textSlashhtml = js.native
  
  @JSImport("rdflib/lib/types", "JSONLDContentType")
  @js.native
  val JSONLDContentType: applicationSlashldPlussignjson = js.native
  
  @JSImport("rdflib/lib/types", "LiteralTermType")
  @js.native
  val LiteralTermType: Literal = js.native
  
  @JSImport("rdflib/lib/types", "N3ContentType")
  @js.native
  val N3ContentType: textSlashn3 = js.native
  
  @JSImport("rdflib/lib/types", "N3LegacyContentType")
  @js.native
  val N3LegacyContentType: applicationSlashn3 = js.native
  
  @JSImport("rdflib/lib/types", "NQuadsAltContentType")
  @js.native
  val NQuadsAltContentType: applicationSlashnquads = js.native
  
  @JSImport("rdflib/lib/types", "NQuadsContentType")
  @js.native
  val NQuadsContentType: `applicationSlashn-quads` = js.native
  
  @JSImport("rdflib/lib/types", "NTriplesContentType")
  @js.native
  val NTriplesContentType: `applicationSlashn-triples` = js.native
  
  @JSImport("rdflib/lib/types", "NamedNodeTermType")
  @js.native
  val NamedNodeTermType: NamedNode = js.native
  
  @JSImport("rdflib/lib/types", "RDFXMLContentType")
  @js.native
  val RDFXMLContentType: applicationSlashrdfPlussignxml = js.native
  
  @JSImport("rdflib/lib/types", "SPARQLUpdateContentType")
  @js.native
  val SPARQLUpdateContentType: `applicationSlashsparql-update` = js.native
  
  @JSImport("rdflib/lib/types", "SPARQLUpdateSingleMatchContentType")
  @js.native
  val SPARQLUpdateSingleMatchContentType: `applicationSlashsparql-update-single-match` = js.native
  
  @JSImport("rdflib/lib/types", "TurtleContentType")
  @js.native
  val TurtleContentType: textSlashturtle = js.native
  
  @JSImport("rdflib/lib/types", "TurtleLegacyContentType")
  @js.native
  val TurtleLegacyContentType: `applicationSlashx-turtle` = js.native
  
  @JSImport("rdflib/lib/types", "VariableTermType")
  @js.native
  val VariableTermType: Variable = js.native
  
  @JSImport("rdflib/lib/types", "XHTMLContentType")
  @js.native
  val XHTMLContentType: applicationSlashxhtmlPlussignxml = js.native
  
  type Bindings = StringDictionary[Term]
  
  /* Rewritten from type alias, can be one of: 
    - types.rdflib.rdflibStrings.applicationSlashrdfPlussignxml
    - types.rdflib.rdflibStrings.textSlashhtml
    - types.rdflib.rdflibStrings.applicationSlashldPlussignjson
    - types.rdflib.rdflibStrings.textSlashn3
    - types.rdflib.rdflibStrings.applicationSlashn3
    - types.rdflib.rdflibStrings.applicationSlashnquads
    - types.rdflib.rdflibStrings.`applicationSlashn-quads`
    - types.rdflib.rdflibStrings.`applicationSlashsparql-update`
    - types.rdflib.rdflibStrings.`applicationSlashsparql-update-single-match`
    - types.rdflib.rdflibStrings.textSlashturtle
    - types.rdflib.rdflibStrings.`applicationSlashx-turtle`
    - types.rdflib.rdflibStrings.applicationSlashxhtmlPlussignxml
  */
  trait ContentType extends StObject
  object ContentType {
    
    inline def applicationSlashldPlussignjson: types.rdflib.rdflibStrings.applicationSlashldPlussignjson = "application/ld+json".asInstanceOf[types.rdflib.rdflibStrings.applicationSlashldPlussignjson]
    
    inline def `applicationSlashn-quads`: types.rdflib.rdflibStrings.`applicationSlashn-quads` = "application/n-quads".asInstanceOf[types.rdflib.rdflibStrings.`applicationSlashn-quads`]
    
    inline def applicationSlashn3: types.rdflib.rdflibStrings.applicationSlashn3 = "application/n3".asInstanceOf[types.rdflib.rdflibStrings.applicationSlashn3]
    
    inline def applicationSlashnquads: types.rdflib.rdflibStrings.applicationSlashnquads = "application/nquads".asInstanceOf[types.rdflib.rdflibStrings.applicationSlashnquads]
    
    inline def applicationSlashrdfPlussignxml: types.rdflib.rdflibStrings.applicationSlashrdfPlussignxml = "application/rdf+xml".asInstanceOf[types.rdflib.rdflibStrings.applicationSlashrdfPlussignxml]
    
    inline def `applicationSlashsparql-update`: types.rdflib.rdflibStrings.`applicationSlashsparql-update` = "application/sparql-update".asInstanceOf[types.rdflib.rdflibStrings.`applicationSlashsparql-update`]
    
    inline def `applicationSlashsparql-update-single-match`: types.rdflib.rdflibStrings.`applicationSlashsparql-update-single-match` = "application/sparql-update-single-match".asInstanceOf[types.rdflib.rdflibStrings.`applicationSlashsparql-update-single-match`]
    
    inline def `applicationSlashx-turtle`: types.rdflib.rdflibStrings.`applicationSlashx-turtle` = "application/x-turtle".asInstanceOf[types.rdflib.rdflibStrings.`applicationSlashx-turtle`]
    
    inline def applicationSlashxhtmlPlussignxml: types.rdflib.rdflibStrings.applicationSlashxhtmlPlussignxml = "application/xhtml+xml".asInstanceOf[types.rdflib.rdflibStrings.applicationSlashxhtmlPlussignxml]
    
    inline def textSlashhtml: types.rdflib.rdflibStrings.textSlashhtml = "text/html".asInstanceOf[types.rdflib.rdflibStrings.textSlashhtml]
    
    inline def textSlashn3: types.rdflib.rdflibStrings.textSlashn3 = "text/n3".asInstanceOf[types.rdflib.rdflibStrings.textSlashn3]
    
    inline def textSlashturtle: types.rdflib.rdflibStrings.textSlashturtle = "text/turtle".asInstanceOf[types.rdflib.rdflibStrings.textSlashturtle]
  }
  
  /* Rewritten from type alias, can be one of: 
    - types.rdflib.tfTypesMod.Term
    - scala.Unit
    - scala.Null
    - types.rdflib.collectionMod.default[C]
  */
  type FromValueReturns[C /* <: default */] = js.UndefOr[_FromValueReturns[C] | Null]
  
  /* Rewritten from type alias, can be one of: 
    - types.rdflib.defaultGraphMod.default
    - types.rdflib.namedNodeMod.default
    - types.rdflib.variableMod.default
  */
  trait GraphType extends StObject
  
  @js.native
  trait IRDFlibDataFactory
    extends StObject
       with DataFactory[
          types.rdflib.namedNodeMod.default | types.rdflib.blankNodeMod.default | types.rdflib.literalMod.default | (types.rdflib.collectionMod.default[
            default | types.rdflib.blankNodeMod.default | types.rdflib.collectionMod.Collection[js.Any] | types.rdflib.literalMod.default | types.rdflib.variableMod.default
          ]) | (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]), 
          Indexable
        ] {
    
    def fetcher(store: types.rdflib.storeMod.default, options: js.Any): types.rdflib.fetcherMod.default = js.native
    
    def graph(): types.rdflib.storeMod.default = js.native
    def graph(features: js.Any): types.rdflib.storeMod.default = js.native
    def graph(features: js.Any, opts: js.Any): types.rdflib.storeMod.default = js.native
    def graph(features: Unit, opts: js.Any): types.rdflib.storeMod.default = js.native
    
    def lit(`val`: String): types.rdflib.literalMod.default = js.native
    def lit(`val`: String, lang: String): types.rdflib.literalMod.default = js.native
    def lit(`val`: String, lang: String, dt: types.rdflib.tfTypesMod.NamedNode): types.rdflib.literalMod.default = js.native
    def lit(`val`: String, lang: Unit, dt: types.rdflib.tfTypesMod.NamedNode): types.rdflib.literalMod.default = js.native
    
    def st(subject: QuadSubject, predicate: QuadPredicate, `object`: QuadObject): types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType] = js.native
    def st(subject: QuadSubject, predicate: QuadPredicate, `object`: QuadObject, graph: QuadGraph): types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType] = js.native
  }
  
  /* Rewritten from type alias, can be one of: 
    - types.rdflib.namedNodeMod.default
    - types.rdflib.literalMod.default
    - types.rdflib.collectionMod.default[
  types.rdflib.nodeInternalMod.default | types.rdflib.blankNodeMod.default | types.rdflib.collectionMod.Collection[js.Any] | types.rdflib.literalMod.default | types.rdflib.variableMod.default]
    - types.rdflib.blankNodeMod.default
    - types.rdflib.variableMod.default
    - types.rdflib.emptyMod.default
  */
  type ObjectType = _ObjectType | (types.rdflib.collectionMod.default[
    default | types.rdflib.blankNodeMod.default | types.rdflib.collectionMod.Collection[js.Any] | types.rdflib.literalMod.default | types.rdflib.variableMod.default
  ])
  
  /* Rewritten from type alias, can be one of: 
    - types.rdflib.namedNodeMod.default
    - types.rdflib.variableMod.default
  */
  trait PredicateType extends StObject
  
  /* Rewritten from type alias, can be one of: 
    - types.rdflib.blankNodeMod.default
    - types.rdflib.namedNodeMod.default
    - types.rdflib.variableMod.default
  */
  trait SubjectType extends StObject
  
  /* Rewritten from type alias, can be one of: 
    - types.rdflib.rdflibStrings.NamedNode
    - types.rdflib.rdflibStrings.BlankNode
    - types.rdflib.rdflibStrings.Literal
    - types.rdflib.rdflibStrings.Variable
    - types.rdflib.rdflibStrings.DefaultGraph
    - types.rdflib.rdflibStrings.Collection
    - types.rdflib.rdflibStrings.Empty
    - types.rdflib.rdflibStrings.Graph
  */
  trait TermType extends StObject
  object TermType {
    
    inline def BlankNode: types.rdflib.rdflibStrings.BlankNode = "BlankNode".asInstanceOf[types.rdflib.rdflibStrings.BlankNode]
    
    inline def Collection: types.rdflib.rdflibStrings.Collection = "Collection".asInstanceOf[types.rdflib.rdflibStrings.Collection]
    
    inline def DefaultGraph: types.rdflib.rdflibStrings.DefaultGraph = "DefaultGraph".asInstanceOf[types.rdflib.rdflibStrings.DefaultGraph]
    
    inline def Empty: types.rdflib.rdflibStrings.Empty = "Empty".asInstanceOf[types.rdflib.rdflibStrings.Empty]
    
    inline def Graph: types.rdflib.rdflibStrings.Graph = "Graph".asInstanceOf[types.rdflib.rdflibStrings.Graph]
    
    inline def Literal: types.rdflib.rdflibStrings.Literal = "Literal".asInstanceOf[types.rdflib.rdflibStrings.Literal]
    
    inline def NamedNode: types.rdflib.rdflibStrings.NamedNode = "NamedNode".asInstanceOf[types.rdflib.rdflibStrings.NamedNode]
    
    inline def Variable: types.rdflib.rdflibStrings.Variable = "Variable".asInstanceOf[types.rdflib.rdflibStrings.Variable]
  }
  
  /* Rewritten from type alias, can be one of: 
    - types.rdflib.tfTypesMod.Term
    - types.rdflib.nodeInternalMod.default
    - js.Date
    - java.lang.String
    - scala.Double
    - scala.Boolean
    - scala.Unit
    - scala.Null
    - types.rdflib.collectionMod.default[
  types.rdflib.nodeInternalMod.default | types.rdflib.blankNodeMod.default | types.rdflib.collectionMod.Collection[js.Any] | types.rdflib.literalMod.default | types.rdflib.variableMod.default]
  */
  type ValueType = js.UndefOr[
    _ValueType | (types.rdflib.collectionMod.default[
      default | types.rdflib.blankNodeMod.default | types.rdflib.collectionMod.Collection[js.Any] | types.rdflib.literalMod.default | types.rdflib.variableMod.default
    ]) | js.Date | String | Double | Boolean | Null
  ]
  
  trait _FromValueReturns[C /* <: default */] extends StObject
  
  trait _ObjectType extends StObject
  
  trait _ValueType extends StObject
}
