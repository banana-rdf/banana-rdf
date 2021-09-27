package types.std

import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}


type BodyInit = org.scalajs.dom.raw.Blob | types.std.BufferSource | org.scalajs.dom.raw.FormData | types.std.URLSearchParams | org.scalajs.dom.experimental.ReadableStream[js.typedarray.Uint8Array] | java.lang.String

type BufferSource = js.typedarray.ArrayBufferView | js.typedarray.ArrayBuffer

type HeadersInit = org.scalajs.dom.experimental.Headers | js.Array[js.Array[java.lang.String]] | (types.std.Record[java.lang.String, java.lang.String])

/**
  * Construct a type with a set of properties K of type T
  */
type Record[K /* <: /* keyof any */ java.lang.String */, T] = org.scalablytyped.runtime.StringDictionary[T]

type RequestInfo = org.scalajs.dom.experimental.Request | java.lang.String
