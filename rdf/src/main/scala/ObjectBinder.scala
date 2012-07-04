package org.w3.banana

import scalaz.{ Validation, Success, Failure }
import shapeless._
import shapeless.HList._
import shapeless.Tuples._

trait PGBElemAux {
  type valueType
}

trait PGBElem[Rdf <: RDF, T] extends PGBElemAux {

  type valueType = T

  def either: Either[PGBSubject[Rdf, T], PGBPredicateObject[Rdf, T]]

}

trait PGBSubject[Rdf <: RDF, T] {

  def makeSubject(t: T): Rdf#URI

  def extract(subject: Rdf#URI): Validation[BananaException, T]

}

trait PGBPredicateObject[Rdf <: RDF, T] {

  def predicate: Rdf#URI

  def binder: PointedGraphBinder[Rdf, T]

}


trait RecordBinder[Rdf <: RDF] {
  self: Diesel[Rdf] =>

  import ops._

  def property[T](uri: Rdf#URI)(implicit objectBinder: PointedGraphBinder[Rdf, T]): PGBElem[Rdf, T] = {

    val pgbPO = new PGBPredicateObject[Rdf, T] {
      val predicate = uri
      val binder = objectBinder
    }

    new PGBElem[Rdf, T] {
      val either = Right(pgbPO)
    }

  }

  /* example: http://example.com/foo/{id}   or    #{id}  */
  /* for now, supports only exactly one group in the regex */
  /* for now, we assume that T.toString does the right thing. TODO: use Show[T] */
  def uriTemplate[T](template: String)(f: String => Validation[BananaException, T]): PGBElem[Rdf, T] = {

    val pgbS = new PGBSubject[Rdf, T] {
      // replace the pattern with (.+?) and make sure that the entire string is read
      val regex = {
        val t = template.replaceAll("""\{.+?\}""", "(.+?)")
        ("^" + t + "$").r
      }

      def makeSubject(t: T): Rdf#URI = {
        val subject = template.replaceAll("""\{.*?\}""", t.toString())
        makeUri(subject)
      }

      def extract(subject: Rdf#URI): Validation[BananaException, T] = {
        regex findFirstIn fromUri(subject) match {
          case Some(regex(id)) => f(id)
          case None => Failure(WrongExpectation("could not apply template " + template + " to " + subject.toString))
        }
      }
    }

    new PGBElem[Rdf, T] {
      val either = Left(pgbS)
    }
  }

  type Acc = (Rdf#Node, List[(Rdf#URI, PointedGraph[Rdf])])

  import Poly._

  object makePG extends Poly2 {
    implicit def caseAccPGBElem[T](implicit t: T) = at[Acc, PGBElem[Rdf, T]] { (acc, elem) =>
      val (subject, pos) = acc
        elem.either match {
          case Left(pgbS) => (pgbS.makeSubject(t): Rdf#Node, pos): Acc
          case Right(pgbPO) => null.asInstanceOf[Acc]
        }
      }
    }

  val accStart: Acc = (bnode(), List.empty)

  def accToGraph(acc: Acc): PointedGraph[Rdf] = {
    val (subject, pos) = acc
    var triples: Set[Rdf#Triple] = Set.empty
    for (po <- pos) {
      val (p, pg) = po
      triples += Triple(subject, p, pg.node)
      triples ++= graphToIterable(pg.graph)
    }
    PointedGraph(subject, makeGraph(triples))
  }

  def pgb[T] = new Object {
    def apply[A1](a1: PGBElem[Rdf, A1])(apply: A1 => T, unapply: T => Option[A1]) = null

    def apply[A1, A2](a1: PGBElem[Rdf, A1], a2: PGBElem[Rdf, A2])(_apply: (A1, A2) => T, unapply: T => Option[(A1, A2)]) = {
      val hlist = (a1, a2).hlisted

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        implicit val Some((a1, a2)) = unapply(t)
        val acc = hlist.foldLeft(accStart)(makePG)
        accToGraph(acc)
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        
        object toElem extends Poly1 {
          implicit def casePGBElem[T] = at[PGBElem[Rdf, T]] { elem =>
            elem.either match {
              case Left(pgbS) => null.asInstanceOf[Validation[BananaException, T]]
              case Right(pgbPO) => null.asInstanceOf[Validation[BananaException, T]]

            }
          }
        }
        import toElem._

        object reducer extends Poly2 {
          implicit def foo[T1, T2] = at[Validation[BananaException, T1], Validation[BananaException, T2]] { (v1, v2) => for { t1 <- v1; t2 <- v2 } yield ((t1, t2)) }
        }

        import reducer._

        val validation: Validation[BananaException, (A1, A2)] = hlist.map(toElem).reduceLeft(reducer)

        validation map _apply
      }

      null
    }

    def apply[A1, A2, A3](a1: PGBElem[Rdf, A1], a2: PGBElem[Rdf, A2], a3: PGBElem[Rdf, A3])(apply: (A1, A2, A3) => T, unapply: T => Option[(A1, A2, A3)]) = null

  }

//  case class PropertiesBuilder1[T1] private[banana] (p1: Property[Rdf, T1]) {
//
//    def bind[T](apply: T1 => T, unapply: T => Option[T1]): ComplexBinderBase[T] = {
//
//      val fromPG: PointedGraph[Rdf] => Validation[BananaException, T] =
//        (pointed: PointedGraph[Rdf]) => {
//          for {
//            t1 <- (pointed / p1.uri).as[T1](p1.binder)
//          } yield {
//            apply(t1)
//          }
//        }
//
//      val toPG: (Rdf#Node, T) => PointedGraph[Rdf] =
//        (subject: Rdf#Node, t: T) => {
//          val t1 = unapply(t).get
//          subject.--(p1.uri).->-(t1)(p1.binder)
//        }
//
//      ComplexBinderBase(fromPG, toPG)
//
//    }

//    def constant[T](constObj: T, constUri: Rdf#URI): PointedGraphBinder[Rdf, T] = NodeToPointedGraphBinder(UriToNodeBinder( new URIBinder[Rdf, T] {
//
//      def fromUri(uri: Rdf#URI): Validation[BananaException, T] =
//        if (constUri == uri)
//          Success(constObj)
//        else
//          Failure(WrongExpectation("was expecting the constant URI " + constUri + " but got " + uri))
//  
//      def toUri(t: T): Rdf#URI = constUri
//
//    }))
//
//  }


  

  
  
}
