package org.w3.banana.plantain

import org.w3.banana._
import java.nio.file._
import scala.concurrent._
import scala.concurrent.stm._
import akka.actor._
import akka.util._
import akka.pattern.{ ask, pipe }
import akka.transactor._
import scalaz.Free
import org.openrdf.model.{ URI => SesameURI, _ }
import org.openrdf.model.impl._
import org.openrdf.query.algebra.evaluation.TripleSource
import org.openrdf.query.QueryEvaluationException
import info.aduna.iteration.CloseableIteration
import PlantainUtil._
import org.slf4j.{ Logger, LoggerFactory }

/*
 * TODO
 * - an LDPS must subscribe to the death of its LDPC
 */

trait LDPR[Rdf <: RDF] {
  def uri: Rdf#URI // *no* trailing slash
  def graph: Rdf#Graph
}

case class PlantainLDPR(uri: URI, graph: Graph) extends LDPR[Plantain]

trait LDPC[Rdf <: RDF] extends RDFStore[Rdf, Future] {
  def uri: Rdf#URI // *with* a trailing slash
}

object StoreActor {
  case class CreateLDPC(uri: URI)
  case class GetLDPC(uri: URI)
  case class DeleteLDPC(uri: URI)
  case class Script[A](script: Plantain#Script[A])
}

import StoreActor._

class PlantainLDPC(val uri: URI, actorRef: ActorRef)(implicit timeout: Timeout) extends LDPC[Plantain] {

  def shutdown(): Unit = ()

  def execute[A](script: Plantain#Script[A]): Future[A] = {
    (actorRef ? Coordinated(Script(script))).asInstanceOf[Future[A]]
  }

}

class PlantainLDPCActor(baseUri: URI, root: Path) extends Actor {

  val graphs = TMap.empty[URI, Graph]

  val tripleSource: TripleSource = new TMapTripleSource(graphs)

  def run[A](coordinated: Coordinated, script: Plantain#Script[A])(implicit t: InTxn): A = {
    script.resume fold (
      {
        case Create(uri, a) => {
          graphs.put(uri, Graph.empty)
          run(coordinated, a)
        }
        case Delete(uri, a) => {
          graphs.remove(uri)
          run(coordinated, a)
        }
        case Get(uri, k) => {
          val graph = graphs(uri)
          run(coordinated, k(graph))
        }
        case Append(uri, triples, a) => {
          val current = graphs.get(uri) getOrElse Graph.empty
          val graph = triples.foldLeft(current){ _ + _ }
          graphs.put(uri, graph)
          run(coordinated, a)
        }
        case Remove(uri, tripleMatches, a) => {
          val current = graphs.get(uri) getOrElse Graph.empty
          val graph = tripleMatches.foldLeft(current){ _ - _ }
          graphs.put(uri, graph)
          run(coordinated, a)
        }
        case Select(query, bindings, k) => {
          val solutions = PlantainUtil.executeSelect(tripleSource, query, bindings)
          run(coordinated, k(solutions))
        }
        case Construct(query, bindings, k) => {
          val graph = PlantainUtil.executeConstruct(tripleSource, query, bindings)
          run(coordinated, k(graph))
        }
        case Ask(query, bindings, k) => {
          val b = PlantainUtil.executeAsk(tripleSource, query, bindings)
          run(coordinated, k(b))
        }
      },
      a => a
    )
  }

  def receive = {
    case coordinated @ Coordinated(Script(script)) => coordinated atomic { implicit t =>
      val r = run(coordinated, script)
      sender ! r
    }
  }

}

trait LDPS[Rdf <: RDF] {
  def baseUri: Rdf#URI
  def shutdown(): Unit
  def createLDPC(uri: URI): Future[LDPC[Rdf]]
  def getLDPC(uri: URI): Future[LDPC[Rdf]]
  def deleteLDPC(uri: URI): Future[Unit]
}

object PlantainLDPS {

  val logger = LoggerFactory.getLogger(classOf[PlantainLDPS])

  def apply(baseUri: URI, root: Path)(implicit timeout: Timeout = Timeout(5000)): PlantainLDPS =
    new PlantainLDPS(baseUri, root)

}

class PlantainLDPS(val baseUri: URI, root: Path)(implicit timeout: Timeout) extends LDPS[Plantain] {

  val system = ActorSystem("plantain")

  val ldpsActorRef = system.actorOf(Props(new PlantainLDPSActor(baseUri, root, system)), name = "store")

  def shutdown(): Unit = {
    system.shutdown()
  }

  def createLDPC(uri: URI): Future[PlantainLDPC] = {
    (ldpsActorRef ? Coordinated(CreateLDPC(uri))).asInstanceOf[Future[PlantainLDPC]]
  }

  def getLDPC(uri: URI): Future[PlantainLDPC] = {
    (ldpsActorRef ? Coordinated(GetLDPC(uri))).asInstanceOf[Future[PlantainLDPC]]
  }

  def deleteLDPC(uri: URI): Future[Unit] = {
    (ldpsActorRef ? Coordinated(DeleteLDPC(uri))).asInstanceOf[Future[Unit]]
  }

}

class PlantainLDPSActor(baseUri: URI, root: Path, system: ActorSystem)(implicit timeout: Timeout) extends Actor {

  val LDPCs = TMap.empty[URI, ActorRef] // PlantainLDPCActor

  def receive = {
    case coordinated @ Coordinated(CreateLDPC(uri)) => coordinated atomic { implicit t =>
      val ldpcActorRef = system.actorOf(Props(new PlantainLDPCActor(uri, root)))
      LDPCs.put(uri, ldpcActorRef)
      val ldpc = new PlantainLDPC(uri, ldpcActorRef)
      sender ! ldpc
    }
    case coordinated @ Coordinated(GetLDPC(uri)) => coordinated atomic { implicit t =>
      val ldpcActorRef = LDPCs(uri)
      val ldpc = new PlantainLDPC(uri, ldpcActorRef)
      sender ! ldpc
    }
    case coordinated @ Coordinated(GetLDPC(uri)) => coordinated atomic { implicit t =>
      val ldpcActorRefOpt = LDPCs.remove(uri)
      ldpcActorRefOpt foreach { ldpcActorRef => system.stop(ldpcActorRef) }
      sender ! ()
    }
  }

}
