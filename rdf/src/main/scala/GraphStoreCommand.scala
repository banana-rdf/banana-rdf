package org.w3.banana

import scalaz._
import scalaz.Scalaz._
import Id._
import Free._

sealed trait RW
case object READ extends RW
case object WRITE extends RW

object Command {

  def GET[Rdf <: RDF](hyperlink: Rdf#URI)(implicit ops: RDFOps[Rdf]): Free[({type l[+x] = Command[Rdf, x]})#l, LinkedDataResource[Rdf]] = {
    import ops._    
    val docUri = hyperlink.fragmentLess
    Command.get(docUri) map { graph =>
      val pointed = PointedGraph(hyperlink, graph)
      LinkedDataResource(docUri, pointed)
    }
  }

  def GET[Rdf <: RDF](hyperlinks: Iterable[Rdf#URI])(implicit ops: RDFOps[Rdf]): Free[({type l[+x] = Command[Rdf, x]})#l, Set[LinkedDataResource[Rdf]]] = {
    import ops._
    implicit val functor: Functor[({type l[+x] = Command[Rdf, x]})#l] = Command.ldcFunctor[Rdf]
    implicit val applicative: Applicative[({type f[+y] = Free[({type l[+x] = Command[Rdf, x]})#l, y]})#f] =
      Free.freeMonad[({type l[+x] = Command[Rdf, x]})#l]
    hyperlinks.map{ hyperlink => GET(hyperlink) }.toList.sequence[({type f[+y] = Free[({type l[+x] = Command[Rdf, x]})#l, y]})#f, LinkedDataResource[Rdf]].map(_.toSet)
  }

  def POST[Rdf <: RDF](uri: Rdf#URI, pointed: PointedGraph[Rdf])(implicit ops: RDFOps[Rdf]): Free[({type l[+x] = Command[Rdf, x]})#l, Unit] = {
    import ops._
    val docUri = uri.fragmentLess
    Command.append(docUri, graphToIterable(pointed.graph.resolveAgainst(docUri)))
  }

  // TODO move somewhere else
  def resolveAgainst[Rdf <: RDF](nodeMatch: Rdf#NodeMatch, docUri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#NodeMatch = {
    import ops._
    foldNodeMatch[Rdf#NodeMatch](nodeMatch)(ANY, node => node.resolveAgainst(docUri))
  }

  def PATCH[Rdf <: RDF](uri: Rdf#URI, tripleMatches: Iterable[TripleMatch[Rdf]] /*, TODO insertTriples: Iterable[Rdf#Triple]*/)(implicit ops: RDFOps[Rdf]): Free[({type l[+x] = Command[Rdf, x]})#l, Unit] = {
    import ops._
    val docUri = uri.fragmentLess
    val deletePattern = tripleMatches map { case (s, p, o) => (resolveAgainst(s, docUri), resolveAgainst(p, docUri), resolveAgainst(o, docUri)) }
    Command.patch(docUri, deletePattern, List.empty)
  }

  def POSTToCollection[Rdf <: RDF](collection: Rdf#URI, pointed: PointedGraph[Rdf])(implicit ops: RDFOps[Rdf]): Free[({type l[+x] = Command[Rdf, x]})#l, Rdf#URI] = {
    import ops._
    val fragment = pointed.pointer.as[Rdf#URI].get
    // was:
    //  pointed.pointer.as[Rdf#URI] flatMap { uri =>
    //    if (uri.isPureFragment) Success(uri) else Failure(NotPureFragment)
    val docUri = collection.newChildUri
    POST(docUri, pointed) map { _ => docUri.resolve(fragment.toString) }
  }


  def DELETE[Rdf <: RDF](uri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Free[({type l[+x] = Command[Rdf, x]})#l, Unit] = {
    Command.delete(uri)
  }

  def PUT[Rdf <: RDF](ldr: LinkedDataResource[Rdf])(implicit ops: RDFOps[Rdf]): Free[({type l[+x] = Command[Rdf, x]})#l, Unit] = {
    for {
      _ <- DELETE(ldr.location)
      _ <- POST(ldr.location, ldr.resource)
    } yield ()
  }

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
