package types.rdflib

import types.rdflib.literalMod.default
import types.rdflib.tfTypesMod.NamedNode
import types.rdflib.tfTypesMod.Quad
import types.rdflib.tfTypesMod.QuadGraph
import types.rdflib.tfTypesMod.QuadObject
import types.rdflib.tfTypesMod.QuadPredicate
import types.rdflib.tfTypesMod.QuadSubject
import types.rdflib.tfTypesMod.RdfJsDataFactory
import types.rdflib.tfTypesMod.Term
import types.rdflib.typesMod.GraphType
import types.rdflib.typesMod.ObjectType
import types.rdflib.typesMod.PredicateType
import types.rdflib.typesMod.SubjectType
import types.std.Record
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object factoryTypesMod {
  
  @js.native
  sealed trait Feature extends StObject
  @JSImport("rdflib/lib/factories/factory-types", "Feature")
  @js.native
  object Feature extends StObject {
    
    @JSBracketAccess
    def apply(value: String): js.UndefOr[Feature & String] = js.native
    
    /** Whether the factory supports termType:Collection terms */
    @js.native
    sealed trait collections
      extends StObject
         with Feature
    /* "COLLECTIONS" */ val collections: types.rdflib.factoryTypesMod.Feature.collections & String = js.native
    
    /** Whether the factory supports termType:DefaultGraph terms */
    @js.native
    sealed trait defaultGraphType
      extends StObject
         with Feature
    /* "DEFAULT_GRAPH_TYPE" */ val defaultGraphType: types.rdflib.factoryTypesMod.Feature.defaultGraphType & String = js.native
    
    /** Whether the factory supports equals on produced instances */
    @js.native
    sealed trait equalsMethod
      extends StObject
         with Feature
    /* "EQUALS_METHOD" */ val equalsMethod: types.rdflib.factoryTypesMod.Feature.equalsMethod & String = js.native
    
    /** Whether the factory can create a unique idempotent identifier for the given term. */
    @js.native
    sealed trait id
      extends StObject
         with Feature
    /* "ID" */ val id: types.rdflib.factoryTypesMod.Feature.id & String = js.native
    
    /**
      * Whether the factory will return the same instance for subsequent calls.
      * This implies `===`, which means methods like `indexOf` can be used.
      */
    @js.native
    sealed trait identity
      extends StObject
         with Feature
    /* "IDENTITY" */ val identity: types.rdflib.factoryTypesMod.Feature.identity & String = js.native
    
    /** Whether the factory supports mapping ids back to instances (should adhere to the identity setting) */
    @js.native
    sealed trait reversibleId
      extends StObject
         with Feature
    /* "REVERSIBLE_ID" */ val reversibleId: types.rdflib.factoryTypesMod.Feature.reversibleId & String = js.native
    
    /** Whether the factory supports termType:Variable terms */
    @js.native
    sealed trait variableType
      extends StObject
         with Feature
    /* "VARIABLE_TYPE" */ val variableType: types.rdflib.factoryTypesMod.Feature.variableType & String = js.native
  }
  
  /* Rewritten from type alias, can be one of: 
    - types.rdflib.tfTypesMod.Term
    - types.rdflib.tfTypesMod.NamedNode
    - types.rdflib.tfTypesMod.BlankNode
    - types.rdflib.tfTypesMod.Literal
    - types.rdflib.tfTypesMod.Quad[
  types.rdflib.tfTypesMod.QuadSubject, 
  types.rdflib.tfTypesMod.QuadPredicate, 
  types.rdflib.tfTypesMod.QuadObject, 
  types.rdflib.tfTypesMod.QuadGraph]
    - scala.Unit
    - scala.Null
  */
  type Comparable = js.UndefOr[_Comparable | (Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]) | Null]
  
  @js.native
  trait DataFactory[FactoryTypes, IndexType]
    extends StObject
       with RdfJsDataFactory {
    
    /**
      * BlankNode index
      * @private
      */
    var bnIndex: js.UndefOr[Double] = js.native
    
    def equals(a: Comparable, b: Comparable): Boolean = js.native
    
    def id(obj: FactoryTypes): IndexType = js.native
    /**
      * Generates a unique session-idempotent identifier for the given object.
      *
      * @example NQ serialization (reversible from value)
      * @example MD5 hash of termType + value (irreversible from value, map needed)
      *
      * @return {Indexable} A unique value which must also be a valid JS object key type.
      */
    def id(obj: Term): IndexType = js.native
    
    def isQuad(obj: js.Any): /* is rdflib.rdflib/lib/statement.default<rdflib.rdflib/lib/types.SubjectType, rdflib.rdflib/lib/types.PredicateType, rdflib.rdflib/lib/types.ObjectType, rdflib.rdflib/lib/types.GraphType> */ Boolean = js.native
    
    def literal(value: String): default = js.native
    
    def quadToNQ(term: types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]): String = js.native
    def quadToNQ(term: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]): String = js.native
    
    def termToNQ(term: Term): String = js.native
    
    def toNQ(term: FactoryTypes): String = js.native
    def toNQ(term: Term): String = js.native
    
    @JSName("variable")
    def variable_MDataFactory(value: String): types.rdflib.variableMod.default = js.native
  }
  
  /* Rewritten from type alias, can be one of: 
    - types.rdflib.namedNodeMod.default
    - types.rdflib.blankNodeMod.default
    - types.rdflib.literalMod.default
    - types.rdflib.variableMod.default
    - types.rdflib.statementMod.default[
  types.rdflib.typesMod.SubjectType, 
  types.rdflib.typesMod.PredicateType, 
  types.rdflib.typesMod.ObjectType, 
  types.rdflib.typesMod.GraphType]
  */
  type DefaultFactoryTypes = _DefaultFactoryTypes | (types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType])
  
  type Indexable = Double | String
  
  type Namespace = js.Function1[/* term */ String, NamedNode]
  
  type SupportTable = Record[Feature, Boolean]
  
  /* Rewritten from type alias, can be one of: 
    - types.rdflib.tfTypesMod.NamedNode
    - types.rdflib.tfTypesMod.BlankNode
    - types.rdflib.tfTypesMod.Literal
    - types.rdflib.tfTypesMod.Quad[
  types.rdflib.tfTypesMod.QuadSubject, 
  types.rdflib.tfTypesMod.QuadPredicate, 
  types.rdflib.tfTypesMod.QuadObject, 
  types.rdflib.tfTypesMod.QuadGraph]
    - types.rdflib.tfTypesMod.Variable
    - types.rdflib.tfTypesMod.Term
  */
  type TFIDFactoryTypes = _TFIDFactoryTypes | (Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph])
  
  trait _Comparable extends StObject
  object _Comparable {
    
    inline def BlankNode(equals_ : Term => Boolean, value: String): types.rdflib.tfTypesMod.BlankNode = {
      val __obj = js.Dynamic.literal(termType = "BlankNode", value = value.asInstanceOf[js.Any])
      __obj.updateDynamic("equals")(js.Any.fromFunction1(equals_))
      __obj.asInstanceOf[types.rdflib.tfTypesMod.BlankNode]
    }
    
    inline def Literal(datatype: NamedNode, equals_ : Term => Boolean, language: String, value: String): types.rdflib.tfTypesMod.Literal = {
      val __obj = js.Dynamic.literal(datatype = datatype.asInstanceOf[js.Any], language = language.asInstanceOf[js.Any], termType = "Literal", value = value.asInstanceOf[js.Any])
      __obj.updateDynamic("equals")(js.Any.fromFunction1(equals_))
      __obj.asInstanceOf[types.rdflib.tfTypesMod.Literal]
    }
    
    inline def NamedNode(equals_ : Term => Boolean, value: String): types.rdflib.tfTypesMod.NamedNode = {
      val __obj = js.Dynamic.literal(termType = "NamedNode", value = value.asInstanceOf[js.Any])
      __obj.updateDynamic("equals")(js.Any.fromFunction1(equals_))
      __obj.asInstanceOf[types.rdflib.tfTypesMod.NamedNode]
    }
    
    inline def Term(equals_ : types.rdflib.tfTypesMod.Term => Boolean, termType: String, value: String): types.rdflib.tfTypesMod.Term = {
      val __obj = js.Dynamic.literal(termType = termType.asInstanceOf[js.Any], value = value.asInstanceOf[js.Any])
      __obj.updateDynamic("equals")(js.Any.fromFunction1(equals_))
      __obj.asInstanceOf[types.rdflib.tfTypesMod.Term]
    }
  }
  
  trait _DefaultFactoryTypes extends StObject
  
  trait _TFIDFactoryTypes extends StObject
  object _TFIDFactoryTypes {
    
    inline def BlankNode(equals_ : Term => Boolean, value: String): types.rdflib.tfTypesMod.BlankNode = {
      val __obj = js.Dynamic.literal(termType = "BlankNode", value = value.asInstanceOf[js.Any])
      __obj.updateDynamic("equals")(js.Any.fromFunction1(equals_))
      __obj.asInstanceOf[types.rdflib.tfTypesMod.BlankNode]
    }
    
    inline def Literal(datatype: NamedNode, equals_ : Term => Boolean, language: String, value: String): types.rdflib.tfTypesMod.Literal = {
      val __obj = js.Dynamic.literal(datatype = datatype.asInstanceOf[js.Any], language = language.asInstanceOf[js.Any], termType = "Literal", value = value.asInstanceOf[js.Any])
      __obj.updateDynamic("equals")(js.Any.fromFunction1(equals_))
      __obj.asInstanceOf[types.rdflib.tfTypesMod.Literal]
    }
    
    inline def NamedNode(equals_ : Term => Boolean, value: String): types.rdflib.tfTypesMod.NamedNode = {
      val __obj = js.Dynamic.literal(termType = "NamedNode", value = value.asInstanceOf[js.Any])
      __obj.updateDynamic("equals")(js.Any.fromFunction1(equals_))
      __obj.asInstanceOf[types.rdflib.tfTypesMod.NamedNode]
    }
    
    inline def Term(equals_ : types.rdflib.tfTypesMod.Term => Boolean, termType: String, value: String): types.rdflib.tfTypesMod.Term = {
      val __obj = js.Dynamic.literal(termType = termType.asInstanceOf[js.Any], value = value.asInstanceOf[js.Any])
      __obj.updateDynamic("equals")(js.Any.fromFunction1(equals_))
      __obj.asInstanceOf[types.rdflib.tfTypesMod.Term]
    }
    
    inline def Variable(equals_ : Term => Boolean, value: String): types.rdflib.tfTypesMod.Variable = {
      val __obj = js.Dynamic.literal(termType = "Variable", value = value.asInstanceOf[js.Any])
      __obj.updateDynamic("equals")(js.Any.fromFunction1(equals_))
      __obj.asInstanceOf[types.rdflib.tfTypesMod.Variable]
    }
  }
}
