package types.rdflib

import org.scalablytyped.runtime.Shortcut
import types.rdflib.collectionMod.Collection
import types.rdflib.factoryTypesMod.DataFactory
import types.rdflib.factoryTypesMod.DefaultFactoryTypes
import types.rdflib.factoryTypesMod.Indexable
import types.rdflib.typesMod.ValueType
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object extendedTermFactoryMod extends Shortcut {
  
  /**
    * Data factory which also supports Collections
    *
    * Necessary for preventing circular dependencies.
    */
  @JSImport("rdflib/lib/factories/extended-term-factory", JSImport.Default)
  @js.native
  val default: CollectionFactory = js.native
  
  @js.native
  trait CollectionFactory
    extends StObject
       with DataFactory[DefaultFactoryTypes, Indexable] {
    
    def collection(elements: js.Array[ValueType]): types.rdflib.collectionMod.default[
        types.rdflib.nodeInternalMod.default | types.rdflib.blankNodeMod.default | Collection[js.Any] | types.rdflib.literalMod.default | types.rdflib.variableMod.default
      ] = js.native
  }
  
  type _To = CollectionFactory
  
  /* This means you don't have to write `default`, but can instead just say `extendedTermFactoryMod.foo` */
  override def _to: CollectionFactory = default
}
