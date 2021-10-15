package org.w3.banana.equalsTest

// this code demonstates that the extension methods in ==
// does not call any of the equals in IntObj.
// see https://users.scala-lang.org/t/equals-on-scalajs-facade/7832

class IntObj(val i: Int) {
	override def equals(obj: Any): Boolean =
		println("in IntObj.equals(obj: Any)")
		false

	def ==(other: IntObj): Boolean =
		println("in IntObj.equals(other: IntObj)")
		other.i == i
}

object Hello {
	opaque type Hello = IntObj
	def apply(i: Int): Hello = IntObj(i)
}

object test {
	import Hello.*

	def main(a: Array[String]): Unit =
		val x: Hello = Hello(25)
		val y: Hello = Hello(50)
		val z: Hello = Hello(25)

		println(y == x)
		println(x == z)
}

