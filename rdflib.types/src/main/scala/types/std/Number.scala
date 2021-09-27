package types.std

import types.std.Intl.NumberFormatOptions
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

@js.native
trait Number extends StObject {
  
  /**
    * Returns a string containing a number represented in exponential notation.
    * @param fractionDigits Number of digits after the decimal point. Must be in the range 0 - 20, inclusive.
    */
  def toExponential(): String = js.native
  def toExponential(fractionDigits: Double): String = js.native
  
  /**
    * Returns a string representing a number in fixed-point notation.
    * @param fractionDigits Number of digits after the decimal point. Must be in the range 0 - 20, inclusive.
    */
  def toFixed(): String = js.native
  def toFixed(fractionDigits: Double): String = js.native
  
  def toLocaleString(locales: String): String = js.native
  def toLocaleString(locales: String, options: NumberFormatOptions): String = js.native
  def toLocaleString(locales: js.Array[String]): String = js.native
  def toLocaleString(locales: js.Array[String], options: NumberFormatOptions): String = js.native
  def toLocaleString(locales: Unit, options: NumberFormatOptions): String = js.native
  
  /**
    * Returns a string containing a number represented either in exponential or fixed-point notation with a specified number of digits.
    * @param precision Number of significant digits. Must be in the range 1 - 21, inclusive.
    */
  def toPrecision(): String = js.native
  def toPrecision(precision: Double): String = js.native
  
  def toString(radix: Double): String = js.native
}
