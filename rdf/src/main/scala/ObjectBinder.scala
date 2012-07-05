package org.w3.banana

import scalaz.{ Validation, Success, Failure, _ }
import scalaz.Scalaz._
import shapeless._
import shapeless.HList._
import shapeless.Tuples._

trait PGBElem[Rdf <: RDF, T] {

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


object RecordBinder {

  type Acc[Rdf <: RDF] = (Rdf#Node, List[(Rdf#URI, PointedGraph[Rdf])])

  def m[Rdf <: RDF, T](acc: Acc[Rdf], t: T, elem: PGBElem[Rdf, T]): Acc[Rdf] = {
    val (subject, pos) = acc
    elem.either match {
      case Left(pgbS) => (pgbS.makeSubject(t), pos)
      case Right(pgbPO) => (subject, (pgbPO.predicate, pgbPO.binder.toPointedGraph(t)) :: pos)
    }
  }

  def accStart[Rdf <: RDF](implicit ops: RDFOperations[Rdf]): Acc[Rdf] = (ops.makeBNode(), List.empty)

  def accToGraph[Rdf <: RDF](acc: Acc[Rdf])(implicit ops: RDFOperations[Rdf]): PointedGraph[Rdf] = {
    val (subject, pos) = acc
    var triples: Set[Rdf#Triple] = Set.empty
    for (po <- pos) {
      val (p, pg) = po
      triples += ops.makeTriple(subject, p, pg.node)
      triples ++= ops.graphToIterable(pg.graph)
    }
    PointedGraph(subject, ops.makeGraph(triples))
  }

}




trait RecordBinder[Rdf <: RDF] {
  self: Diesel[Rdf] =>

  import RecordBinder._
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

  private def extract[T](pointed: PointedGraph[Rdf], elem: PGBElem[Rdf, T]): Validation[BananaException, T] = elem.either match {
    case Left(pgbS) => pointed.as[Rdf#URI] flatMap pgbS.extract
    case Right(pgbPO) => (pointed / pgbPO.predicate).as[T](pgbPO.binder)
  }

  def pgb[T] = new Object {
    def apply[A1](a1: PGBElem[Rdf, A1])(apply: A1 => T, unapply: T => Option[A1]) = null

    def apply[T1, T2](el1: PGBElem[Rdf, T1], el2: PGBElem[Rdf, T2])(apply: (T1, T2) => T, unapply: T => Option[(T1, T2)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2)) = unapply(t)
        val acc = m(m(accStart, t1, el1), t2, el2)
        accToGraph(acc)
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        val v1 = extract(pointed, el1)
        val v2 = extract(pointed, el2)
        for (t1 <- v1; t2 <- v2) yield apply(t1, t2)
      }

    }

    def apply[A1, A2, A3](a1: PGBElem[Rdf, A1], a2: PGBElem[Rdf, A2], a3: PGBElem[Rdf, A3])(apply: (A1, A2, A3) => T, unapply: T => Option[(A1, A2, A3)]) = null

  }
  
}
