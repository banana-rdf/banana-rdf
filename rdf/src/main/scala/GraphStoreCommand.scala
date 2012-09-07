package org.w3.banana

import scalaz._
import Id._
import Free._

sealed trait RW
case object READ extends RW
case object WRITE extends RW

object Command {

  def create[Rdf <: RDF](uri: Rdf#URI): Free[({ type l[+x] = Command[Rdf, x] })#l, Unit] =
    Suspend[({ type l[+x] = Command[Rdf, x] })#l, Unit](Create(uri, Return[({ type l[+x] = Command[Rdf, x] })#l, Unit](())))

  def append[Rdf <: RDF](uri: Rdf#URI, triples: Iterable[Rdf#Triple]): Free[({ type l[+x] = Command[Rdf, x] })#l, Unit] =
    Suspend[({ type l[+x] = Command[Rdf, x] })#l, Unit](Append(uri, triples, Return[({ type l[+x] = Command[Rdf, x] })#l, Unit](())))

  def remove[Rdf <: RDF](uri: Rdf#URI, tripleMatches: Iterable[TripleMatch[Rdf]]): Free[({ type l[+x] = Command[Rdf, x] })#l, Unit] =
    Suspend[({ type l[+x] = Command[Rdf, x] })#l, Unit](Remove(uri, tripleMatches, Return[({ type l[+x] = Command[Rdf, x] })#l, Unit](())))

  def delete[Rdf <: RDF](uri: Rdf#URI): Free[({ type l[+x] = Command[Rdf, x] })#l, Unit] =
    Suspend[({ type l[+x] = Command[Rdf, x] })#l, Unit](Delete(uri, Return[({ type l[+x] = Command[Rdf, x] })#l, Unit](())))

  def patch[Rdf <: RDF](uri: Rdf#URI, deleteTripleMatches: Iterable[TripleMatch[Rdf]], insertTriples: Iterable[Rdf#Triple]): Free[({ type l[+x] = Command[Rdf, x] })#l, Unit] =
    for {
      _ <- remove(uri, deleteTripleMatches)
      _ <- append(uri, insertTriples)
    } yield ()

  def get[Rdf <: RDF](uri: Rdf#URI): Free[({ type l[+x] = Command[Rdf, x] })#l, Rdf#Graph] =
    Suspend[({ type l[+x] = Command[Rdf, x] })#l, Rdf#Graph](
      Get(uri,
        g => Return[({ type l[+x] = Command[Rdf, x] })#l, Rdf#Graph](g)))

  def select[Rdf <: RDF](query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node]): Free[({ type l[+x] = Command[Rdf, x] })#l, Rdf#Solutions] =
    Suspend[({ type l[+x] = Command[Rdf, x] })#l, Rdf#Solutions](
      Select(query,
        bindings,
        s => Return[({ type l[+x] = Command[Rdf, x] })#l, Rdf#Solutions](s)))

  def construct[Rdf <: RDF](query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node]): Free[({ type l[+x] = Command[Rdf, x] })#l, Rdf#Graph] =
    Suspend[({ type l[+x] = Command[Rdf, x] })#l, Rdf#Graph](
      Construct(query,
        bindings,
        g => Return[({ type l[+x] = Command[Rdf, x] })#l, Rdf#Graph](g)))

  def ask[Rdf <: RDF](query: Rdf#AskQuery, bindings: Map[String, Rdf#Node]): Free[({ type l[+x] = Command[Rdf, x] })#l, Boolean] =
    Suspend[({ type l[+x] = Command[Rdf, x] })#l, Boolean](
      Ask(query,
        bindings,
        b => Return[({ type l[+x] = Command[Rdf, x] })#l, Boolean](b)))

  implicit def ldcFunctor[Rdf <: RDF]: Functor[({ type l[+x] = Command[Rdf, x] })#l] =
    new Functor[({ type l[+ x] = Command[Rdf, x] })#l] {

      def map[A, B](command: Command[Rdf, A])(f: A => B): Command[Rdf, B] =
        command match {
          case Create(uri, a) => Create(uri, f(a))
          case Delete(uri, a) => Delete(uri, f(a))
          case Get(uri, k) => Get(uri, x => f(k(x)))
          case Append(uri, triples, a) => Append(uri, triples, f(a))
          case Remove(uri, tripleMatches, a) => Remove(uri, tripleMatches, f(a))
          case Select(query, bindings, k) => Select(query, bindings, x => f(k(x)))
          case Construct(query, bindings, k) => Construct(query, bindings, x => f(k(x)))
          case Ask(query, bindings, k) => Ask(query, bindings, x => f(k(x)))
        }

    }

}

sealed trait Command[Rdf <: RDF, +A]

case class Create[Rdf <: RDF, A](uri: Rdf#URI, a: A) extends Command[Rdf, A]

case class Delete[Rdf <: RDF, A](uri: Rdf#URI, a: A) extends Command[Rdf, A]

case class Get[Rdf <: RDF, A](uri: Rdf#URI, h: Rdf#Graph => A) extends Command[Rdf, A]

case class Append[Rdf <: RDF, A](uri: Rdf#URI, triples: Iterable[Rdf#Triple], a: A) extends Command[Rdf, A]

case class Remove[Rdf <: RDF, A](uri: Rdf#URI, tripleMatches: Iterable[TripleMatch[Rdf]], a: A) extends Command[Rdf, A]

case class Select[Rdf <: RDF, A](query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node], k: Rdf#Solutions => A) extends Command[Rdf, A]

case class Construct[Rdf <: RDF, A](query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node], k: Rdf#Graph => A) extends Command[Rdf, A]

case class Ask[Rdf <: RDF, A](query: Rdf#AskQuery, bindings: Map[String, Rdf#Node], k: Boolean => A) extends Command[Rdf, A]
