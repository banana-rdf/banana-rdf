package org.w3.banana.plantain

import scala.language.reflectiveCalls

import org.w3.banana._
import java.nio.file._
import scala.concurrent._
import akka.actor._
import akka.util._
import akka.pattern.ask
import org.openrdf.query.algebra.evaluation.TripleSource
import org.slf4j.LoggerFactory
import org.w3.banana.plantain.PlantainOps._
import annotation.tailrec
import java.util.Date
import play.api.libs.iteratee._
import java.io.{IOException, OutputStream}
import scala.util.Try
import java.net.{URI => jURI}
import scala.Some
import org.w3.banana.plantain.PlantainRWW.Cmd
import scalaz.\/-
import scalaz.-\/
import play.api.libs.iteratee.Input.El

import org.w3.banana.plantain.PlantainRWW.Script

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

case class PlantainBinary(root: Path, uri: Plantain#URI) extends BinaryResource[Plantain] {

  lazy val path = root.resolve(uri.lastPathSegment)


  // also should be on a metadata trait, since all resources have update times
  def updated = Try { new Date(Files.getLastModifiedTime(path).toMillis) }.toOption

  val size = Try { Files.size(path) }.toOption

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

  val ops = Plantain.ops
}

/**
 * it's important for the uris in the graph to be absolute
 * this invariant is assumed by the sparql engine (TripleSource)
 */
case class PlantainLDPR(uri: URI, graph: Graph, updated: Option[Date] = Some(new Date)) extends LDPR[Plantain] {

  def relativeGraph: Graph =
    graph.triples.foldLeft(Graph.empty){ (current, triple) => current + triple.relativizeAgainst(uri) }

  val ops = Plantain.ops
}

class PlantainLDPC(val uri: URI, actorRef: ActorRef, val updated: Option[Date] = Some(new Date))(implicit timeout: Timeout) extends LDPC[Plantain] {

  override def toString: String = uri.toString

  def execute[A](script: Plantain#Script[A]): Future[A] = {
    (actorRef ? Script(script)).asInstanceOf[Future[A]]
  }

  def ops = Plantain.ops

 }

class PlantainLDPCActor(baseUri: URI, root: Path) extends RActor {
  val NonLDPRs = scala.collection.mutable.Map.empty[String, NamedResource[Plantain]]
  // invariant to be preserved: the Graph are always relative to 

  val LDPRs = scala.collection.mutable.Map("" -> PlantainLDPR(baseUri,plantain.Graph.empty),
    ";acl"-> PlantainLDPR(plantain.URI.fromString(baseUri.underlying.toString+";acl"), Graph.empty))



  val tripleSource: TripleSource = new TMapTripleSource(LDPRs)

  /**
   *
   * @param script
   * @param t
   * @tparam A
   * @throws NoSuchElementException if the resource does not exist
   * @return
   */
  @tailrec
  final def run[A](sender: ActorRef, script: Plantain#Script[A]) {
    script.resume match {
      case -\/(cmd) => {
        if(local(cmd.uri.underlying,baseUri.underlying).exists(!_.contains('/'))) {
          val script = runLocalCmd(cmd)
          run(sender, script)
        }
        else {
          val a = context.actorFor("/user/rww")
          log.info(s"sending to $a")
          a forward Cmd(cmd) }
      }
      case \/-(a) => {
        log.info(s"returning to $sender $a")
        sender ! a
      }
    }
  }

