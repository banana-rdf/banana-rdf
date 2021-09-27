package types.rdflib

import types.rdflib.tfTypesMod.NamedNode
import types.rdflib.tfTypesMod.RdfJsDataFactory
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object namespaceMod {
  
  @JSImport("rdflib/lib/namespace", JSImport.Namespace)
  @js.native
  val ^ : js.Any = js.native
  
  inline def default(nsuri: String): js.Function1[/* ln */ String, NamedNode] = ^.asInstanceOf[js.Dynamic].applyDynamic("default")(nsuri.asInstanceOf[js.Any]).asInstanceOf[js.Function1[/* ln */ String, NamedNode]]
  inline def default(nsuri: String, factory: RdfJsDataFactory): js.Function1[/* ln */ String, NamedNode] = (^.asInstanceOf[js.Dynamic].applyDynamic("default")(nsuri.asInstanceOf[js.Any], factory.asInstanceOf[js.Any])).asInstanceOf[js.Function1[/* ln */ String, NamedNode]]
}
