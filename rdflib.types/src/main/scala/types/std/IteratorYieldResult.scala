package types.std

import types.std.stdBooleans.`false`
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

trait IteratorYieldResult[TYield]
  extends StObject
     with IteratorResult[TYield, js.Any] {
  
  var done: js.UndefOr[`false`] = js.undefined
  
  var value: TYield
}
object IteratorYieldResult {
  
  inline def apply[TYield](value: TYield): IteratorYieldResult[TYield] = {
    val __obj = js.Dynamic.literal(value = value.asInstanceOf[js.Any])
    __obj.asInstanceOf[IteratorYieldResult[TYield]]
  }
  
  extension [Self <: IteratorYieldResult[?], TYield](x: Self & IteratorYieldResult[TYield]) {
    
    inline def setDone(value: `false`): Self = StObject.set(x, "done", value.asInstanceOf[js.Any])
    
    inline def setDoneUndefined: Self = StObject.set(x, "done", js.undefined)
    
    inline def setValue(value: TYield): Self = StObject.set(x, "value", value.asInstanceOf[js.Any])
  }
}