  /**
   * Runs a command that can be evaluated on this container.
   * @param cmd the command to evaluate
   * @tparam A The final return type of the script
   * @return a script for further evaluation
   */
  def runLocalCmd[A](cmd: LDPCommand[Plantain, Plantain#Script[A]]): Plantain#Script[A] = {
    log.info(s"in RunLocalCmd - received $cmd")
    cmd match {
      case CreateLDPR(_, slugOpt, graph, k) => {
        val (uri, pathSegment) = deconstruct(slugOpt)
        val ldpr = PlantainLDPR(uri, graph.resolveAgainst(uri))
        LDPRs.put(pathSegment, ldpr)
        if (!pathSegment.endsWith(";acl"))
          LDPRs.put(pathSegment+";acl",PlantainLDPR(ldpr.acl.get, Graph.empty))
        k(uri)
      }
      case CreateBinary(_, slugOpt, mime: MimeType, k) => {
        val (uri, pathSegment) = deconstruct(slugOpt)
        //todo: make sure the uri does not end in ";meta" or whatever else the meta standard will be
        val bin = PlantainBinary(root, uri)
        NonLDPRs.put(pathSegment, bin)
        k(bin)
      }
      case CreateContainer(_,slugOpt,graph,k) => {
         val (uri,pathSegment) = deconstruct(slugOpt) //todo: deconstruct should check the file system. This should in fact use a file sytem call
         val p = root.resolve(pathSegment)
         Files.createDirectory(p)
         val dirUri = uri/""
         context.actorOf(Props(new PlantainLDPCActor(dirUri, p)),pathSegment)
         k(dirUri)
      }
      case GetResource(uri, agent, k) => {
        val path = uri.lastPathSegment
        val res = LDPRs.get(path).getOrElse(NonLDPRs(path))
        k(res)
      }
      case GetMeta(uri, k) => {
        //todo: GetMeta here is very close to GetResource, as currently there is no big work difference between the two
        //The point of GetMeta is mostly to remove work if there were work that was very time
        //consuming ( such as serialising a graph )
        val path = uri.lastPathSegment
        val res = LDPRs.get(path).getOrElse(NonLDPRs(path))
        k(res.asInstanceOf[Meta[Plantain]])
      }
      case DeleteResource(uri, a) => {
        LDPRs.remove(uri.lastPathSegment).orElse {
          NonLDPRs.remove(uri.lastPathSegment)
        } orElse (throw new NoSuchElementException("Could not find resource " + uri))
        a //todo: why no function here?
      }
      case UpdateLDPR(uri, remove, add, a) => {
        val pathSegment = uri.lastPathSegment
        val graph = LDPRs.get(pathSegment).map(_.graph).getOrElse {
          throw new NoSuchElementException(s"Resource does not exist at $uri with path segment '$pathSegment'")
        }
        val temp = remove.foldLeft(graph) {
          (graph, tripleMatch) => graph - tripleMatch.resolveAgainst(uri.resolveAgainst(baseUri))
        }
        val resultGraph = add.foldLeft(temp) {
          (graph, triple) => graph + triple.resolveAgainst(uri.resolveAgainst(baseUri))
        }
        val ldpr = PlantainLDPR(uri, resultGraph)
        LDPRs.put(pathSegment, ldpr)
        a //todo: why no function here?
      }
      case SelectLDPR(uri, query, bindings, k) => {
        val graph = LDPRs(uri.lastPathSegment).graph
        val solutions = PlantainUtil.executeSelect(graph, query, bindings)
        k(solutions)
      }
      case ConstructLDPR(uri, query, bindings, k) => {
        val graph = LDPRs(uri.lastPathSegment).graph
        val resultGraph = PlantainUtil.executeConstruct(graph, query, bindings)
        k(resultGraph)
      }
      case AskLDPR(uri, query, bindings, k) => {
        val graph = LDPRs(uri.lastPathSegment).graph
        val resultGraph = PlantainUtil.executeAsk(graph, query, bindings)
        k(resultGraph)
      }
      case SelectLDPC(_,query, bindings, k) => {
        val solutions = PlantainUtil.executeSelect(tripleSource, query, bindings)
        k(solutions)
      }
      case ConstructLDPC(_,query, bindings, k) => {
        val graph = PlantainUtil.executeConstruct(tripleSource, query, bindings)
        k(graph)
      }
      case AskLDPC(_,query, bindings, k) => {
        val b = PlantainUtil.executeAsk(tripleSource, query, bindings)
        k(b)
      }
    }
  }


  protected def deconstruct[A](slugOpt: Option[String]): (Plantain#URI, String) = {
    import PlantainRWW.randomPathSegment
    slugOpt match {
      case None => {
        val pathSegment = randomPathSegment
        val uri = baseUri / pathSegment
        (uri, pathSegment)
      }
      case Some(slug) => {
         val safeSlug = slug.replace('/','_')
         if (LDPRs.get(safeSlug) == None) (baseUri/safeSlug,safeSlug)
         else deconstruct(None)
      }
    }
  }

  def receive = {
    returnErrors {
      case Script(script) => {
          run(sender, script)
      }
      case Cmd(command) => {
        val script =runLocalCmd(command)
        log.info(s"returned $script ")
        run(sender, script)
      }
    }
  }

}


object PlantainRWW {

