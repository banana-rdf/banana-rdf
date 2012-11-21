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
import PlantainLDPS._
import PlantainOps.{ uriSyntax, nodeSyntax, tripleSyntax, tripleMatchSyntax }

/*
 * TODO
 * - an LDPS must subscribe to the death of its LDPC
 */

trait LDPR[Rdf <: RDF] {
  def uri: Rdf#URI // *no* trailing slash
  def graph: Rdf#Graph // all uris are relative to uri
}

trait LDPC[Rdf <: RDF] {
  def uri: Rdf#URI // *with* a trailing slash
  def execute[A](script: LDPCommand.Script[Rdf, A]): Future[A]
}

trait LDPS[Rdf <: RDF] {
  def baseUri: Rdf#URI
  def shutdown(): Unit
  def createLDPC(uri: Rdf#URI): Future[LDPC[Rdf]]
  def getLDPC(uri: Rdf#URI): Future[LDPC[Rdf]]
  def deleteLDPC(uri: Rdf#URI): Future[Unit]
}



/**
 * it's important for the uris in the graph to be absolute
 * this invariant is assumed by the sparql engine (TripleSource)
 */
case class PlantainLDPR(uri: URI, graph: Graph) extends LDPR[Plantain] {

  /* the graph such that all URIs are relative to $uri */
  def relativeGraph: Graph = {
    graph.triples.foldLeft(Graph.empty){ (current, triple) => current + triple.relativizeAgainst(uri) }
  }

}

class PlantainLDPC(val uri: URI, actorRef: ActorRef)(implicit timeout: Timeout) extends LDPC[Plantain] {

  override def toString: String = uri.toString

  def execute[A](script: Plantain#Script[A]): Future[A] = {
    (actorRef ? Coordinated(Script(script))).asInstanceOf[Future[A]]
  }

}

class PlantainLDPCActor(baseUri: URI, root: Path) extends Actor {

  // invariant to be preserved: the Graph are always relative to 
  val LDPRs = TMap.empty[String, PlantainLDPR]

  val tripleSource: TripleSource = new TMapTripleSource(LDPRs)

  def run[A](coordinated: Coordinated, script: Plantain#Script[A])(implicit t: InTxn): A = {
    script.resume fold (
      {
        case CreateLDPR(uriOpt, graph, a) => {
          import PlantainOps._
          val (uri, pathSegment) = uriOpt match {
            case None => {
              val pathSegment = randomPathSegment
              val uri = baseUri / pathSegment
              (uri, pathSegment)
            }
            case Some(uri) => (uri, uri.lastPathSegment)
          }
          val ldpr = PlantainLDPR(uri, graph.resolveAgainst(uri))
          LDPRs.put(pathSegment, ldpr)
          run(coordinated, a)
        }
        case GetLDPR(uri, k) => {
          val ldpr = LDPRs(uri.lastPathSegment)
          run(coordinated, k(ldpr.relativeGraph))
        }
        case DeleteLDPR(uri, a) => {
          LDPRs.remove(uri.lastPathSegment)
          run(coordinated, a)
        }
        case UpdateLDPR(uri, remove, add, a) => {
          val pathSegment = uri.lastPathSegment
          val graph = LDPRs.get(pathSegment).map(_.graph) getOrElse Graph.empty
          val temp = remove.foldLeft(graph){ (graph, tripleMatch) => graph - tripleMatch.resolveAgainst(uri) }
          val resultGraph = add.foldLeft(temp){ (graph, triple) => graph + triple.resolveAgainst(uri) }
          val ldpr = PlantainLDPR(uri, resultGraph)
          LDPRs.put(pathSegment, ldpr)
          run(coordinated, a)
        }
        case SelectLDPR(uri, query, bindings, k) => {
          val graph = LDPRs(uri.lastPathSegment).graph
          val solutions = PlantainUtil.executeSelect(graph, query, bindings)
          run(coordinated, k(solutions))
        }
        case ConstructLDPR(uri, query, bindings, k) => {
          val graph = LDPRs(uri.lastPathSegment).graph
          val resultGraph = PlantainUtil.executeConstruct(graph, query, bindings)
          run(coordinated, k(graph))
        }
        case AskLDPR(uri, query, bindings, k) => {
          val graph = LDPRs(uri.lastPathSegment).graph
          val b = PlantainUtil.executeAsk(graph, query, bindings)
          run(coordinated, k(b))
        }
        case SelectLDPC(query, bindings, k) => {
          val solutions = PlantainUtil.executeSelect(tripleSource, query, bindings)
          run(coordinated, k(solutions))
        }
        case ConstructLDPC(query, bindings, k) => {
          val graph = PlantainUtil.executeConstruct(tripleSource, query, bindings)
          run(coordinated, k(graph))
        }
        case AskLDPC(query, bindings, k) => {
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


object PlantainLDPS {

  case class CreateLDPC(uri: URI)
  case class GetLDPC(uri: URI)
  case class DeleteLDPC(uri: URI)
  case class Script[A](script: Plantain#Script[A])

  val logger = LoggerFactory.getLogger(classOf[PlantainLDPS])

  def apply(baseUri: URI, root: Path)(implicit timeout: Timeout = Timeout(5000)): PlantainLDPS =
    new PlantainLDPS(baseUri, root)

  def randomPathSegment(): String = java.util.UUID.randomUUID().toString.replaceAll("-", "")

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
    case coordinated @ Coordinated(DeleteLDPC(uri)) => coordinated atomic { implicit t =>
      val ldpcActorRefOpt = LDPCs.remove(uri)
      ldpcActorRefOpt foreach { ldpcActorRef => system.stop(ldpcActorRef) }
      sender ! ()
    }
  }

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
