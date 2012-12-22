package org.w3.banana.plantain

import org.w3.banana._
import java.nio.file._
import scala.concurrent._
import scala.concurrent.stm._
import akka.actor._
import akka.util._
import akka.pattern.{ ask, pipe }
import akka.transactor._
import scalaz.{ -\/, \/- , Free }
import org.openrdf.model.{ URI => SesameURI, _ }
import org.openrdf.model.impl._
import org.openrdf.query.algebra.evaluation.TripleSource
import org.openrdf.query.QueryEvaluationException
import info.aduna.iteration.CloseableIteration
import PlantainUtil._
import org.slf4j.{ Logger, LoggerFactory }
import PlantainLDPS._
import PlantainOps.{ uriSyntax, nodeSyntax, tripleSyntax, tripleMatchSyntax }
import annotation.tailrec
import java.util.Date
import play.api.libs.iteratee.{Error, Enumerator, Iteratee}
import java.io.{File, IOException, OutputStream}
import play.api.libs.iteratee.Input.El
import scala.util.Try
import scala.language.reflectiveCalls

// A resource on the server ( Resource is already taken. )
// note:
// There can be named and unamed resources, as when a POST creates a
// resource that is not given a name...
trait LocalResource[Rdf<:RDF] {
   def uri: Rdf#URI
}

///**
// * Metadata about a resource
// *   This may be thought to be so generic that a graph representation would do,
// *   but it is very likely to be very limited set of properties and so to be
// *   better done in form methods for efficiency reasons.
// */
//trait Meta[Rdf <: RDF]  {
//
//  def uri: Rdf#URI
//
//  def updated: Date
//
//// def tpe:
//
//  /**
//   * location of initial ACL for this resource
//   **/
//  def acl: Option[Rdf#URI]
//
//  //other metadata candidates:
//  // - owner
//  // - etag
//  //
//
//}
//

/**
 * A binary resource does not get direct semantic interpretation.
 * It has a mime type. One can write bytes to it, to replace its content, or one
 * can read its content.
 * @tparam Rdf
 */
trait BinaryResource[Rdf<:RDF] extends LocalResource[Rdf] {
  /*
   * A resource should ideally be versioned, so any change would get a version URI
   * ( but this is probably something that should be on a MetaData trait
   **/
  def version: Rdf#URI

  def size: Try[Long]

  // also should be on a metadata trait, since all resources have update times
  def updated: Try[Date]
  def mime: MimeType

  // creates a new BinaryResource, with new time stamp, etc...
  def write:  Iteratee[Array[Byte], BinaryResource[Rdf]]
  def reader(chunkSize: Int): Enumerator[Array[Byte]]
}

/*
 * TODO
 * - an LDPS must subscribe to the death of its LDPC
 */

trait LDPR[Rdf <: RDF] extends LocalResource[Rdf] {
  def uri: Rdf#URI // *no* trailing slash

  def graph: Rdf#Graph // all uris are relative to uri

  /* the graph such that all URIs are relative to $uri */
  def relativeGraph: Rdf#Graph
}

trait LDPC[Rdf <: RDF] extends LocalResource[Rdf] {
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

case class OperationNotSupported(msg: String) extends Exception(msg)

case class PlantainBinary(root: Path, uri: Plantain#URI) extends BinaryResource[Plantain] {

  lazy val path = root.resolve(uri.lastPathSegment)

  /*
   * A resource should ideally be versioned, so any change would get a version URI
   * ( but this is probably something that should be on a MetaData trait
   **/
  def version = ???

  // also should be on a metadata trait, since all resources have update times
  def updated = Try { new Date(Files.getLastModifiedTime(path).toMillis) }

  val size = Try { Files.size(path) }

  def mime = ???

  // creates a new BinaryResource, with new time stamp, etc...
  //here I can just write to the file, as that should be a very quick operation, which even if it blocks,
  //should be extreemly fast server side.  Iteratee
  def write: Iteratee[Array[Byte], PlantainBinary ] = {
    val tmpfile = Files.createTempFile(path.getParent,path.getFileName.toString,"tmp")
    val out = Files.newOutputStream(tmpfile, StandardOpenOption.WRITE)
    val i = Iteratee.fold[Array[Byte],OutputStream](out){ (out, bytes ) =>
      try {
        out.write(bytes)
      } catch {
        case outerr: IOException => Error("Problem writing bytes: "+outerr, El(bytes))
      }
      out
    }
    i.mapDone{ _ =>
       Files.move(tmpfile,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING)
       this // we can return this
    }
  }

  //this will probably require an agent to push things along.
  def reader(chunkSize: Int=1024*8) = Enumerator.fromFile(path.toFile,chunkSize)
}

/**
 * it's important for the uris in the graph to be absolute
 * this invariant is assumed by the sparql engine (TripleSource)
 */
case class PlantainLDPR(uri: URI, graph: Graph) extends LDPR[Plantain] {

  def relativeGraph: Graph =
    graph.triples.foldLeft(Graph.empty){ (current, triple) => current + triple.relativizeAgainst(uri) }


}

class PlantainLDPC(val uri: URI, actorRef: ActorRef)(implicit timeout: Timeout) extends LDPC[Plantain] {

