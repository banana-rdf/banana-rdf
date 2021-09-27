package types.rdflib

import types.rdflib.factoryTypesMod._Comparable
import types.rdflib.factoryTypesMod._TFIDFactoryTypes
import types.rdflib.rdflibStrings._empty
import types.rdflib.tfTypesMod.QuadGraph
import types.rdflib.tfTypesMod.QuadObject
import types.rdflib.typesMod.GraphType
import types.rdflib.typesMod._FromValueReturns
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object defaultGraphMod {
  
  @JSImport("rdflib/lib/default-graph", JSImport.Namespace)
  @js.native
  val ^ : js.Any = js.native
  
  @JSImport("rdflib/lib/default-graph", JSImport.Default)
  @js.native
  class default ()
    extends DefaultGraph
       with GraphType
  
  inline def isDefaultGraph(`object`: js.Any): /* is rdflib.rdflib/lib/default-graph.DefaultGraph */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isDefaultGraph")(`object`.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/default-graph.DefaultGraph */ Boolean]
  
  /* import warning: transforms.RemoveMultipleInheritance#findNewParents newComments Dropped parents 
  - types.rdflib.typesMod._ValueType because Already inherited
  - types.rdflib.tfTypesMod.Term because var conflicts: termType, value. Inlined 
  - types.rdflib.tfTypesMod.DefaultGraph because var conflicts: termType, value. Inlined  */ @js.native
  trait DefaultGraph
    extends types.rdflib.nodeInternalMod.default
       with QuadGraph
       with QuadObject
       with _Comparable
       with _FromValueReturns[js.Any]
       with _TFIDFactoryTypes {
    
    @JSName("termType")
    var termType_DefaultGraph: types.rdflib.rdflibStrings.DefaultGraph = js.native
    
    var uri: String = js.native
    
    @JSName("value")
    var value_DefaultGraph: _empty = js.native
  }
}
