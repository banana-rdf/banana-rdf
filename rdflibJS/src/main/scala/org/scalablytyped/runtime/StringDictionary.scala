package org.scalablytyped.runtime

import scala.scalajs.js
import scala.scalajs.js.WrappedDictionary

import scala.language.implicitConversions

trait StringDictionary[+V] extends StObject

object StringDictionary {

	/** Returns a new empty dictionary */
	@inline def empty[A]: StringDictionary[A] =
		(new js.Object).asInstanceOf[StringDictionary[A]]

	@inline
	def apply[A](properties: (String, A)*): StringDictionary[A] =
		js.special.objectLiteral(properties*).asInstanceOf[StringDictionary[A]]

	@inline implicit def wrapStringDictionary[V](dict: StringDictionary[V]): WrappedDictionary[V] =
		new WrappedDictionary(dict.asInstanceOf[js.Dictionary[V]])
}
