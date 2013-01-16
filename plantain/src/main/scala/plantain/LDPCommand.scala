package org.w3.banana.plantain

import org.w3.banana._
import scalaz.{ Free, Functor }
import scalaz.Free.Suspend
import scalaz.Free.Return

sealed trait LDPCommand[Rdf <: RDF, +A]

case class CreateLDPR[Rdf <: RDF, A](uri: Option[Rdf#URI], graph: Rdf#Graph, k: Rdf#URI => A) extends LDPCommand[Rdf, A]

case class CreateBinary[Rdf <: RDF, A](uri: Option[Rdf#URI], k: BinaryResource[Rdf] => A ) extends LDPCommand[Rdf, A]

case class GetMeta[Rdf<: RDF, A](uri: Rdf#URI, k: Meta[Rdf] => A ) extends LDPCommand[Rdf, A]

case class GetResource[Rdf <: RDF, A](uri: Rdf#URI, k: NamedResource[Rdf] => A) extends LDPCommand[Rdf, A]

case class DeleteResource[Rdf <: RDF, A](uri: Rdf#URI, a: A) extends LDPCommand[Rdf, A]

case class UpdateLDPR[Rdf <: RDF, A](uri: Rdf#URI, remove: Iterable[TripleMatch[Rdf]], add: Iterable[Rdf#Triple], a: A) extends LDPCommand[Rdf, A]

case class SelectLDPR[Rdf <: RDF, A](uri: Rdf#URI, query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node], k: Rdf#Solutions => A) extends LDPCommand[Rdf, A]

case class ConstructLDPR[Rdf <: RDF, A](uri: Rdf#URI, query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node], k: Rdf#Graph => A) extends LDPCommand[Rdf, A]

case class AskLDPR[Rdf <: RDF, A](uri: Rdf#URI, query: Rdf#AskQuery, bindings: Map[String, Rdf#Node], k: Boolean => A) extends LDPCommand[Rdf, A]

case class SelectLDPC[Rdf <: RDF, A](query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node], k: Rdf#Solutions => A) extends LDPCommand[Rdf, A]

case class ConstructLDPC[Rdf <: RDF, A](query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node], k: Rdf#Graph => A) extends LDPCommand[Rdf, A]

case class AskLDPC[Rdf <: RDF, A](query: Rdf#AskQuery, bindings: Map[String, Rdf#Node], k: Boolean => A) extends LDPCommand[Rdf, A]

object LDPCommand {

  type Script[Rdf <: RDF, A] = Free[({ type l[+x] = LDPCommand[Rdf, x] })#l, A]

  def `return`[Rdf <: RDF, A](a: => A): Script[Rdf, A] =
    Return[({ type l[+x] = LDPCommand[Rdf, x] })#l, A](a)

  def suspend[Rdf <: RDF, A](a: LDPCommand[Rdf, Script[Rdf, A]]): Script[Rdf, A] =
    Suspend[({ type l[+x] = LDPCommand[Rdf, x] })#l, A](a)

  private def nop[Rdf <: RDF]: Script[Rdf, Unit] = `return`(())

  def createLDPR[Rdf <: RDF](uri: Option[Rdf#URI], graph: Rdf#Graph): Script[Rdf, Rdf#URI] =
    suspend(CreateLDPR(uri, graph, uri => `return`(uri)))

  def createBinary[Rdf <: RDF](uri: Option[Rdf#URI]): Script[Rdf, BinaryResource[Rdf]] =
    suspend(CreateBinary(uri, bin => `return`(bin)))


  def getLDPR[Rdf <: RDF, A](uri: Rdf#URI): Script[Rdf, Rdf#Graph] =
    getResource(uri).map{res =>
      res match {
        case ldpr: LDPR[Rdf] =>  ldpr.relativeGraph
        case obj => throw OperationNotSupported("cannot do this operation on a "+obj.getClass)
      }
    }

  def getMeta[Rdf <: RDF,A](uri: Rdf#URI): Script[Rdf, Meta[Rdf]] =
    suspend[Rdf,Meta[Rdf]](GetMeta(uri, ldpr => `return`(ldpr)))

  def getResource[Rdf <: RDF, A](uri: Rdf#URI): Script[Rdf, NamedResource[Rdf]] =
    suspend[Rdf,NamedResource[Rdf]](GetResource(uri, resource => `return`(resource)))

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

  def selectLDPC[Rdf <: RDF](query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node]): Script[Rdf, Rdf#Solutions] =
    suspend(SelectLDPC(query, bindings, solutions => `return`(solutions)))

  def constructLDPC[Rdf <: RDF](query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node]): Script[Rdf, Rdf#Graph] =
    suspend(ConstructLDPC(query, bindings, graph => `return`(graph)))

  def askLDPC[Rdf <: RDF](query: Rdf#AskQuery, bindings: Map[String, Rdf#Node]): Script[Rdf, Boolean] =
    suspend(AskLDPC(query, bindings, b => `return`(b)))

  implicit def ldpCommandFunctor[Rdf <: RDF]: Functor[({ type l[+x] = LDPCommand[Rdf, x] })#l] =
    new Functor[({ type l[+x] = LDPCommand[Rdf, x] })#l] {

      def map[A, B](ldpCommand: LDPCommand[Rdf, A])(f: A => B): LDPCommand[Rdf, B] =
        ldpCommand match {
          case CreateBinary(uri, k) => CreateBinary(uri, x=> f(k(x)))
          case CreateLDPR(uri, graph, k) => CreateLDPR(uri, graph, x => f(k(x)))
          case GetResource(uri, k) => GetResource(uri, x=> f(k(x)))
          case GetMeta(uri, k) => GetMeta(uri, x => f(k(x)))
          case DeleteResource(uri, a) =>  DeleteResource(uri, f(a))
          case UpdateLDPR(uri, remove, add, a) => UpdateLDPR(uri, remove, add, f(a))
          case SelectLDPR(uri, query, bindings, k) => SelectLDPR(uri, query, bindings, x => f(k(x)))
          case ConstructLDPR(uri, query, bindings, k) => ConstructLDPR(uri, query, bindings, x => f(k(x)))
          case AskLDPR(uri, query, bindings, k) => AskLDPR(uri, query, bindings, x => f(k(x)))
          case SelectLDPC(query, bindings, k) => SelectLDPC(query, bindings, x => f(k(x)))
          case ConstructLDPC(query, bindings, k) => ConstructLDPC(query, bindings, x => f(k(x)))
          case AskLDPC(query, bindings, k) => AskLDPC(query, bindings, x => f(k(x)))
        }

    }

}
