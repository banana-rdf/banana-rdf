package org.w3.banana.ldp

import org.w3.banana._
import scalaz.{ Free, Functor }
import scalaz.Free.Suspend
import scalaz.Free.Return
import java.security.Principal

sealed trait LDPCommand[Rdf <: RDF, +A]{
  //the uri on which the action is applied.
  def uri: Rdf#URI
}

sealed trait LDPContainerCmd[Rdf <: RDF, +A] extends LDPCommand[Rdf,A] {
  def uri: Rdf#URI = container
  def container: Rdf#URI //just a synonym for URI when the command can only be run on a container
}

trait Subject {
  def id: List[Principal]
}

case class Subj (id: List[Principal]) extends Subject

case class Admin(onBehalfOf: Subject, id: List[Principal]=List())




case class CreateLDPR[Rdf <: RDF, A](container: Rdf#URI,
                                     slug: Option[String],
                                     graph: Rdf#Graph,
                                     k: Rdf#URI => A) extends LDPContainerCmd[Rdf, A]

case class CreateBinary[Rdf <: RDF, A](container: Rdf#URI,
                                       slug: Option[String],
                                       mime: MimeType,
                                       k: BinaryResource[Rdf] => A ) extends LDPContainerCmd[Rdf, A]

case class CreateContainer[Rdf <: RDF, A](container: Rdf#URI,
                                          slug: Option[String],
                                          graph: Rdf#Graph,
                                          k: Rdf#URI => A) extends LDPContainerCmd[Rdf,A]


case class GetMeta[Rdf<: RDF, A](uri: Rdf#URI,
                                 k: Meta[Rdf] => A ) extends LDPCommand[Rdf, A]


case class GetResource[Rdf <: RDF, A](uri: Rdf#URI,
                                      subject: Option[Subject],
                                      k: NamedResource[Rdf] => A) extends LDPCommand[Rdf, A]


case class DeleteResource[Rdf <: RDF, A](uri: Rdf#URI, a: A) extends LDPCommand[Rdf, A]


case class UpdateLDPR[Rdf <: RDF, A](uri: Rdf#URI,
                                     remove: Iterable[TripleMatch[Rdf]],
                                     add: Iterable[Rdf#Triple],
                                     a: A) extends LDPCommand[Rdf, A]

case class SelectLDPR[Rdf <: RDF, A](uri: Rdf#URI,
                                     query: Rdf#SelectQuery,
                                     bindings: Map[String, Rdf#Node],
                                     k: Rdf#Solutions => A) extends LDPCommand[Rdf, A]

case class ConstructLDPR[Rdf <: RDF, A](uri: Rdf#URI,
                                        query: Rdf#ConstructQuery,
                                        bindings: Map[String, Rdf#Node],
                                        k: Rdf#Graph => A) extends LDPCommand[Rdf, A]


case class AskLDPR[Rdf <: RDF, A](uri: Rdf#URI,
                                  query: Rdf#AskQuery,
                                  bindings: Map[String, Rdf#Node],
                                  k: Boolean => A) extends LDPCommand[Rdf, A]

case class SelectLDPC[Rdf <: RDF, A](container: Rdf#URI,
                                     query: Rdf#SelectQuery,
                                     bindings: Map[String, Rdf#Node],
                                     k: Rdf#Solutions => A) extends LDPContainerCmd[Rdf, A]


case class ConstructLDPC[Rdf <: RDF, A](container: Rdf#URI,
                                        query: Rdf#ConstructQuery,
                                        bindings: Map[String, Rdf#Node],
                                        k: Rdf#Graph => A) extends LDPContainerCmd[Rdf, A]

/**
 *
 * @param uri
 * @param query
 * @param bindings
 * @param k function from boolean to A, boolean would be true if say PATCH changes something.
 * @tparam Rdf
 * @tparam A
 */
case class PatchLDPR[Rdf <: RDF, A](uri: Rdf#URI,
                                    query: Rdf#UpdateQuery,
                                    bindings: Map[String, Rdf#Node],
                                    k: Boolean => A) extends LDPCommand[Rdf, A]


case class AskLDPC[Rdf <: RDF, A](container: Rdf#URI,
                                  query: Rdf#AskQuery,
                                  bindings: Map[String, Rdf#Node],
                                  k: Boolean => A)  extends LDPContainerCmd[Rdf, A]

object LDPCommand {

  type Script[Rdf <: RDF, A] = Free[({ type l[+x] = LDPCommand[Rdf, x] })#l, A]

  def `return`[Rdf <: RDF, A](a: => A): Script[Rdf, A] =
    Return[({ type l[+x] = LDPCommand[Rdf, x] })#l, A](a)

  def suspend[Rdf <: RDF, A](a: LDPCommand[Rdf, Script[Rdf, A]]): Script[Rdf, A] =
    Suspend[({ type l[+x] = LDPCommand[Rdf, x] })#l, A](a)

  private def nop[Rdf <: RDF]: Script[Rdf, Unit] = `return`(())

  def createLDPR[Rdf <: RDF](container: Rdf#URI, slug: Option[String], graph: Rdf#Graph): Script[Rdf, Rdf#URI] =
    suspend(CreateLDPR(container, slug, graph, uri => `return`(uri)))

  def createContainer[Rdf <: RDF](container: Rdf#URI, slug: Option[String], graph: Rdf#Graph): Script[Rdf, Rdf#URI] =
    suspend(CreateContainer(container, slug, graph, uri => `return`(uri)))

  def createBinary[Rdf <: RDF](container: Rdf#URI, slug: Option[String], mime: MimeType): Script[Rdf, BinaryResource[Rdf]] =
    suspend(CreateBinary(container, slug, mime, bin => `return`(bin)))


  def getLDPR[Rdf <: RDF, A](uri: Rdf#URI)(implicit ops: RDFOps[Rdf]): Script[Rdf, Rdf#Graph] =
    getResource(uri).map{res =>
      res match {
        case ldpr: LDPR[Rdf] =>  ldpr.relativeGraph
        case obj => throw OperationNotSupported("cannot do this operation on a "+obj.getClass)
      }
    }

  def getMeta[Rdf <: RDF,A](uri: Rdf#URI): Script[Rdf, Meta[Rdf]] =
    suspend[Rdf,Meta[Rdf]](GetMeta(uri, ldpr => `return`(ldpr)))

  def getResource[Rdf <: RDF, A](uri: Rdf#URI, subj: Option[Subject] = None): Script[Rdf, NamedResource[Rdf]] =
    suspend[Rdf,NamedResource[Rdf]](GetResource(uri, subj, resource => `return`(resource)))

  def deleteResource[Rdf <: RDF](uri: Rdf#URI): Script[Rdf, Unit] =
    suspend(DeleteResource(uri, nop))

  def updateLDPR[Rdf <: RDF](uri: Rdf#URI,
                             remove: Iterable[TripleMatch[Rdf]]=Iterable.empty,
                             add: Iterable[Rdf#Triple]=Iterable.empty): Script[Rdf, Unit] =
    suspend(UpdateLDPR(uri, remove, add, nop))

  def selectLDPR[Rdf <: RDF](uri: Rdf#URI, query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node]): Script[Rdf, Rdf#Solutions] =
    suspend(SelectLDPR(uri, query, bindings, solutions => `return`(solutions)))

  def constructLDPR[Rdf <: RDF](uri: Rdf#URI, query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node]): Script[Rdf, Rdf#Graph] =
    suspend(ConstructLDPR(uri, query, bindings, graph => `return`(graph)))

  def askLDPR[Rdf <: RDF](uri: Rdf#URI, query: Rdf#AskQuery, bindings: Map[String, Rdf#Node]): Script[Rdf, Boolean] =
    suspend(AskLDPR(uri, query, bindings, b => `return`(b)))

  def patchLDPR[Rdf <: RDF](uri: Rdf#URI, query: Rdf#UpdateQuery, bindings: Map[String, Rdf#Node]): Script[Rdf, Boolean] =
    suspend(PatchLDPR(uri, query, bindings, b => `return`(b)))

  def selectLDPC[Rdf <: RDF](container: Rdf#URI, query: Rdf#SelectQuery,
                             bindings: Map[String, Rdf#Node]): Script[Rdf, Rdf#Solutions] =
    suspend(SelectLDPC(container, query, bindings, solutions => `return`(solutions)))

  def constructLDPC[Rdf <: RDF](container: Rdf#URI, query: Rdf#ConstructQuery,
                                bindings: Map[String, Rdf#Node]): Script[Rdf, Rdf#Graph] =
    suspend(ConstructLDPC(container, query, bindings, graph => `return`(graph)))

  def askLDPC[Rdf <: RDF](container: Rdf#URI, query: Rdf#AskQuery,
                          bindings: Map[String, Rdf#Node]): Script[Rdf, Boolean] =
    suspend(AskLDPC(container, query, bindings, b => `return`(b)))

  implicit def ldpCommandFunctor[Rdf <: RDF]: Functor[({ type l[+x] = LDPCommand[Rdf, x] })#l] =
    new Functor[({ type l[+x] = LDPCommand[Rdf, x] })#l] {

      def map[A, B](ldpCommand: LDPCommand[Rdf, A])(f: A => B): LDPCommand[Rdf, B] =
        ldpCommand match {
          case CreateBinary(uric, slug, mime, k) => CreateBinary(uric, slug, mime, x=> f(k(x)))
          case CreateLDPR(uric, slug, graph, k) => CreateLDPR(uric, slug, graph, x => f(k(x)))
          case CreateContainer(uric, slug, graph, k) => CreateContainer(uric, slug, graph, x => f(k(x)))
          case GetResource(uri, subj, k) => GetResource(uri, subj, x=> f(k(x)))
          case GetMeta(uri, k) => GetMeta(uri, x => f(k(x)))
          case DeleteResource(uri, a) =>  DeleteResource(uri, f(a))
          case UpdateLDPR(uri, remove, add, a) => UpdateLDPR(uri, remove, add, f(a))
          case SelectLDPR(uri, query, bindings, k) => SelectLDPR(uri, query, bindings, x => f(k(x)))
          case ConstructLDPR(uri, query, bindings, k) => ConstructLDPR(uri, query, bindings, x => f(k(x)))
          case AskLDPR(uri, query, bindings, k) => AskLDPR(uri, query, bindings, x => f(k(x)))
          case PatchLDPR(uri, query, bindings, k) => PatchLDPR(uri, query, bindings, x => f(k(x)))
          case SelectLDPC(uric, query, bindings, k) => SelectLDPC(uric, query, bindings, x => f(k(x)))
          case ConstructLDPC(uric, query, bindings, k) => ConstructLDPC(uric, query, bindings, x => f(k(x)))
          case AskLDPC(uric, query, bindings, k) => AskLDPC(uric, query, bindings, x => f(k(x)))
        }

    }

}
