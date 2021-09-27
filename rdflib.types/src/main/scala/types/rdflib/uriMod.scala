package types.rdflib

import types.rdflib.namedNodeMod.default
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object uriMod {
  
  @JSImport("rdflib/lib/uri", JSImport.Namespace)
  @js.native
  val ^ : js.Any = js.native
  
  inline def docpart(uri: String): String = ^.asInstanceOf[js.Dynamic].applyDynamic("docpart")(uri.asInstanceOf[js.Any]).asInstanceOf[String]
  
  inline def document(x: String): default = ^.asInstanceOf[js.Dynamic].applyDynamic("document")(x.asInstanceOf[js.Any]).asInstanceOf[default]
  
  inline def hostpart(u: String): String = ^.asInstanceOf[js.Dynamic].applyDynamic("hostpart")(u.asInstanceOf[js.Any]).asInstanceOf[String]
  
  inline def join(`given`: String, base: String): String = (^.asInstanceOf[js.Dynamic].applyDynamic("join")(`given`.asInstanceOf[js.Any], base.asInstanceOf[js.Any])).asInstanceOf[String]
  
  inline def protocol(uri: String): String | Null = ^.asInstanceOf[js.Dynamic].applyDynamic("protocol")(uri.asInstanceOf[js.Any]).asInstanceOf[String | Null]
  
  inline def refTo(base: String, uri: String): String = (^.asInstanceOf[js.Dynamic].applyDynamic("refTo")(base.asInstanceOf[js.Any], uri.asInstanceOf[js.Any])).asInstanceOf[String]
}
