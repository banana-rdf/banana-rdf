package org.w3.banana.ldp

import org.w3.banana._
import akka.actor.ActorRef
import collection.mutable
import concurrent.{ExecutionContext, Future}
import scalaz.\/-
import scalaz.-\/
import util.Failure
import util.Success

/**
* A LDP actor that interacts with remote LDP resources
*
* @param excluding base of URIs that the local server is listening to. So don't fetch those URIs externally
* @param fetcher   fetches resources off the web. Important for testing, among other things.
* @tparam Rdf
*/
class LDPWebActor[Rdf<:RDF](val excluding: Rdf#URI, val fetcher: ResourceFetcher[Rdf])
                           (implicit ops: RDFOps[Rdf], sparqlGraph: SparqlGraph[Rdf], ec: ExecutionContext) extends RActor {

  import org.w3.banana.syntax._

  val cache = mutable.HashMap[Rdf#URI,Future[NamedResource[Rdf]]]()

  def fetch(uri: Rdf#URI): Future[NamedResource[Rdf]] = {
    val url = uri.underlying
    log.info(s"WebProxy: fetching $uri")
    cache.get(uri).getOrElse {
      val result = fetcher.fetch(url.toURL)
      cache.put(uri, result)
      result
    }
  }

  def fetchGraph(uri: Rdf#URI): Future[LDPR[Rdf]] = {
    fetch(uri).flatMap { res =>
      res match {
        case ldpr: LDPR[Rdf] => Future.successful(ldpr)
        case _ => Future.failed(WrongTypeException("remote resource cannot be transformed to a graph"))
      }
    }
  }


  /**
   * Runs a command that can be evaluated on this container.
   * @param cmd the command to evaluate
   * @tparam A The final return type of the script
   * @return a script for further evaluation
   */
  def runLocalCmd[A](cmd: LDPCommand[Rdf, LDPCommand.Script[Rdf,A]]) {
    log.info(s"in RunLocalCmd - received $cmd")

    cmd match {
      //      case CreateLDPR(_, slugOpt, graph, k) => {
      //        val ldpr = PlantainLDPR(uri, graph.resolveAgainst(uri))
      //        LDPRs.put(pathSegment, ldpr)
      //        k(uri)
      //      }
      //      case CreateBinary(_, slugOpt, mime: MimeType, k) => {
      //        val (uri, pathSegment) = deconstruct(slugOpt)
      //        //todo: make sure the uri does not end in ";meta" or whatever else the meta standard will be
      //        val bin = PlantainBinary(root, uri)
      //        NonLDPRs.put(pathSegment, bin)
      //        k(bin)
      //      }
      //      case CreateContainer(_,slugOpt,graph,k) => {
      //        val (uri,pathSegment) = deconstruct(slugOpt) //todo: deconstruct should check the file system. This should in fact use a file sytem call
      //        val p = root.resolve(pathSegment)
      //        Files.createDirectory(p)
      //        context.actorOf(Props(new PlantainLDPCActor(uri, p)),pathSegment)
      //        k(uri)
      //      }
      case GetResource(uri,_, k) => {
        val result = fetch(uri)
        val sender = context.sender  //very important. Calling in function onComplete will return deadLetter
        result.onComplete { tryres =>
          tryres match {
            case Success(response) => {
              log.info(s"cache received $response")
              self tell (k(response),sender)
            }
            case Failure(e) => {
              log.info(s"Cache failed with $e")
              sender ! akka.actor.Status.Failure({
                e match {
                  case be : BananaException => be
                  case other: Throwable => WrappedException("failure fetching resource", other.getCause)
                }
              })
            }
          }
        }
      }
      //      case GetMeta(uri, k) => {
      //        //todo: GetMeta here is very close to GetResource, as currently there is no big work difference between the two
      //        //The point of GetMeta is mostly to remove work if there were work that was very time
      //        //consuming ( such as serialising a graph )
      //        val path = uri.lastPathSegment
      //        val res = LDPRs.get(path).getOrElse(NonLDPRs(path))
      //        k(res.asInstanceOf[Meta[Rdf]])
      //      }
      //      case DeleteResource(uri, a) => {
      //        LDPRs.remove(uri.lastPathSegment).orElse {
      //          NonLDPRs.remove(uri.lastPathSegment)
      //        } orElse (throw new NoSuchElementException("Could not find resource " + uri))
      //        a //todo: why no function here?
      //      }
      //      case UpdateLDPR(uri, remove, add, a) => {
      //        val pathSegment = uri.lastPathSegment
      //        val graph = LDPRs.get(pathSegment).map(_.graph).getOrElse {
      //          throw new NoSuchElementException(s"Resource does not exist at $uri with path segment '$pathSegment'")
      //        }
      //        val temp = remove.foldLeft(graph) {
      //          (graph, tripleMatch) => graph - tripleMatch.resolveAgainst(uri.resolveAgainst(baseUri))
      //        }
      //        val resultGraph = add.foldLeft(temp) {
      //          (graph, triple) => graph + triple.resolveAgainst(uri.resolveAgainst(baseUri))
      //        }
      //        val ldpr = PlantainLDPR(uri, resultGraph)
      //        LDPRs.put(pathSegment, ldpr)
      //        a //todo: why no function here?
      //      }
      case SelectLDPR(uri, query, bindings, k) => {
        val sender = context.sender  //very important. Calling in function onComplete will return deadLetter
        log.info(s"received SelectLDPR($uri,_,_,_) from ${sender}")
        val result = fetchGraph(uri)
        result.onComplete{ tryRes =>
          tryRes match {
            case Success(ldpr) => {
              val solutions = sparqlGraph(ldpr.graph).executeSelect(query, bindings)
              log.info(s"sending solutions for SelectLDPR($uri,_,_) to $self with sender ${sender}")
              self tell  (Scrpt(k(solutions)),sender)
            }
            case Failure(e) => sender ! akka.actor.Status.Failure({
              log.info(s"sederror cause=${e.getCause} stack trace =${e.getStackTraceString}")
              e match {
                case e: BananaException => e
                case other: Throwable => WrappedException("failure fetching resource", other.getCause)
              }
            })
          }

        }
      }
      //      case ConstructLDPR(uri, query, bindings, k) => {
      //        val graph = LDPRs(uri.lastPathSegment).graph
      //        val resultGraph = PlantainUtil.executeConstruct(graph, query, bindings)
      //        k(resultGraph)
      //      }
      //      case AskLDPR(uri, query, bindings, k) => {
      //        val graph = LDPRs(uri.lastPathSegment).graph
      //        val resultGraph = PlantainUtil.executeAsk(graph, query, bindings)
      //        k(resultGraph)
      //      }
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


  /**
   * @param script
   * @param t
   * @tparam A
   * @throws NoSuchElementException if the resource does not exist
   */
  final def run[A](sender: ActorRef, script: LDPCommand.Script[Rdf,A]) {
    script.resume match {
      case -\/(cmd) => {
        if(local(cmd.uri.underlying,excluding.underlying) == None) {
          runLocalCmd(cmd)
          //if we were to have some commands return an immediate value, then we could do
          // the following with the returned script
          //  run(sender, script)
        }
        else {
          val a = context.actorFor("/user/web")
          log.info(s"forwarding to $a")
          a forward Cmd(cmd)
        }
      }
      case \/-(a) => {
        log.info(s"returning to $sender $a")
        sender ! a
      }
    }
  }


  def receive = returnErrors {
    case s: Scrpt[Rdf,_]  => {
      run(sender, s.script)
    }
    case cmd: Cmd[Rdf,_] => {
      runLocalCmd(cmd.command)
    }
  }
}
