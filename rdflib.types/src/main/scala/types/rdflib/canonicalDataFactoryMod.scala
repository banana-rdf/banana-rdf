package types.rdflib

import org.scalablytyped.runtime.Shortcut
import types.rdflib.factoryTypesMod.DataFactory
import types.rdflib.factoryTypesMod.DefaultFactoryTypes
import types.rdflib.factoryTypesMod.Indexable
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object canonicalDataFactoryMod extends Shortcut {
  
  /** A basic internal RDFlib datafactory, which does not support Collections  */
  @JSImport("rdflib/lib/factories/canonical-data-factory", JSImport.Default)
  @js.native
  val default: DataFactory[DefaultFactoryTypes, Indexable] = js.native
  
  @JSImport("rdflib/lib/factories/canonical-data-factory", "defaultGraphURI")
  @js.native
  val defaultGraphURI: /* "chrome:theSession" */ String = js.native
  
  type _To = DataFactory[DefaultFactoryTypes, Indexable]
  
  /* This means you don't have to write `default`, but can instead just say `canonicalDataFactoryMod.foo` */
  override def _to: DataFactory[DefaultFactoryTypes, Indexable] = default
}
