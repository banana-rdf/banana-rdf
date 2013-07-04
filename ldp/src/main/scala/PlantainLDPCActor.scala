package org.w3.banana.ldp

import scala.language.reflectiveCalls

import org.w3.banana._
import java.nio.file._
import akka.actor._
import org.openrdf.query.algebra.evaluation.TripleSource
import annotation.tailrec
import java.net.{URI => jURI}
import org.w3.banana.plantain.{PlantainLDPatch, PlantainUtil, Plantain}
import java.nio.file.attribute.BasicFileAttributes
import java.util
import org.w3.banana.syntax._
import scalaz.-\/
import scalaz.\/-
import scala.Some
import java.nio.file.Path
import scala.util.{Success, Failure, Try}
import java.util.Date
import scala.io.Codec
import java.io.{File, FileOutputStream}
import java.nio.file.DirectoryStream.Filter
import scala.collection.parallel.mutable
import org.w3.banana.ldp.ResourceDoesNotExist

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
                                     sparqlGraph: SparqlGraph[Plantain]) extends PlantainLDPRActor(baseUri,root) {
  import org.w3.banana.syntax._
  import ops._


  val ldp = LDPPrefix[Plantain]
  val rdfs = RDFSPrefix[Plantain]
  val rdf = RDFPrefix[Plantain]



  override lazy val fileName = "index"

  override def preStart {
     //start all agents for all files and subdirectories
    //start starting directories lazily may in many cases be a better option, especially for systems with a huge
    //number of directories.... http://stackoverflow.com/questions/16633515/creating-akka-actor-hierarchies-lazily
    //currently we do no optimization, just to make code simpler. Optimization to come later.
    //todo: memory optimizations
    //todo: handle exceptions
    //todo: deal with index file...

    Files.walkFileTree(root,util.Collections.emptySet(), 1,
      new SimpleFileVisitor[Path] {

        override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes) = {
          if (dir == root) super.preVisitDirectory(dir,attrs)
          else FileVisitResult.SKIP_SUBTREE
        }

        override def visitFile(file: Path, attrs: BasicFileAttributes) = {
          val pathSegment = file.getFileName.toString
          if (attrs.isDirectory) {
            val fullURI = uriW[Plantain](baseUri)/(pathSegment+"/")
            context.actorOf(Props(new PlantainLDPCActor(fullURI,root.resolve(file))),pathSegment)
          } else if (attrs.isSymbolicLink) {
            val fullURI = uriW[Plantain](baseUri)/pathSegment
            //we use symbolic links to point to the file that contains the default representation
            //this is because we may have many different representations, and we don't want an actor
            //for each of the representations.
            context.actorOf(Props(new PlantainLDPRActor(fullURI,root.resolve(file))),pathSegment)
          }
        FileVisitResult.CONTINUE
      }
    })
    val ldpcuri = URI("")
    val ldpcGraph = Graph(Triple(ldpcuri,rdf.typ,ldp.Container))
    val fileGraph = context.children.foldLeft(ldpcGraph){
      case (g,ref)=> g + Triple(ldpcuri,rdfs.member,URI(ref.path.name))
    }
    setResource(fileName,fileGraph)
  }


  def randomPathSegment(): String = java.util.UUID.randomUUID().toString.replaceAll("-", "")

  protected def aclPath(path: String) = path+".acl"

  protected def isAclPath(path: String) = path.endsWith(".acl")

  /**
   * for directories we resolve requests for metadata files as referring to .xxx.ext files
   * inside the directory
   *
   * @param name
   * @return
   */
  override
  def fileFrom(name: String): File = {
    //note any request to this method means that the initial request started with collection.xxxx
    //so we know that the first part of the string is a collection
    val doti = name.indexOf('.')
    val afterDot = if (doti>=0) name.substring(doti) else ""
    root.resolve(afterDot+ext).toFile
  }


  /**
   * Runs a command that can be evaluated on this container.
   * @param cmd the command to evaluate
   * @tparam A The final return type of the script
   * @return a script for further evaluation
   */
  override
  def runLocalCmd[A](cmd: LDPCommand[Plantain, LDPCommand.Script[Plantain,A]]) {
    log.info(s"in PlantainLDPCActor.runLocalCmd - received $cmd")

    cmd match {
      case CreateLDPR(_, slugOpt, graph, k) => {
        val (uri, path) = mkFile(slugOpt,ext)

        val (actor,iri) = try {
          val actor = context.actorOf(Props(new PlantainLDPRActor(uri,path)),path.getFileName.toString)
          (actor,uri)
        } catch {
          case e: InvalidActorNameException => {
            val (uri2,path2) = mkFile(slugOpt,ext)
            Files.deleteIfExists(Files.readSymbolicLink(path))
            Files.delete(path)
            val actor = context.actorOf(Props(new PlantainLDPRActor(uri2,path2)),path2.getFileName.toString)
            (actor,uri2)
          }
        }
          //todo: this graph->iter is very wasteful

//          val scrpt =  if (!isAclPath(path.toString)) { //todo: probably better to have actor deal with its own acls
//          val aclp = aclPath(path.getFileName.toString) //todo have aclPath work with Paths?
//            List(LDPCommand.createLDPR[Plantain](baseUri, Some(aclp), Graph.empty))
//          } else Nil
          val scrpt2 = if (graph != Graph.empty)
            List(LDPCommand.updateLDPR[Plantain](iri, add = graphToIterable(graph)))
          else Nil
          val sl =  scrpt2 ::: List(k(iri))
          val s = sl.tail.foldLeft(sl.head){case (h,n)=> h.flatMap(_=>n)}

          actor forward Scrpt(s)
      }
      case CreateBinary(_, slugOpt, mime: MimeType, k) => {
        mimeExt.extension(mime).map { ext =>
          val (uri, path) = mkFile(slugOpt, ext)
          val (actor,iri) = try {
            val actor = context.actorOf(Props(new PlantainLDPRActor(uri,path)),path.getFileName.toString)
            (actor,uri)
          } catch {
            case e: InvalidActorNameException => {
              val (uri2,path2) = mkFile(slugOpt,ext)
              Files.deleteIfExists(Files.readSymbolicLink(path))
              Files.delete(path)
              val actor = context.actorOf(Props(new PlantainLDPRActor(uri2,path2)),path2.getFileName.toString)
              (actor, uri2)
            }
          }
          val s = LDPCommand.getResource[Plantain,NamedResource[Plantain]](iri)
          actor forward Scrpt(s.flatMap{
            case br: BinaryResource[Plantain] =>k(br)
            case x => throw UnsupportedMediaType("was looking for a BinaryResource but received a "+x.getClass)//todo: not the right error code
          })
          //todo: make sure the uri does not end in ";aclPath" or whatever else the aclPath standard will be
        } getOrElse(throw UnsupportedMediaType("we do not yet support "+mime))
      }
      case CreateContainer(_,slugOpt,graph,k) => {
        val (uri, pathSegment) = mkDir(slugOpt)
        val p = root.resolve(pathSegment)
        val dirUri = uriW[Plantain](uri) / ""
        val ldpc = context.actorOf(Props(new PlantainLDPCActor(dirUri, p)), pathSegment.getFileName.toString)
        val scrpt = if (graph != Graph.empty)
          LDPCommand.updateLDPR[Plantain](uri, add = graphToIterable(graph)).flatMap(_ => k(dirUri))
        else k(dirUri)
        ldpc forward Scrpt(scrpt)
      }
      case DeleteResource(uri, a) => {
//        val name = uriW[Plantain](uri).lastPathSegment
        log.info(s"DeleteResource($uri,$a) Resource is a Container")
        import scalax.file.Path
        //the children may not yet have been stopped, which is why the directory needs to be looked at
        if (context.children.size == 0 || !Files.newDirectoryStream(root).iterator().hasNext) {
          context.stop(self)
        } else {
          throw PreconditionFailed("Can't delete a container that has remaining members")
        }
        rwwActor forward Scrpt(a) //todo: why no function here?
      }
      case _ => super.runLocalCmd(cmd)
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
   * creates a file from the slug, and returns the URI and path for it.
   * @param slugOpt, optional file name
   * @param ext for the extension of the file, should not be the "" string
   **/
  protected def mkFile[A](slugOpt: Option[String], ext: String): (Plantain#URI, Path) = {
    assert (ext != "")
    def mkTmpFile: Path = {
      val file = Files.createTempFile(root, "r_", ext)
      val name = file.getFileName.toString
      Files.createSymbolicLink(root.resolve(name.substring(0, name.length - ext.length)), file)
    }
    val path = slugOpt match {
      case None =>  mkTmpFile
      case Some(slug) => {
        val safeSlug = slug.replaceAll("[/.]+", "_")
        val slugFile = root.resolve(safeSlug)
        if (Files.exists(slugFile,LinkOption.NOFOLLOW_LINKS))
          mkTmpFile
        else {
          Files.createSymbolicLink(slugFile,slugFile.getFileSystem.getPath(slugFile.getFileName.toString+ext))
        }
      }
    }
    val uri = uriW[Plantain](baseUri) / path.getFileName.toString
    (uri, path)

  }

  /**
   * creates a dir/collection from the slug, and returns the URI and path for it.
   * @param slugOpt, optional file name
   **/
  protected def mkDir[A](slugOpt: Option[String]): (Plantain#URI, Path) = {
    val path = slugOpt match {
      case None =>  Files.createTempDirectory(root,"d")
      case Some(slug) => {
        val safeSlug = slug.replaceAll("[/.]+", "_")
        val slugFile = root.resolve(safeSlug)
        if (Files.exists(slugFile))
          Files.createTempDirectory(root,"d"+safeSlug)
        else {
          Files.createDirectory(slugFile)
        }
      }
    }
    val uri = uriW[Plantain](baseUri) / path.getFileName.toString
    (uri, path)

  }


}


class PlantainLDPRActor(baseUri: Plantain#URI,path: Path)
                       (implicit ops: RDFOps[Plantain],
                                 sparqlGraph: SparqlGraph[Plantain],
                                 reader: RDFReader[Plantain,Turtle],
                                 writer: RDFWriter[Plantain,Turtle]
                       ) extends RActor {
  var ext = ".ttl"
  val acl = ".acl"

  val mimeExt = WellKnownMimeExtensions

  import org.w3.banana.syntax._
  import ops._
  import scala.collection.convert.decorateAsScala._

  val cache = scala.collection.mutable.Map[String, NamedResource[Plantain]]()

  val filter = new Filter[Path]() {
    val fileName = path.getFileName.toString
    def accept(entry: Path) = {
      val ename = entry.getFileName.toString
      val result = matches(fileName,ename)
      result
    }

    def matches(fileName: String, entryName: String): Boolean = {
      entryName.startsWith(fileName) && (
        entryName.length == fileName.length ||
          entryName.charAt(fileName.length) == '.' ||
          (entryName.length >= fileName.length+4 && entryName.substring(fileName.length, fileName.length + 4) == ".acl"
              && ( entryName.length == fileName.length+4 ||
                   entryName.charAt(fileName.length+4) == '.' )
            )
        )
    }
  }

  override def preStart() {
    println(s"~~~~~> creating PlantainLDPRActor($baseUri,$path) ~~~")
  }

  def fileName = path.getFileName.toString

  /**
   *
   * @param name the name of the file - with extensions.
   * @throws ResourceDoesNotExist
   * @return
   */
  @throws[ResourceDoesNotExist]
  def getResource(name: String): NamedResource[Plantain] = {
    import scalax.file.{Path=>xPath}
    import scalax.io.{Resource=>xResource}
    //todo: the file should be verified to see if it is up to date.
    cache.getOrElseUpdate(name, {
      //at this point it is still very easy - only two cases! but won't stay like this...
      val (file,iri) = path(name)

      if (file.createNewFile()) {
        if (file.toString.endsWith(ext)) {
           LocalLDPR[Plantain](iri,Graph.empty,Option(new Date(path.toFile.lastModified())))
        } else {
           LocalBinaryR[Plantain](file.toPath, iri)
        }
      } else {
        val res = xResource.fromFile(file)
        val ldprOpt = reader.read(res, iri.toString).map { g =>
            LocalLDPR[Plantain](iri, g, Option(new Date(path.toFile.lastModified())))
        }
        ldprOpt.get //todo: this could break
      }
    })

//      //todo: this globbing is not secure. Requests could contain files with globs in them.
//      val pathStream = Files.newDirectoryStream(path.getParent,fileName+".*")
//      val paths = try {
//        for (p <- pathStream.asScala) yield p
//      } finally {
//        pathStream.close()
//      }
//      paths.collectFirst{
//        case path if path.endsWith(".ttl") =>
//        val res = xResource.fromFile(path.toFile)
//          cache = reader.read(res,"").toOption.map{g=>
//            LocalLDPR[Plantain](baseUri, g,Option(new Date(path.toFile.lastModified())))
//          }
//          cache
//        case path if path.endsWith(".jpg") =>  {
//          cache = Option(LocalBinaryR[Plantain](path,baseUri))
//          cache
//        }
//      }.flatten
//    }
  }

  /**
   *
   * @param name
   * @return
   */
  private def path(name: String): (File,Plantain#URI) = {
    //note: this is really simple at present, but is bound to get more complex,...
    val file = fileFrom(name)
    val uriw =uriW[Plantain](baseUri)
    val iri = uriw.resolve(name)
    (file,iri)
  }

  /**
   * //todo: this feels like it is missing a parameter for mime types...
   * @param name the name of the requested resource
   * @return the file name on disk for it
   */
  def fileFrom(name: String): File = {
    if (name.endsWith(acl)) path.resolveSibling(name + ext).toFile
    else if (Files.isSymbolicLink(path)) path.resolveSibling(Files.readSymbolicLink(path)).toFile
    else path.resolveSibling(name + ext).toFile
  }

  def setResource(name: String, graph: Plantain#Graph) {
    import scalax.io.{Resource=>xResource}
    implicit val codec = Codec.UTF8
    val (file,iri) = path(name)
    file.createNewFile()
    writer.write(graph,xResource.fromOutputStream(new FileOutputStream(file)),"") match {
      case scala.util.Failure(t) => throw new StoreProblem(t)
      case x => x
    }
    cache.put(name,LocalLDPR[Plantain](iri,graph,Some(new Date(file.lastModified()))))
  }

  /*
     * Runs a command that can be evaluated on this container.
     * @param cmd the command to evaluate
     * @tparam A The final return type of the script
     * @return a script for further evaluation
     */
  def runLocalCmd[A](cmd: LDPCommand[Plantain, LDPCommand.Script[Plantain,A]]) {
    log.info(s"in PlantainLDPRActor.runLocalCmd - received $cmd")

    def name(uri: Plantain#URI): String = {
      uriW[Plantain](uri).lastPathSegment
    }
    cmd match {
      case GetResource(uri, agent, k) => {
        val res = getResource(uriW[Plantain](uri).lastPathSegment)
        self forward Scrpt(k(res))
      }
      case GetMeta(uri, k) => {
        //todo: GetMeta here is very close to GetResource, as currently there is no big work difference between the two
        //The point of GetMeta is mostly to remove work if there were work that was very time
        //consuming ( such as serialising a graph )
        val res = getResource(uriW[Plantain](uri).lastPathSegment)
        self forward  Scrpt(k(res))
      }
      case DeleteResource(uri, a) => {
        log.info(s"DeleteResource($uri,$a)")
        val pathStream = Files.newDirectoryStream(path.getParent,filter)
        try {
          for (p <- pathStream.asScala) {
            log.info(s"deleting $p")
            Files.delete(p)
          }
        } finally {
          pathStream.close()
        }
        context.stop(self)
        rwwActor forward Scrpt(a) //todo: why no function here?
      }
      case UpdateLDPR(uri, remove, add, a) => {
        val nme = name(uri)
        getResource(nme) match {
          case LocalLDPR(_,graph,updated) => {
            val temp = remove.foldLeft(graph) {
              (graph, tripleMatch) => graph - tripleMatch.resolveAgainst(uriW[Plantain](uri).resolveAgainst(baseUri))
            }
            val resultGraph = add.foldLeft(temp) {
              (graph, triple) => graph + triple.resolveAgainst(uriW[Plantain](uri).resolveAgainst(baseUri))
            }
            setResource(nme,resultGraph)
          }
          case _ => throw RequestNotAcceptable(s"$uri does not contain a GRAPH, cannot Update")
        }
        self forward Scrpt(a)
      }
      case PatchLDPR(uri, update, bindings, k) => {
        val nme = name(uri)
        getResource(nme) match {
          case LocalLDPR(_,graph,updated) => {
            PlantainLDPatch.executePatch(graph,update,bindings) match {
              case Success(gr) => {
                setResource(nme, gr)
                self forward Scrpt(k(true))
              }
              case Failure(e) => throw e
             }
          }
          case _ => throw RequestNotAcceptable(s"$uri does not contain a GRAPH - PATCH is not possible")
        }
      }
      case SelectLDPR(uri, query, bindings, k) => {
        getResource(name(uri)) match {
          case LocalLDPR(_,graph,_) => {
            val solutions = sparqlGraph(graph).executeSelect(query, bindings)
            self forward Scrpt(k(solutions))
          }
          case _ => throw RequestNotAcceptable(s"$uri does not contain a GRAPH - SELECT is not possible")
        }
      }
      case ConstructLDPR(uri, query, bindings, k) => {
        getResource(name(uri)) match {
          case LocalLDPR(_,graph,_) => {
            val result = sparqlGraph(graph).executeConstruct(query, bindings)
            self forward Scrpt(k(result))
          }
          case _ => throw RequestNotAcceptable(s"$uri does not contain a GRAPH - SELECT is not possible")
        }

      }
      case AskLDPR(uri, query, bindings, k) => {
        getResource(name(uri)) match {
          case LocalLDPR(_,graph,_) => {
            val result = sparqlGraph(graph).executeAsk(query, bindings)
            self forward Scrpt(k(result))
          }
          case _ => throw RequestNotAcceptable(s"$uri does not contain a GRAPH - SELECT is not possible")
        }
      }
    }
  }

  /**
   *
   * @param script
   * @tparam A
   * @throws NoSuchElementException if the resource does not exist
   * @return
   */
  final def run[A](sender: ActorRef, script: LDPCommand.Script[Plantain,A]) {
    script.resume match {
      case -\/(cmd) => {
        if(cmd.uri == baseUri) { //todo: improve for issues of extensions ( eg. .n3, ... )
          runLocalCmd(cmd)
        }
        else {
          log.info(s"sending to $rwwActor")
          rwwActor forward Cmd(cmd)
        }
      }
      case \/-(a) => {
        log.info(s"returning to $sender $a")
        sender ! a
      }
    }
  }


  lazy val rwwActor= context.actorFor("/user/rww")

  def receive = returnErrors {
    case s: Scrpt[Plantain,_]  => {
      run(sender, s.script)
    }
    case cmd: Cmd[Plantain,_] => {
      runLocalCmd(cmd.command)
    }
  }
}
