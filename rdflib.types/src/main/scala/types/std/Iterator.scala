package types.std

import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

@js.native
trait Iterator[T, TReturn, TNext] extends StObject {
  
  // NOTE: 'next' is defined using a tuple to ensure we report the correct assignability errors in all places.
  def next(
    /* import warning: parser.TsParser#functionParam Dropping repeated marker of param args because its type [] | [TNext] is not an array type */ args: js.Array[js.Any | TNext]
  ): IteratorResult[T, TReturn] = js.native
  
  var `return`: js.UndefOr[js.Function1[/* value */ js.UndefOr[TReturn], IteratorResult[T, TReturn]]] = js.native
  
  var `throw`: js.UndefOr[js.Function1[/* e */ js.UndefOr[js.Any], IteratorResult[T, TReturn]]] = js.native
}
