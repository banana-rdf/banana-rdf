package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers

abstract class ObjectBinderTest[Rdf <: RDF]()(implicit diesel: Diesel[Rdf]) extends WordSpec with MustMatchers {

  import diesel._
  import ops._

  sealed trait T

  object T {

    val clazz = uri("http://example.com/T#class")

    val binder: SealedBinder[Rdf, T] =
      ObjectBinder.sealedB[T](Foo.binder, Bar.binder) {
        case _: Foo => toPGB[T, Foo](Foo.binder)
        case Bar => Bar.binder
      }

  }

  case class Foo(i: Int, j: String) extends T

  object Foo {
    
    val clazz = uri("http://example.com/Foo#class")

    val i = property[Int](uri("http://example.com/Foo#i"))
    
    val j = property[String](uri("http://example.com/Foo#j"))
    
    val uriBinder: URIBinder[Rdf, Foo] = null

    val binder: PointedGraphBinder[Rdf, Foo] =
      ObjectBinder
        .property(i).property(j).bind(Foo.apply, Foo.unapply)
        .instanceOf(T.clazz).instanceOf(Foo.clazz)
        .uriBinder(uriBinder)

    implicitly[Foo <:< T]

    implicitly[ToPGB[T] <:< ToPGB[Foo]]

  }

  case object Bar extends T {

    val clazz = uri("http://example.Bar#class")

    val constant = uri("http://example.com/Bar#thing")

    val binder: PointedGraphBinder[Rdf, T] =
      ObjectBinder.constant(Bar, constant)


    

  }

}