  override def toString: String = uri.toString

  def execute[A](script: Plantain#Script[A]): Future[A] = {
    (actorRef ? Coordinated(Script(script))).asInstanceOf[Future[A]]
  }

}

class PlantainLDPCActor(baseUri: URI, root: Path) extends Actor {
  import org.w3.banana.plantain.Plantain.diesel._
  val NonLDPRs = TMap.empty[String, LocalResource[Plantain]]
  // invariant to be preserved: the Graph are always relative to 

  val LDPRs = TMap.empty[String, PlantainLDPR]


  val tripleSource: TripleSource = new TMapTripleSource(LDPRs)

  @tailrec
  final def run[A](coordinated: Coordinated, script: Plantain#Script[A])(implicit t: InTxn): A = {
    import PlantainOps._
    script.resume match {
      case -\/(CreateLDPR(uriOpt, graph, k)) => {
        val (uri, pathSegment) = deconstruct(uriOpt)
        val ldpr = PlantainLDPR(uri, graph.resolveAgainst(uri))
        LDPRs.put(pathSegment, ldpr)
        run(coordinated, k(uri))
      }
      case -\/(CreateBinary(uriOpt, k)) => {
        val (uri, pathSegment) = deconstruct(uriOpt)
        //todo: make sure the uri does not end in ";meta" or whatever else the meta standard will be
        val bin = PlantainBinary(root,uri)
        NonLDPRs.put(pathSegment, bin)
        run(coordinated, k(bin))
      }
      case -\/(GetResource(uri,k)) => {
        val path = uri.lastPathSegment
        val res = LDPRs.get(path).getOrElse(NonLDPRs(path))
        run(coordinated, k(res))
      }
      case -\/(GetMeta(uri,k)) => {
        val metaURI = if (uri.toString.endsWith(";meta")) uri else URI(uri.toString+";meta")
        val meta = LDPRs.get(metaURI.lastPathSegment) getOrElse PlantainLDPR(metaURI,emptyGraph)
        run(coordinated,k(meta))
      }
      case -\/(DeleteResource(uri, a)) => {
         LDPRs.remove(uri.lastPathSegment).orElse{
           NonLDPRs.remove(uri.lastPathSegment)
         }
        run(coordinated, a)
      }
      case -\/(UpdateLDPR(uri, remove, add, a)) => {
        val pathSegment = uri.lastPathSegment
        val graph = LDPRs.get(pathSegment).map(_.graph) getOrElse emptyGraph
        val temp = remove.foldLeft(graph) {
          (graph, tripleMatch) => graph - tripleMatch.resolveAgainst(uri)
        }
        val resultGraph = add.foldLeft(temp) {
          (graph, triple) => graph + triple.resolveAgainst(uri)
        }
        val ldpr = PlantainLDPR(uri, resultGraph)
        LDPRs.put(pathSegment, ldpr)
        run(coordinated, a)
      }
      case -\/(SelectLDPR(uri, query, bindings, k)) => {
        val graph = LDPRs(uri.lastPathSegment).graph
        val solutions = PlantainUtil.executeSelect(graph, query, bindings)
        run(coordinated, k(solutions))
      }
      case -\/(ConstructLDPR(uri, query, bindings, k)) => {
        val graph = LDPRs(uri.lastPathSegment).graph
        val resultGraph = PlantainUtil.executeConstruct(graph, query, bindings)
        run(coordinated, k(graph))
      }
      case -\/(AskLDPR(uri, query, bindings, k)) => {
        val graph = LDPRs(uri.lastPathSegment).graph
        val b = PlantainUtil.executeAsk(graph, query, bindings)
        run(coordinated, k(b))
      }
      case -\/(SelectLDPC(query, bindings, k)) => {
        val solutions = PlantainUtil.executeSelect(tripleSource, query, bindings)
        run(coordinated, k(solutions))
      }
      case -\/(ConstructLDPC(query, bindings, k)) => {
        val graph = PlantainUtil.executeConstruct(tripleSource, query, bindings)
        run(coordinated, k(graph))
      }
      case -\/(AskLDPC(query, bindings, k)) => {
        val b = PlantainUtil.executeAsk(tripleSource, query, bindings)
        run(coordinated, k(b))
      }
      case \/-(a) => a

    }
  }


  protected def deconstruct[A](uriOpt: Option[Plantain#URI]): (Plantain#URI, String) = {
    uriOpt match {
      case None => {
        val pathSegment = randomPathSegment
        val uri = baseUri / pathSegment
        (uri, pathSegment)
      }
      case Some(uri) => (uri, uri.lastPathSegment)
    }
  }

  def receive = {
    case coordinated @ Coordinated(Script(script)) => coordinated atomic { implicit t =>
      try {
      val r = run(coordinated, script)
      sender ! r
      } catch {
        case e: Exception => sender ! akka.actor.Status.Failure(e)
      }
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
