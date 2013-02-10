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
import org.w3.banana.plantain.PlantainLDPS.Cmd
import scalaz.\/-
import org.w3.banana.WrongExpectation
import scalaz.-\/
import play.api.libs.iteratee.Input.El
import org.w3.banana.plantain.PlantainLDPS.DeleteLDPC
import org.w3.banana.plantain.PlantainLDPS.GetLDPC
import org.w3.banana.plantain.PlantainLDPS.CreateLDPC
import org.w3.banana.plantain.PlantainLDPS.Script

trait RActor extends Actor {
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
      def apply(a: Any): Unit = try { System.out.println(s"${context.self} received $a"); pf.apply(a) } catch {
        case e: Exception => sender ! akka.actor.Status.Failure(e)
      }
    }

  def local(u: jURI, base: jURI): Option[String] = {
    if ((!u.isAbsolute ) || (u.getScheme == base.getScheme && u.getHost == base.getHost && u.getPort == base.getPort)) {
      if (u.getPath.startsWith(base.getPath)) {
        val res = Some(u.getPath.substring(base.getPath.size))
        System.out.println("res="+res)
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
    if (uri.toString.endsWith(";meta")) uri
    else ops.URI(uri.toString+";meta")
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
    ";meta"-> PlantainLDPR(plantain.URI.fromString(baseUri.underlying.toString+";meta"), Graph.empty))



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
          val a = context.actorFor("/user/web")
          System.out.println(s"sending to $a")
          a forward Cmd(cmd) }
      }
      case \/-(a) => {
        System.out.println(s"returning to $sender $a")
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
    System.out.println(s"in RunLocalCmd - received $cmd")
    cmd match {
      case CreateLDPR(_, slugOpt, graph, k) => {
        val (uri, pathSegment) = deconstruct(slugOpt)
        val ldpr = PlantainLDPR(uri, graph.resolveAgainst(uri))
        LDPRs.put(pathSegment, ldpr)
        if (!pathSegment.endsWith(";meta"))
          LDPRs.put(pathSegment+";meta",PlantainLDPR(ldpr.acl.get, Graph.empty))
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
      case GetResource(uri, k) => {
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
    import PlantainLDPS.randomPathSegment
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
        System.out.println(s"returned $script ")
        run(sender, script)
      }
    }
  }

}


object PlantainLDPS {

