package org.w3.banana.ldp

import scala.language.reflectiveCalls

import org.w3.banana._
import java.nio.file._
import scala.concurrent._
import akka.actor._
import akka.util._
import akka.pattern.ask
import org.slf4j.LoggerFactory
import java.util.Date
import play.api.libs.iteratee._
import play.api.libs.iteratee.Input.El
import java.io.{IOException, OutputStream}
import scala.util.Try
import java.net.{URI => jURI}
import scala.Some
import scalaz.\/-
import scalaz.-\/

trait RActor extends Actor with akka.actor.ActorLogging {

  /**
   * @param path of the resource
   * @return the pair consisting of the collection and the name of the resource to make a request on
   */
  def split(path: String): Pair[String, String] = {
    val i = path.lastIndexOf('/')
    if (i <0) ("",path)
    else (path.substring(0,i+1),path.substring(i+1,path.length))
  }

  def returnErrors[A,B](pf: Receive): Receive = new PartialFunction[Any,Unit] {
    //interestingly it seems we can't catch an error here! If we do, we have to return a true or a false
    // and whatever we choose it could have bad sideffects. What happens if the isDefinedAt throws an exception?
      def isDefinedAt(x: Any): Boolean = pf.isDefinedAt(x)
      def apply(a: Any): Unit = try {
        log.info(s"received $a");
        pf.apply(a)
      } catch {
        case e: Exception => sender ! akka.actor.Status.Failure(e)
      }
    }

  def local(u: jURI, base: jURI): Option[String] = {
    if ((!u.isAbsolute ) || (u.getScheme == base.getScheme && u.getHost == base.getHost && u.getPort == base.getPort)) {
      if (u.getPath.startsWith(base.getPath)) {
        val res = Some(u.getPath.substring(base.getPath.size))
        res
      } else None
    } else None
  }

}

// A resource on the server ( Resource is already taken. )
// note:
// There can be named and unamed resources, as when a POST creates a
// resource that is not given a name... so this should probably extend a more abstract resource
trait NamedResource[Rdf<:RDF] extends Meta[Rdf] {
   def uri: Rdf#URI
}

/**
 * Metadata about a resource
 *   This may be thought to be so generic that a graph representation would do,
 *   but it is very likely to be very limited set of properties and so to be
 *   better done in form methods for efficiency reasons.
 */
trait Meta[Rdf <: RDF] {
  def uri: Rdf#URI

  def ops: RDFOps[Rdf]

  def updated: Option[Date]
  /*
 * A resource should ideally be versioned, so any change would get a version URI
 * ( but this is probably something that should be on a MetaData trait
 **/
  def version: Option[Rdf#URI] = None

  /**
   * location of initial ACL for this resource
   * This initial implementation is too hard wired. IT should be something that is settable at configuration time
   **/
  lazy val acl: Option[Rdf#URI] = Some{
    if (uri.toString.endsWith(";acl")) uri
    else ops.URI(uri.toString+";acl")
  }

  //other metadata candidates:
  // - owner
  // - etag
  //

}


/**
 * A binary resource does not get direct semantic interpretation.
 * It has a mime type. One can write bytes to it, to replace its content, or one
 * can read its content.
 * @tparam Rdf
 */
trait BinaryResource[Rdf<:RDF] extends NamedResource[Rdf]  {

  def size: Option[Long]

  def mime: MimeType

  // creates a new BinaryResource, with new time stamp, etc...
  def write:  Iteratee[Array[Byte], BinaryResource[Rdf]]
  def reader(chunkSize: Int): Enumerator[Array[Byte]]
}

/*
 * TODO
 * - an LDPS must subscribe to the death of its LDPC
 */

trait LDPR[Rdf <: RDF] extends NamedResource[Rdf]  {
  def uri: Rdf#URI // *no* trailing slash

  def graph: Rdf#Graph // all uris are relative to uri

