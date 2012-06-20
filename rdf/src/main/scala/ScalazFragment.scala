package org.w3.banana.scalaz

sealed trait Validation[+E, +A] {

  def fold[X](failure: E => X, success: A => X): X = this match {
    case Success(x) => success(x)
    case Failure(x) => failure(x)
  }

  def map[B](f: A => B): Validation[E, B] = this match {
    case Success(a) => Success(f(a))
    case Failure(e) => Failure(e)
  }

  def foreach[U](f: A => U): Unit = this match {
    case Success(a) => f(a)
    case Failure(e) =>
  }

  def flatMap[B, EE >: E](f: A => Validation[EE, B]): Validation[EE, B] = this match {
    case Success(a) => f(a)
    case Failure(e) => Failure(e)
  }

  /** Convert to a `scala.Either`. `Success` is converted to `scala.Right`, and `Failure` to `scala.Left`. */
  def either: Either[E, A] = this match {
    case Success(a) => Right(a)
    case Failure(e) => Left(e)
  }

  def isSuccess: Boolean = this match {
    case Success(_) => true
    case Failure(_) => false
  }

  def isFailure: Boolean = !isSuccess

  /** Returns the contents of this validation, in an `Some`, if it is a `Success`, otherwise `None` */
  def toOption: Option[A] = this match {
    case Success(a) => Some(a)
    case Failure(_) => None
  }
}

final case class Success[E, A](a: A) extends Validation[E, A]

final case class Failure[E, A](e: E) extends Validation[E, A]



sealed trait Either3[+A, +B, +C] {
  def fold[Z](left: A => Z, middle: B => Z, right: C => Z): Z = this match {
    case Left3(a)   => left(a)
    case Middle3(b) => middle(b)
    case Right3(c)  => right(c)
  }

  def eitherLeft:  Either[Either[A, B], C] = this match {
    case Left3(a)   => Left(Left(a))
    case Middle3(b) => Left(Right(b))
    case Right3(c)  => Right(c)
  }

  def eitherRight: Either[A, Either[B, C]] = this match {
    case Left3(a)   => Left(a)
    case Middle3(b) => Right(Left(b))
    case Right3(c)  => Right(Right(c))
  }

  def leftOr[Z](z: => Z)(f: A => Z)   = fold(f, _ => z, _ => z)
  def middleOr[Z](z: => Z)(f: B => Z) = fold(_ => z, f, _ => z)
  def rightOr[Z](z: => Z)(f: C => Z)  = fold(_ => z, _ => z, f)
}

case class Left3[+A, +B, +C](a: A) extends Either3[A, B, C]
case class Middle3[+A, +B, +C](b: B) extends Either3[A, B, C]
case class Right3[+A, +B, +C](c: C) extends Either3[A, B, C]