  case class CreateLDPC(uri: URI)
  case class GetLDPC(uri: URI)
  case class DeleteLDPC(uri: URI)
  case class Script[A](script: Plantain#Script[A])
  case class Cmd[A](command: LDPCommand[Plantain, Plantain#Script[A]])
  val logger = LoggerFactory.getLogger(classOf[PlantainLDPS])

  def apply(baseUri: URI, root: Path)(implicit timeout: Timeout = Timeout(5000)): PlantainLDPS =
    new PlantainLDPS(baseUri, root)

  def randomPathSegment(): String = java.util.UUID.randomUUID().toString.replaceAll("-", "")

}

case class ParentDoesNotExist(message: String) extends Exception(message) with BananaException
case class ResourceExists(message: String) extends Exception(message) with BananaException
case class AccessDenied(message: String) extends Exception(message) with BananaException

class PlantainLDPSActor(baseUri: URI, root: Path, system: ActorSystem)(implicit timeout: Timeout) extends RActor {

  val ldpcActorRef = system.actorOf(Props(new PlantainLDPCActor(baseUri, root)),"rootContainer")
  System.out.println(s"created ldpc=<$ldpcActorRef>")

  val LDPCs = scala.collection.mutable.Map((URI(new jURI(""))->ldpcActorRef))

  def valid(uri: URI): Option[jURI] = {
    val norm = baseUri.underlying.relativize(uri.underlying.normalize())
    if (norm.isAbsolute) {
      sender ! akka.actor.Status.Failure ( WrongExpectation(s"collection does not fetch remote URIs such as $uri") )
      None
    } else if (norm.toString.startsWith("..")) {
      sender ! akka.actor.Status.Failure ( AccessDenied(s"cannot access beneath root $baseUri as requested by $uri" ) )
      None
    } else {
      Some(norm)
    }
  }

  def receive = returnErrors {
    case CreateLDPC(uri) => {
      val fullUri = uri.resolveAgainst(baseUri)

      valid(uri).map { normedUri =>
        val path = root.resolve(normedUri.toString)
       if (Files.exists(path)) {
          sender ! akka.actor.Status.Failure(ResourceExists(s"collection at <$baseUri> with name '$normedUri'exists"))
        } else {
          if (Files.exists(path.getParent)) {
            Files.createDirectory(path)
            val ldpcActorRef = system.actorOf(Props(new PlantainLDPCActor(fullUri, path)))
            LDPCs.put(URI(normedUri), ldpcActorRef)
            val ldpc = new PlantainLDPC(fullUri, ldpcActorRef)
            sender ! ldpc
          } else {
            sender ! akka.actor.Status.Failure(
              ParentDoesNotExist(s"parent does not exist for <$fullUri> create it first."))
          }
        }
      }
    }
    case GetLDPC(uri) => {
      val ldpcActorRef = LDPCs(URI(baseUri.underlying.relativize(uri.underlying)))
      val ldpc = new PlantainLDPC(uri.resolveAgainst(baseUri), ldpcActorRef)
      sender ! ldpc
    }
    case DeleteLDPC(uri) =>  {
      valid(uri).map { normedUri =>
        val LDPCsubset = LDPCs.retain{ case (path,_) => path.toString.startsWith(normedUri.toString) }
        LDPCsubset foreach { case (_, ldpcActorRef) => system.stop(ldpcActorRef) }
        import scalax.file.Path
        //todo: do we loose something by not using java7's Path?
        //for more on scalax.io see http://daily-scala.blogspot.fr/2012/07/introducing-scala-io-this-is-start-of.html
        Path(root.resolve(normedUri.toString).toFile).deleteRecursively(true)
        sender ! ()
      }
    }
  }
}

class PlantainLDPS(val baseUri: URI, root: Path)(implicit timeout: Timeout) extends LDPS[Plantain] {

  val system = ActorSystem("plantain")

  val ldpsActorRef = system.actorOf(Props(new PlantainLDPSActor(baseUri, root, system)), name = "store")

  System.out.println(s"created ldps=<$ldpsActorRef>")

  def shutdown(): Unit = {
    system.shutdown()
  }

  def createLDPC(uri: URI): Future[PlantainLDPC] = {
    (ldpsActorRef ? CreateLDPC(uri)).asInstanceOf[Future[PlantainLDPC]]
  }

  def getLDPC(uri: URI): Future[PlantainLDPC] = {
    (ldpsActorRef ? GetLDPC(uri)).asInstanceOf[Future[PlantainLDPC]]
  }

  def deleteLDPC(uri: URI): Future[Unit] = {
    (ldpsActorRef ? DeleteLDPC(uri)).asInstanceOf[Future[Unit]]
  }

}

trait RWW[Rdf <: RDF] {  //not sure which of exec or execute is going to be needed
  def execute[A](script: LDPCommand.Script[Rdf,A]): Future[A]
  def exec[A](cmd: LDPCommand[Plantain, Plantain#Script[A]]): Future[A]
}

class PlantainRWW(ldps: PlantainLDPS)(implicit timeout: Timeout) extends RWW[Plantain] {
  val webActorRef = ldps.system.actorOf(Props(new PlantainRWWeb(ldps)),name="web")

  def execute[A](script: LDPCommand.Script[Plantain, A]) = {
    (webActorRef ? Script[A](script)).asInstanceOf[Future[A]]
  }

  def exec[A](cmd: LDPCommand[Plantain, Plantain#Script[A]]) = {
    (webActorRef ? cmd).asInstanceOf[Future[A]]
  }
}




//ldps is provisional, one should get rid of it
class PlantainRWWeb(ldps: PlantainLDPS)(implicit timeout: Timeout) extends RActor {

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
    local(cmd.uri.underlying,ldps.baseUri.underlying).map { path=>
      // this is a request for a local actor. Here we can distinguish between LDPCs as those that end in /
      val (coll,file) = split(path)
      val rootContainer = ldps.ldpsActorRef.path.parent/"rootContainer"
      val p = if (""==coll) rootContainer else rootContainer/coll.split('/').toIterable
      System.out.println(s"sending message to akka('$path')=$p ")
      context.actorFor(p) forward Cmd(cmd)
    } orElse {
      System.out.println("in orElse")
      null
      //todo: this relative uri comparison is too simple.
      //     really one should look to see if it
      //     is the same host and then send it to the local lpdserver ( because a remote server may
      //     link to this server ) and if so there is no need to go though the external http layer to
      //     fetch graphs
      //send to web cache
    }
  }


}