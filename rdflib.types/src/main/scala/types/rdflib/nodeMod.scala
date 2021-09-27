package types.rdflib

import types.rdflib.typesMod.FromValueReturns
import types.rdflib.typesMod.ValueType
import types.std.Number
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object nodeMod {
  
  @JSImport("rdflib/lib/node", JSImport.Default)
  @js.native
  abstract class default protected ()
    extends types.rdflib.nodeInternalMod.default {
    /* protected */ def this(value: String) = this()
  }
  /* static members */
  object default {
    
    @JSImport("rdflib/lib/node", JSImport.Default)
    @js.native
    val ^ : js.Any = js.native
    
    inline def fromValue[T /* <: FromValueReturns[js.Any] */](value: ValueType): T = ^.asInstanceOf[js.Dynamic].applyDynamic("fromValue")(value.asInstanceOf[js.Any]).asInstanceOf[T]
    
    inline def toJS(term: js.Any): js.Date | Number | String | Boolean | js.Object | (js.Array[js.Date | Number | String | Boolean | js.Object]) = ^.asInstanceOf[js.Dynamic].applyDynamic("toJS")(term.asInstanceOf[js.Any]).asInstanceOf[js.Date | Number | String | Boolean | js.Object | (js.Array[js.Date | Number | String | Boolean | js.Object])]
  }
}
