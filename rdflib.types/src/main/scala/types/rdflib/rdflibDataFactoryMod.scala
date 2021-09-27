package types.rdflib

import org.scalablytyped.runtime.Shortcut
import types.rdflib.typesMod.IRDFlibDataFactory
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object rdflibDataFactoryMod extends Shortcut {
  
  /** Full RDFLib.js Data Factory */
  @JSImport("rdflib/lib/factories/rdflib-data-factory", JSImport.Default)
  @js.native
  val default: IRDFlibDataFactory = js.native
  
  type _To = IRDFlibDataFactory
  
  /* This means you don't have to write `default`, but can instead just say `rdflibDataFactoryMod.foo` */
  override def _to: IRDFlibDataFactory = default
}
