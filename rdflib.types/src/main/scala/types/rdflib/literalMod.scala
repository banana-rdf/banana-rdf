package types.rdflib

import types.rdflib.factoryTypesMod._Comparable
import types.rdflib.factoryTypesMod._DefaultFactoryTypes
import types.rdflib.factoryTypesMod._TFIDFactoryTypes
import types.rdflib.tfTypesMod.NamedNode
import types.rdflib.tfTypesMod.QuadObject
import types.rdflib.typesMod.FromValueReturns
import types.rdflib.typesMod.ValueType
import types.rdflib.typesMod._FromValueReturns
import types.rdflib.typesMod._ObjectType
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object literalMod {
  
  @JSImport("rdflib/lib/literal", JSImport.Default)
  @js.native
  open class default protected ()
    extends Literal
       with _DefaultFactoryTypes
       with _ObjectType {
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
  object default {
    
    @JSImport("rdflib/lib/literal", JSImport.Default)
    @js.native
    val ^ : js.Any = js.native
    
    /**
      * Builds a literal node from a boolean value
      * @param value - The value
      */
    inline def fromBoolean(value: Boolean): Literal = ^.asInstanceOf[js.Dynamic].applyDynamic("fromBoolean")(value.asInstanceOf[js.Any]).asInstanceOf[Literal]
    
    /**
      * Builds a literal node from a date value
      * @param value The value
      */
    inline def fromDate(value: js.Date): Literal = ^.asInstanceOf[js.Dynamic].applyDynamic("fromDate")(value.asInstanceOf[js.Any]).asInstanceOf[Literal]
    
    /**
      * Builds a literal node from a number value
      * @param value - The value
      */
    inline def fromNumber(value: Double): Literal = ^.asInstanceOf[js.Dynamic].applyDynamic("fromNumber")(value.asInstanceOf[js.Any]).asInstanceOf[Literal]
    
    /**
      * Builds a literal node from an input value
      * @param value - The input value
      */
    inline def fromValue[T /* <: FromValueReturns[js.Any] */](value: ValueType): T = ^.asInstanceOf[js.Dynamic].applyDynamic("fromValue")(value.asInstanceOf[js.Any]).asInstanceOf[T]
    
    /** Serializes a literal to an N-Triples string */
    inline def toNT(literal: Literal): String = ^.asInstanceOf[js.Dynamic].applyDynamic("toNT")(literal.asInstanceOf[js.Any]).asInstanceOf[String]
  }
  
  /* import warning: transforms.RemoveMultipleInheritance#findNewParents newComments Dropped parents 
  - types.rdflib.typesMod._ValueType because Already inherited
  - types.rdflib.tfTypesMod.Term because var conflicts: termType, value. Inlined 
  - types.rdflib.tfTypesMod.Literal because var conflicts: termType, value. Inlined language, datatype */ @js.native
  trait Literal
    extends types.rdflib.nodeInternalMod.default
       with QuadObject
       with _Comparable
       with _TFIDFactoryTypes
       with _FromValueReturns[js.Any] {
    
    /**
      * Gets a copy of this literal
      */
    def copy(): Literal = js.native
    
    /**
      * The literal's datatype as a named node
      */
    var datatype: NamedNode | types.rdflib.namedNodeMod.default = js.native
    
    var isVar: Double = js.native
    
    /**
      * The language for the literal
      * @deprecated use {language} instead
      */
    def lang: String = js.native
    def lang_=(language: String): Unit = js.native
    
    /**
      * The language for the literal
      */
    var language: String = js.native
    
    @JSName("termType")
    var termType_Literal: types.rdflib.rdflibStrings.Literal = js.native
  }
}
