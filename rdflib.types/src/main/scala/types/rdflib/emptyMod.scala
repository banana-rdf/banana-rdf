package types.rdflib

import types.rdflib.factoryTypesMod._Comparable
import types.rdflib.factoryTypesMod._TFIDFactoryTypes
import types.rdflib.tfTypesMod.QuadObject
import types.rdflib.typesMod._FromValueReturns
import types.rdflib.typesMod._ObjectType
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object emptyMod {
  
  @JSImport("rdflib/lib/empty", JSImport.Default)
  @js.native
  class default ()
    extends Empty
       with _ObjectType
  
  /* import warning: transforms.RemoveMultipleInheritance#findNewParents newComments Dropped parents 
  - types.rdflib.typesMod._ValueType because Already inherited
  - types.rdflib.tfTypesMod.Term because var conflicts: termType, value. Inlined  */ @js.native
  trait Empty
    extends types.rdflib.nodeInternalMod.default
       with QuadObject
       with _Comparable
       with _FromValueReturns[js.Any]
       with _TFIDFactoryTypes {
    
    @JSName("termType")
    var termType_Empty: types.rdflib.rdflibStrings.Empty = js.native
  }
}
