package org.w3.banana.ldp

import scala.language.reflectiveCalls

import org.w3.banana._
import java.nio.file._
import akka.actor._
import org.openrdf.query.algebra.evaluation.TripleSource
import annotation.tailrec
import java.net.{URI => jURI}
import scala.Some
import scalaz.\/-
import scalaz.-\/
import org.w3.banana.plantain.Plantain
import collection.parallel.mutable

/**
 * A LDP Container actor that is responsible for the equivalent of a directory
 *
 *
 * @param baseUri the URI for the container
 * @param root the path on the file system where data is saved to
 * @param ops
 * @param sparqlGraph
 */
class PlantainLDPCActor(baseUri: Plantain#URI, root: Path)
                                 (implicit ops: RDFOps[Plantain],
                                     sparqlGraph: SparqlGraph[Plantain]) extends RActor {
  import org.w3.banana.syntax._
  import scala.collection.mutable._
  import ops._

  val NonLDPRs: Map[String,NamedResource[Plantain]] = Map.empty[String, NamedResource[Plantain]]
  // invariant to be preserved: the Graph are always relative to

  val LDPRs: Map[String,LocalLDPR[Plantain]] = scala.collection.mutable.Map("" -> LocalLDPR[Plantain](baseUri,Graph.empty),
    ";acl"-> LocalLDPR[Plantain](URI(baseUri.toString+";acl"), Graph.empty))


  val tripleSource: TripleSource = new TMapTripleSource(LDPRs)

  def randomPathSegment(): String = java.util.UUID.randomUUID().toString.replaceAll("-", "")

  /**
   *
   * @param script
   * @param t
   * @tparam A
   * @throws NoSuchElementException if the resource does not exist
   * @return
   */
  @tailrec
  final def run[A](sender: ActorRef, script: LDPCommand.Script[Plantain,A]) {
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
  OneForOneStrategy

  /**
   * Runs a command that can be evaluated on this container.
   * @param cmd the command to evaluate
   * @tparam A The final return type of the script
   * @return a script for further evaluation
   */
  def runLocalCmd[A](cmd: LDPCommand[Plantain, LDPCommand.Script[Plantain,A]]): LDPCommand.Script[Plantain,A] = {
    log.info(s"in RunLocalCmd - received $cmd")

    cmd match {
      case CreateLDPR(_, slugOpt, graph, k) => {
        val (uri, pathSegment) = deconstruct(slugOpt)
        val ldpr = LocalLDPR[Plantain](uri, graphW[Plantain](graph).resolveAgainst(uri))
        LDPRs.put(pathSegment, ldpr)
        if (!pathSegment.endsWith(";acl"))
          LDPRs.put(pathSegment+";acl",LocalLDPR[Plantain](ldpr.acl.get, Graph.empty))
        k(uri)
      }
      case CreateBinary(_, slugOpt, mime: MimeType, k) => {
        val (uri, pathSegment) = deconstruct(slugOpt)
        //todo: make sure the uri does not end in ";meta" or whatever else the meta standard will be
        val bin = LocalBinaryR[Plantain](root, uri)
        NonLDPRs.put(pathSegment, bin)
        k(bin)
      }
      case CreateContainer(_,slugOpt,graph,k) => {
         val (uri,pathSegment) = deconstruct(slugOpt) //todo: deconstruct should check the file system. This should in fact use a file sytem call
         val p = root.resolve(pathSegment)
         Files.createDirectory(p)
         val dirUri = uriW[Plantain](uri)/""
         context.actorOf(Props(new PlantainLDPCActor(dirUri, p)),pathSegment)
         k(dirUri)
      }
      case GetResource(uri, agent, k) => {
        val path = uriW[Plantain](uri).lastPathSegment
        val res = LDPRs.get(path).getOrElse(NonLDPRs(path))
        k(res)
      }
      case GetMeta(uri, k) => {
        //todo: GetMeta here is very close to GetResource, as currently there is no big work difference between the two
        //The point of GetMeta is mostly to remove work if there were work that was very time
        //consuming ( such as serialising a graph )
        val path = uriW[Plantain](uri).lastPathSegment
        val res = LDPRs.get(path).getOrElse(NonLDPRs(path))
        k(res.asInstanceOf[Meta[Plantain]])
      }
      case DeleteResource(uri, a) => {
        val name = uriW[Plantain](uri).lastPathSegment
        log.info(s"DeleteResource($uri,$a).name=$name")
        if (name == "") {
          import scalax.file.Path
          context.stop(self)
          Path(root.toFile).deleteRecursively(true)
        } else LDPRs.remove(name).orElse {
          NonLDPRs.remove(name)
        } orElse (throw new NoSuchElementException("Could not find resource " + uri))
        a //todo: why no function here?
      }
      case UpdateLDPR(uri, remove, add, a) => {
        val pathSegment = uriW[Plantain](uri).lastPathSegment
        val graph = LDPRs.get(pathSegment).map(_.graph).getOrElse {
          throw new NoSuchElementException(s"Resource does not exist at $uri with path segment '$pathSegment'")
        }
        val temp = remove.foldLeft(graph) {
          (graph, tripleMatch) => graph - tripleMatch.resolveAgainst(uriW[Plantain](uri).resolveAgainst(baseUri))
        }
        val resultGraph = add.foldLeft(temp) {
          (graph, triple) => graph + triple.resolveAgainst(uriW[Plantain](uri).resolveAgainst(baseUri))
        }
        val ldpr = LocalLDPR[Plantain](uri, resultGraph)
        LDPRs.put(pathSegment, ldpr)
        a //todo: why no function here?
      }
      case SelectLDPR(uri, query, bindings, k) => {
        val graph = LDPRs(uri.lastPathSegment).graph
        val solutions = sparqlGraph(graph).executeSelect(query, bindings)
        k(solutions)
      }
      case ConstructLDPR(uri, query, bindings, k) => {
        val graph = LDPRs(uri.lastPathSegment).graph
        val resultGraph = sparqlGraph(graph).executeConstruct(query, bindings)
        k(resultGraph)
      }
      case AskLDPR(uri, query, bindings, k) => {
        val graph = LDPRs(uri.lastPathSegment).graph
        val resultGraph = sparqlGraph(graph).executeAsk(query, bindings)
        k(resultGraph)
      }
//      case SelectLDPC(_,query, bindings, k) => {
//        val solutions = PlantainUtil.executeSelect(tripleSource, query, bindings)
//        k(solutions)
//      }
//      case ConstructLDPC(_,query, bindings, k) => {
//        val graph = PlantainUtil.executeConstruct(tripleSource, query, bindings)
//        k(graph)
//      }
//      case AskLDPC(_,query, bindings, k) => {
//        val b = PlantainUtil.executeAsk(tripleSource, query, bindings)
//        k(b)
//      }
    }
  }


  protected def deconstruct[A](slugOpt: Option[String]): (Plantain#URI, String) = {
    slugOpt match {
      case None => {
        val pathSegment = randomPathSegment
        val uri = uriW[Plantain](baseUri) / pathSegment
        (uri, pathSegment)
      }
      case Some(slug) => {
         val safeSlug = slug.replace('/','_')
         if (LDPRs.get(safeSlug) == None) (uriW[Plantain](baseUri)/safeSlug,safeSlug)
         else deconstruct(None)
      }
    }
  }

  def receive = returnErrors {
      case s: Scrpt[Plantain,_]  => {
          run(sender, s.script)
      }
      case cmd: Cmd[Plantain,_] => {
        val script =runLocalCmd(cmd.command)
        log.info(s"returned $script ")
        run(sender, script)
      }
    }

}
