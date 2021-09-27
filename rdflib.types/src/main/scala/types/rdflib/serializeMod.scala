package types.rdflib

import types.rdflib.anon.Flags
import types.rdflib.tfTypesMod.BlankNode
import types.rdflib.tfTypesMod.NamedNode
import types.rdflib.typesMod.ContentType
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object serializeMod {
  
  @JSImport("rdflib/lib/serialize", JSImport.Namespace)
  @js.native
  val ^ : js.Any = js.native
  
  inline def default(
    /** The graph or nodes that should be serialized */
  target: types.rdflib.formulaMod.default | NamedNode | BlankNode,
    /** The store */
  kb: js.UndefOr[types.rdflib.storeMod.default],
    base: js.UndefOr[js.Any],
    /**
    * The mime type.
    * Defaults to Turtle.
    */
  contentType: js.UndefOr[String | ContentType],
    callback: js.UndefOr[
      js.Function2[
        /* err */ js.UndefOr[js.Error | Null], 
        /* result */ js.UndefOr[String | Null], 
        js.Any
      ]
    ],
    options: js.UndefOr[Flags]
  ): js.UndefOr[String] = (^.asInstanceOf[js.Dynamic].applyDynamic("default")(target.asInstanceOf[js.Any], kb.asInstanceOf[js.Any], base.asInstanceOf[js.Any], contentType.asInstanceOf[js.Any], callback.asInstanceOf[js.Any], options.asInstanceOf[js.Any])).asInstanceOf[js.UndefOr[String]]
}