  /* the graph such that all URIs are relative to $uri */
  def relativeGraph: Rdf#Graph
}

trait LDPC[Rdf <: RDF] extends NamedResource[Rdf] {
  def uri: Rdf#URI // *with* a trailing slash
  def execute[A](script: LDPCommand.Script[Rdf, A]): Future[A]
}


case class OperationNotSupported(msg: String) extends Exception(msg)

case class BinaryRes[Rdf<:RDF](root: Path, uri: Rdf#URI)(implicit val ops: RDFOps[Rdf]) extends BinaryResource[Rdf] {
  import org.w3.banana.syntax.URISyntax.uriW

  lazy val path = root.resolve(uriW(uri).lastPathSegment)


  // also should be on a metadata trait, since all resources have update times
  def updated = Try { new Date(Files.getLastModifiedTime(path).toMillis) }.toOption

  val size = Try { Files.size(path) }.toOption

  def mime = ???

  // creates a new BinaryResource, with new time stamp, etc...
  //here I can just write to the file, as that should be a very quick operation, which even if it blocks,
  //should be extreemly fast server side.  Iteratee
  def write: Iteratee[Array[Byte], BinaryRes[Rdf] ] = {
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
case class LDPRes[Rdf<:RDF](uri: Rdf#URI,
                                  graph: Rdf#Graph,
                                  updated: Option[Date] = Some(new Date))
                                 (implicit val ops: RDFOps[Rdf]) extends LDPR[Rdf] {
  import org.w3.banana.syntax._

  def relativeGraph: Rdf#Graph =graph.resolveAgainst(uri)
}


case class Scrpt[Rdf<:RDF,A](script:LDPCommand.Script[Rdf,A])
case class Cmd[Rdf<:RDF,A](command: LDPCommand[Rdf, LDPCommand.Script[Rdf,A]])


object RWWeb {

  val logger = LoggerFactory.getLogger(this.getClass)

  def apply[Rdf<:RDF](baseUri: Rdf#URI, root: Path, cache: Option[Props])
                     (implicit ops: RDFOps[Rdf], timeout: Timeout = Timeout(5000)): RWWeb[Rdf] =
    new RWWeb(baseUri)


}

case class ParentDoesNotExist(message: String) extends Exception(message) with BananaException
case class ResourceExists(message: String) extends Exception(message) with BananaException
case class AccessDenied(message: String) extends Exception(message) with BananaException



trait RWW[Rdf <: RDF] {  //not sure which of exec or execute is going to be needed
  def execute[A](script: LDPCommand.Script[Rdf,A]): Future[A]
  def exec[A](cmd: LDPCommand[Rdf, LDPCommand.Script[Rdf,A]]): Future[A]
  /*
   * these two functions look wrong and very Java-y.
   * I don't know what the right patter to do this is. Setting it up in a constructor seems too
   * restrictive
   */
  def setWebActor(webActor: ActorRef)
  def setLDPSActor(ldpsActor: ActorRef)
  def shutdown(): Unit
}

class RWWeb[Rdf<:RDF](val baseUri: Rdf#URI)
                           (implicit ops: RDFOps[Rdf], timeout: Timeout) extends RWW[Rdf] {
  val system = ActorSystem("plantain")
  val rwwActorRef = system.actorOf(Props(new RWWebActor(baseUri)),name="rww")
  import RWWeb.logger

  logger.info(s"Created rwwActorRef=<$rwwActorRef>")


  def execute[A](script: LDPCommand.Script[Rdf, A]) = {
    (rwwActorRef ? Scrpt[Rdf,A](script)).asInstanceOf[Future[A]]
  }

  def exec[A](cmd: LDPCommand[Rdf, LDPCommand.Script[Rdf,A]]) = {
    (rwwActorRef ? Cmd(cmd)).asInstanceOf[Future[A]]
  }

  def shutdown(): Unit = {
    system.shutdown()
  }

  def setWebActor(ref: ActorRef) {
    rwwActorRef ! WebActor(ref)
  }

  def setLDPSActor(ldpsActor: ActorRef) {
    rwwActorRef ! LDPSActor(ldpsActor)
  }
}


case class WebActor(web: ActorRef)
case class LDPSActor(ldps: ActorRef)

class RWWebActor[Rdf<:RDF](val baseUri: Rdf#URI)
                             (implicit ops: RDFOps[Rdf], timeout: Timeout) extends RActor {
  import syntax.URISyntax.uriW
  import ops._

  var rootContainer: Option[ActorRef] = None
  var web : Option[ActorRef] = None


  def receive = returnErrors {
    case Scrpt(script) => {
       script.resume match {
         case command: -\/[LDPCommand[Rdf, LDPCommand.Script[Rdf,_]]] => forwardSwitch(Cmd(command.a))
         case \/-(res) => sender ! res
       }
    }
    case cmd: Cmd[Rdf,_] => forwardSwitch(cmd)
    case WebActor(webActor) => {
      log.info(s"setting web actor to <$webActor> ")
      web = Some(webActor)
    }
    case LDPSActor(ldps) => {
       log.info(s"setting rootContainer to <$ldps> ")
       rootContainer = Some(ldps)
    }
  }

  /** We in fact ignore the R and A types, since we cannot capture */
  protected def forwardSwitch[A](cmd: Cmd[Rdf,A]) {
      local(cmd.command.uri.underlying,baseUri.underlying).map { path=>
        rootContainer match {
          case Some(root) => {
            val (coll,file) = split(path)
            val p = if (""==coll) root.path else root.path/coll.split('/').toIterable
            log.info(s"sending message $cmd to akka('$path')=$p ")
            context.actorFor(p) forward cmd
          }
          case None => log.warning("RWWebActor not set up yet: missing rootContainer")
        }
    } getOrElse {
      //todo: this relative uri comparison is too simple.
      //     really one should look to see if it
      //     is the same host and then send it to the local lpdserver ( because a remote server may
      //     link to this server ) and if so there is no need to go though the external http layer to
      //     fetch graphs
      web.map {
        log.info(s"sending message $cmd to general web agent <$web>")
        _ forward cmd
      }.getOrElse(log.warning("RWWebActor not set up yet: missing web actor"))
    }

  }


}