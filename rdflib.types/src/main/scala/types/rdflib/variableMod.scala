package types.rdflib

import types.rdflib.factoryTypesMod._Comparable
import types.rdflib.factoryTypesMod._DefaultFactoryTypes
import types.rdflib.factoryTypesMod._TFIDFactoryTypes
import types.rdflib.tfTypesMod.QuadGraph
import types.rdflib.tfTypesMod.QuadObject
import types.rdflib.tfTypesMod.QuadPredicate
import types.rdflib.tfTypesMod.QuadSubject
import types.rdflib.typesMod.GraphType
import types.rdflib.typesMod.PredicateType
import types.rdflib.typesMod.SubjectType
import types.rdflib.typesMod._FromValueReturns
import types.rdflib.typesMod._ObjectType
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object variableMod {
  
  @JSImport("rdflib/lib/variable", JSImport.Default)
  @js.native
  /**
    * Initializes this variable
    * @param name The variable's name
    */
  open class default ()
    extends Variable
       with GraphType
       with PredicateType
       with SubjectType
       with _DefaultFactoryTypes
       with _ObjectType {
    def this(name: String) = this()
  }
  /* static members */
  object default {
    
    @JSImport("rdflib/lib/variable", JSImport.Default)
    @js.native
    val ^ : js.Any = js.native
    
    inline def toString(variable: js.Any): String = ^.asInstanceOf[js.Dynamic].applyDynamic("toString")(variable.asInstanceOf[js.Any]).asInstanceOf[String]
  }
  
  /* import warning: transforms.RemoveMultipleInheritance#findNewParents newComments Dropped parents 
  - types.rdflib.typesMod._ValueType because Already inherited
  - types.rdflib.tfTypesMod.Term because var conflicts: termType, value. Inlined 
  - types.rdflib.tfTypesMod.Variable because var conflicts: termType, value. Inlined  */ @js.native
  trait Variable
    extends types.rdflib.nodeInternalMod.default
       with QuadGraph
       with QuadObject
       with QuadPredicate
       with QuadSubject
       with _TFIDFactoryTypes
       with _Comparable
       with _FromValueReturns[js.Any] {
    
    /** The base string for a variable's name */
    var base: String = js.native
    
    def equals(other: js.Any): Boolean = js.native
    
    var isVar: Double = js.native
    
    def substitute(bindings: js.Any): js.Any = js.native
    
    @JSName("termType")
    var termType_Variable: types.rdflib.rdflibStrings.Variable = js.native
    
    /** The unique identifier of this variable */
    var uri: String = js.native
  }
}
