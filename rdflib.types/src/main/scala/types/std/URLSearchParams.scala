package types.std

import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

@js.native
trait URLSearchParams extends StObject {
  
  /**
    * Appends a specified key/value pair as a new search parameter.
    */
  def append(name: String, value: String): Unit = js.native
  
  /**
    * Deletes the given search parameter, and its associated value, from the list of all search parameters.
    */
  def delete(name: String): Unit = js.native
  
  /**
    * Returns an array of key, value pairs for every entry in the search params.
    */
  def entries(): IterableIterator[js.Tuple2[String, String]] = js.native
  
  def forEach(callbackfn: js.Function3[/* value */ String, /* key */ String, /* parent */ this.type, Unit]): Unit = js.native
  def forEach(
    callbackfn: js.Function3[/* value */ String, /* key */ String, /* parent */ this.type, Unit],
    thisArg: js.Any
  ): Unit = js.native
  
  /**
    * Returns the first value associated to the given search parameter.
    */
  def get(name: String): String | Null = js.native
  
  /**
    * Returns all the values association with a given search parameter.
    */
  def getAll(name: String): js.Array[String] = js.native
  
  /**
    * Returns a Boolean indicating if such a search parameter exists.
    */
  def has(name: String): Boolean = js.native
  
  @JSName(js.Symbol.iterator)
  var iterator: js.Function0[IterableIterator[js.Tuple2[String, String]]] = js.native
  
  /**
    * Returns a list of keys in the search params.
    */
  def keys(): IterableIterator[String] = js.native
  
  /**
    * Sets the value associated to a given search parameter to the given value. If there were several values, delete the others.
    */
  def set(name: String, value: String): Unit = js.native
  
  def sort(): Unit = js.native
  
  /**
    * Returns a list of values in the search params.
    */
  def values(): IterableIterator[String] = js.native
}
