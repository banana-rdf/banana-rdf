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

object namedNodeMod {
  
  @JSImport("rdflib/lib/named-node", JSImport.Default)
  @js.native
  class default protected ()
    extends NamedNode
       with GraphType
       with PredicateType
       with SubjectType
       with _DefaultFactoryTypes
       with _ObjectType {
    /**
      * Create a named (IRI) RDF Node
      * @constructor
      * @param iri - The IRI for this node
      */
    def this(iri: String) = this()
  }
  /* static members */
  object default {
    
    @JSImport("rdflib/lib/named-node", JSImport.Default)
    @js.native
    val ^ : js.Any = js.native
    
    /**
      * Creates a named node from the specified input value
      * @param value - An input value
      */
    inline def fromValue(value: js.Any): js.Any = ^.asInstanceOf[js.Dynamic].applyDynamic("fromValue")(value.asInstanceOf[js.Any]).asInstanceOf[js.Any]
  }
  
  /* import warning: transforms.RemoveMultipleInheritance#findNewParents newComments Dropped parents 
  - types.rdflib.typesMod._ValueType because Already inherited
  - types.rdflib.tfTypesMod.Term because var conflicts: termType, value. Inlined 
  - types.rdflib.tfTypesMod.NamedNode because var conflicts: termType, value. Inlined  */ @js.native
  trait NamedNode
    extends types.rdflib.nodeInternalMod.default
       with QuadGraph
       with QuadObject
       with QuadPredicate
       with QuadSubject
       with _Comparable
       with _TFIDFactoryTypes
       with _FromValueReturns[js.Any] {
    
    /**
      * Returns an $rdf node for the containing directory, ending in slash.
      */
    def dir(): NamedNode | Null = js.native
    
    /**
      * Creates the fetchable named node for the document.
      * Removes everything from the # anchor tag.
      */
    def doc(): NamedNode = js.native
    
    /** The local identifier with the document */
    def id(): String = js.native
    
    /**
      * Returns an NN for the whole web site, ending in slash.
      * Contrast with the "origin" which does NOT have a trailing slash
      */
    def site(): NamedNode = js.native
    
    @JSName("termType")
    var termType_NamedNode: types.rdflib.rdflibStrings.NamedNode = js.native
    
    /** Alias for value, favored by Tim */
    def uri: String = js.native
    def uri_=(uri: String): Unit = js.native
  }
}
