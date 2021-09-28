package types.rdflib

import types.rdflib.factoryTypesMod._Comparable
import types.rdflib.factoryTypesMod._DefaultFactoryTypes
import types.rdflib.factoryTypesMod._TFIDFactoryTypes
import types.rdflib.rdflibStrings._Colon
import types.rdflib.tfTypesMod.QuadGraph
import types.rdflib.tfTypesMod.QuadObject
import types.rdflib.tfTypesMod.QuadSubject
import types.rdflib.typesMod.SubjectType
import types.rdflib.typesMod._FromValueReturns
import types.rdflib.typesMod._ObjectType
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object blankNodeMod {
  
  @JSImport("rdflib/lib/blank-node", JSImport.Default)
  @js.native
  /**
    * Initializes this node
    * @param [id] The identifier for the blank node
    */
  open class default ()
    extends BlankNode
       with SubjectType
       with _DefaultFactoryTypes
       with _ObjectType {
    def this(id: String) = this()
    def this(id: js.Any) = this()
  }
  /* static members */
  object default {
    
    @JSImport("rdflib/lib/blank-node", JSImport.Default)
    @js.native
    val ^ : js.Any = js.native
    
    @JSImport("rdflib/lib/blank-node", "default.NTAnonymousNodePrefix")
    @js.native
    def NTAnonymousNodePrefix: _Colon = js.native
    inline def NTAnonymousNodePrefix_=(x: _Colon): Unit = ^.asInstanceOf[js.Dynamic].updateDynamic("NTAnonymousNodePrefix")(x.asInstanceOf[js.Any])
    
    @JSImport("rdflib/lib/blank-node", "default.getId")
    @js.native
    def getId: js.Any = js.native
    inline def getId_=(x: js.Any): Unit = ^.asInstanceOf[js.Dynamic].updateDynamic("getId")(x.asInstanceOf[js.Any])
    
    /**
      * The next unique identifier for blank nodes
      */
    @JSImport("rdflib/lib/blank-node", "default.nextId")
    @js.native
    def nextId: Double = js.native
    inline def nextId_=(x: Double): Unit = ^.asInstanceOf[js.Dynamic].updateDynamic("nextId")(x.asInstanceOf[js.Any])
  }
  
  /* import warning: transforms.RemoveMultipleInheritance#findNewParents newComments Dropped parents 
  - types.rdflib.typesMod._ValueType because Already inherited
  - types.rdflib.tfTypesMod.Term because var conflicts: termType, value. Inlined 
  - types.rdflib.tfTypesMod.BlankNode because var conflicts: termType, value. Inlined  */ @js.native
  trait BlankNode
    extends types.rdflib.nodeInternalMod.default
       with QuadGraph
       with QuadObject
       with QuadSubject
       with _Comparable
       with _TFIDFactoryTypes
       with _FromValueReturns[js.Any] {
    
    def compareTerm(other: BlankNode): Double = js.native
    
    /**
      * Gets a copy of this blank node in the specified formula
      * @param formula The formula
      */
    def copy(formula: types.rdflib.storeMod.default): BlankNode = js.native
    
    /**
      * The identifier for the blank node
      */
    def id: String = js.native
    def id_=(value: String): Unit = js.native
    
    /** Whether this is a blank node */
    var isBlank: Double = js.native
    
    /**
      * This type of node is a variable.
      *
      * Note that the existence of this property already indicates that it is a variable.
      */
    var isVar: Double = js.native
    
    @JSName("termType")
    var termType_BlankNode: types.rdflib.rdflibStrings.BlankNode = js.native
  }
}