  case class Script[A](script: Plantain#Script[A])
  case class Cmd[A](command: LDPCommand[Plantain, Plantain#Script[A]])
  val logger = LoggerFactory.getLogger(classOf[PlantainRWW])

  def apply(baseUri: URI, root: Path, cache: Option[Props])(implicit timeout: Timeout = Timeout(5000)): PlantainRWW =
    new PlantainRWW(baseUri, root, cache)

  def randomPathSegment(): String = java.util.UUID.randomUUID().toString.replaceAll("-", "")

}

case class ParentDoesNotExist(message: String) extends Exception(message) with BananaException
case class ResourceExists(message: String) extends Exception(message) with BananaException
case class AccessDenied(message: String) extends Exception(message) with BananaException



trait RWW[Rdf <: RDF] {  //not sure which of exec or execute is going to be needed
  def execute[A](script: LDPCommand.Script[Rdf,A]): Future[A]
  def exec[A](cmd: LDPCommand[Plantain, Plantain#Script[A]]): Future[A]
  def shutdown(): Unit
}

class PlantainRWW(val baseUri: URI, root: Path, cache: Option[Props])(implicit timeout: Timeout) extends RWW[Plantain] {
  val system = ActorSystem("plantain")
  val rwwActorRef = system.actorOf(Props(new PlantainRWWeb(baseUri,root)),name="rww")
  val cacheRef = cache.map {p => system.actorOf(p,name="web") }
  val ldpcRoot = system.actorOf(Props(new PlantainLDPCActor(baseUri, root)),"rootContainer")
  import PlantainRWW.logger

  logger.info(s"Created root container=<$ldpcRoot>")
  logger.info(s"Created web actor <$cacheRef>")
  logger.info(s"Created rwwActorRef=<$rwwActorRef>")


  def execute[A](script: LDPCommand.Script[Plantain, A]) = {
    (rwwActorRef ? Script[A](script)).asInstanceOf[Future[A]]
  }

  def exec[A](cmd: LDPCommand[Plantain, Plantain#Script[A]]) = {
    (rwwActorRef ? cmd).asInstanceOf[Future[A]]
  }

  def shutdown(): Unit = {
    system.shutdown()
  }
}




//ldps is provisional, one should get rid of it
class PlantainRWWeb(val baseUri: URI, root: Path)(implicit timeout: Timeout) extends RActor {

  val rootContainerPath = context.parent.path/"rootContainer"
  lazy val web = context.system.actorFor(context.parent.path/"web")

  log.info(s"rootContainer=<$rootContainerPath> webcache=<$web>  ")

  def receive = returnErrors {
    case Script(script) => {
       script.resume match {
         case -\/(command) => forwardSwitch(command)
         case \/-(res) => sender ! res
       }
    }
    case Cmd(cmd) => forwardSwitch(cmd)
  }

  def forwardSwitch[A](cmd: LDPCommand[Plantain, Plantain#Script[A]]) {
    local(cmd.uri.underlying,baseUri.underlying).map { path=>
      // this is a request for a local actor. Here we can distinguish between LDPCs as those that end in /
      val (coll,file) = split(path)
      val p = if (""==coll) rootContainerPath else rootContainerPath/coll.split('/').toIterable
      log.info(s"sending message to akka('$path')=$p ")
      context.actorFor(p) forward Cmd(cmd)
    } getOrElse {
      log.info(s"PlantainRWWeb, sending message to general web agent <$web>")
      //todo: this relative uri comparison is too simple.
      //     really one should look to see if it
      //     is the same host and then send it to the local lpdserver ( because a remote server may
      //     link to this server ) and if so there is no need to go though the external http layer to
      //     fetch graphs
      web forward Cmd(cmd)
    }
  }


}