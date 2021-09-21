

trait Semigroup[T]:
	extension (t: T)
		infix def combine(other: T): T
		infix def <+>(other: T): T = t.combine(other)

trait Monoid[T] extends Semigroup[T]:
	def unit: T

given StringMonoid: Monoid[String] with
	def unit: String = ""
	extension (s: String)
		infix def combine(other: String): String =
			s + other

given NumericMonoid[T: Numeric]: Monoid[T] with
	println("Initialising NumericMonoid for "+this)
	override def unit = summon[Numeric[T]].zero
	extension (t: T)
		infix def combine(other: T): T =
			summon[Numeric[T]].plus(t, other)


"34" <+> "43" <+> StringMonoid.unit
//case class <+>[A,B](a: A, b: B)
//<+>("3","5") match
//	case a <+> b => s"$a thne $b"

//object IntMonoid extends Monoid[Int]:
//	def unit: Int = 0
//	extension (s: Int)
//		infix def combine(other: Int): Int =
//			s + other
//
//given im: Monoid[Int] = IntMonoid


given NumericMonoid[T: Numeric]: Monoid[T] with
	println("Initialising NumericMonoid for "+this)
	override def unit = summon[Numeric[T]].zero
	extension (t: T)
		infix def combine(other: T): T =
			summon[Numeric[T]].plus(t, other)

4 <+> 5
5 <+> 6
5.4 <+> 4.5

given BigDM: Monoid[BigDecimal] = NumericMonoid(Numeric[BigDecimal])
BigDecimal(3.13) <+> BigDecimal(4.132) <+> BigDecimal(3.222)
