package org.w3.banana.ldp

import org.w3.banana._
import org.w3.banana.plantain._
import org.w3.banana.plantain.model._
import org.scalatest._
import org.scalatest.matchers._
import org.w3.banana.ldp.LDPCommand._
import java.nio.file.{Path, Files}
import play.api.libs.iteratee.Enumerator
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration.Duration
import java.util.concurrent.{TimeoutException, TimeUnit}
import akka.util.Timeout
import scala.Some
import akka.actor.Props
import org.w3.banana.ldp.auth.WACAuthZ
import scala.util.Try

object PlantainTest {
  val dir = Files.createTempDirectory("plantain" )
  val base =URI.fromString("http://example.com/foo/")
  val rootLDPCActorProps = Props(new PlantainLDPCActor(base, dir))
}

class PlantainLDPSTest extends LDPSTest[Plantain](PlantainTest.base,PlantainTest.dir,PlantainTest.rootLDPCActorProps)(
  Plantain.ops,Plantain.rdfxmlReader)

abstract class LDPSTest[Rdf <: RDF](baseUri: Rdf#URI, dir: Path, rootLDPCActorProps: Props)(
  implicit ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, RDFXML]) extends WordSpec with MustMatchers with BeforeAndAfterAll with TestHelper {

  import diesel._
  import ops._
  import syntax.{graphW,uriW,stringW}

  implicit val timeout = Timeout(10,TimeUnit.MINUTES)
  val rww = new RWWeb[Rdf](baseUri)
  rww.setLDPSActor(rww.system.actorOf(rootLDPCActorProps,"rootContainer"))
  implicit val authz =  new WACAuthZ[Rdf](new WebResource(rww))
  import authz._

  val foaf = FOAFPrefix[Rdf]
  val wac = WebACLPrefix[Rdf]
  val ldp = LDPPrefix[Rdf]

  val betehess = URI("http://example.com/foo/bertails/card#me")
  val betehessCard = URI("http://example.com/foo/bertails/card")

  val containsRel = (URI(".") -- ldp.created ->- URI("")).graph // added by LDP to members

  val graph: Rdf#Graph = (
    URI("#me")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.title ->- "Mr"
  ).graph

  // make the card readable by the whole world ( and link to the main file that make it read/write to Alex )
  val graphCardACL: Rdf#Graph = (
    bnode()
      -- wac.accessTo ->- betehessCard
      -- wac.agentClass ->- foaf.Agent
      -- wac.mode     ->- wac.Read
    ).graph union (
      URI(betehessCard.toString+".acl") -- wac.include ->- URI("http://example.com/foo/bertails/.acl")
    ).graph


  val graphCollectionACL: Rdf#Graph = ( bnode()
      -- wac.accessTo ->- betehessCard
      -- wac.agent    ->-  betehess
      -- wac.mode     ->- (wac.Read, wac.Write)
      -- wac.accessToClass ->- (
        bnode() -- wac.regex ->- TypedLiteral("http://example.com/foo/bertails/.*")
    )
  ).graph

  val graph2: Rdf#Graph = (
    URI("#me")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.knows ->- (
      URI("http://bblfish.net/#hjs")
      -- foaf.name ->- "Henry Story"
      -- foaf.currentProject ->- URI("http://webid.info/")
    )
  ).graph

  val foo: Rdf#Graph = (
    URI("http://example.com/foo/")
    -- rdf("foo") ->- "foo"
    -- rdf("bar") ->- "bar"
  ).graph

  val helloWorldBinary = "☯ Hello, World! ☮".getBytes("UTF-8")

  val helloWorldBinary2 = "Hello, World!".getBytes("UTF-8")

  val baseLdpc =  URI("http://example.com/foo/")


  "CreateLDPR should create an LDPR with the given graph" in {
    val ldprUri = URI("http://example.com/foo/betehess")
    val ldprUri2 = URI("http://example.com/foo/betehess2")

    val script = for {
      rUri <- rww.execute(createLDPR(baseLdpc,Some(ldprUri.lastPathSegment),graph))
      rGraph <- rww.execute(getLDPR(ldprUri))
    } yield {
      rUri must be(ldprUri)
      assert(rGraph isIsomorphicWith (graph union containsRel).resolveAgainst(ldprUri))
    }
    script.getOrFail()

    //this version should be more efficient, in that it should not need to leave the
    //container collection. If one could calculate the number of message sends this would
    //be visible
    val script2 = rww.execute{
      for {
        ruri <- createLDPR(baseLdpc,Some(ldprUri2.lastPathSegment),graph)
        rGraph  <- getLDPR(ruri)
      } yield {
        ruri must be(ldprUri2)
        assert(rGraph isIsomorphicWith (graph union containsRel).resolveAgainst(ruri))
      }
    }
    script2.getOrFail()

    val deleteScript = rww.execute {
      for {
        _ <- deleteResource(ldprUri)
        _ <- deleteResource(ldprUri2)
        x <- getResource(ldprUri)
      } yield x
    }
    deleteScript.failed.getOrFail()



  }

    "CreateLDPR should create an LDPR with the given graph -- with given uri" in {
      val ldpcUri = baseLdpc
      val ldprUri = URI("http://example.com/foo/betehess2")
      val script = rww.execute {
        for {
          rUri <- createLDPR(ldpcUri, Some(ldprUri.lastPathSegment), graph)
          rGraph <- getLDPR(ldprUri)
        } yield {
          rUri must be(ldprUri)
          assert(rGraph isIsomorphicWith (graph union containsRel).resolveAgainst(rUri))
        }
      }
      script.getOrFail()
    }

  "CreateLDPR should create an LDPR with the given graph -- no given uri" in {
    val ldpcUri = URI("http://example.com/foo/")
    val innerldpcUri = ldpcUri/"test1/"
    val content = innerldpcUri/"ouch"
    val script = rww.execute { for {
        ldpc <- createContainer(ldpcUri,Some("test1"),Graph.empty)
        rUri <- createLDPR(ldpc,Some("ouch"), graph)
        rGraph <- getLDPR(rUri)
      } yield {
        rUri  must be(content)
        assert(rGraph isIsomorphicWith (graph union containsRel).resolveAgainst(rUri))
      }
    }
    script.getOrFail()

    //can't delete the container, because this requires it to have no members
    val scr2 = rww.execute( deleteResource(innerldpcUri) )
    try {
      scr2.getOrFail()
    } catch {
      case e: PreconditionFailed => //it's ok
    }

    val scr3 = rww.execute(
      for {
        _ <- deleteResource(content)
        _ <- deleteResource(innerldpcUri)
      } yield {}
    )

    scr3.getOrFail()
  }




  "CreateLDPC & LDPR with ACLs" in {

    val ldpcUri = URI("http://example.com/foo/bertails/")
    val ldpcMetaFull = URI("http://example.com/foo/bertails/.acl")
    val ldprUri = URI("card")

    val ldprMeta = URI("http://example.com/foo/bertails/card.acl")

    //create container with ACLs
    val createContainerScript = rww.execute {
      for {
        ldpcUri <- createContainer(baseLdpc,Some("bertails"),Graph.empty)
        ldpc <- getResource(ldpcUri)
        _   <- updateLDPR(ldpc.acl.get , None, graphCollectionACL.toIterable)
        acl <- getLDPR(ldpc.acl.get)
      } yield {
        ldpc.acl.get must be(ldpcMetaFull)
        assert(acl isIsomorphicWith graphCollectionACL)
      }
    }
    createContainerScript.getOrFail()

    val createProfile = rww.execute{
      for {
        rUri <- createLDPR(ldpcUri,Some(betehessCard.lastPathSegment), graph)
        cardRes  <- getResource(rUri)
        x    <- updateLDPR(cardRes.acl.get, add = graphCardACL.toIterable )
        acl  <- getLDPR(cardRes.acl.get)
      } yield {
        cardRes.location must be(betehessCard)
        cardRes.acl.get must be(ldprMeta)
        assert(acl.resolveAgainst(ldprMeta) isIsomorphicWith graphCardACL)
        cardRes match {
          case card: LDPR[Rdf] => assert( card.graph isIsomorphicWith (graph union containsRel).resolveAgainst(betehessCard))
          case _ => throw new Exception("received the wrong type of resource")
        }
      }
    }

    createProfile.getOrFail()


    val authZ1 = for {
         athzd <- getAuthFor(betehessCard, wac.Read)
       } yield { athzd.contains(foaf.Agent)  }


    authZ1.getOrFail()

    val authZ2 = for {
      athzd <- getAuthFor(betehessCard, wac.Write)
    } yield {
      assert(athzd.contains(betehess))
    }


    authZ2.getOrFail()
  }

//    val aclPath =  rww.execute{
//      for {
//        aclPath <- getMeta(ldprUri)
//        acl = aclPath.acl.get
//        _   <- updateLDPR(acl,Iterable.empty,graphMeta.toIterable)
//      } yield acl
//    }

   //add access control tests here on the graph created above



  "Create Binary" in {
    val ldpcUri = URI("http://example.com/foo/cb/")
    val binUri = URI("http://example.com/foo/cb/img")
    val ldprMeta = URI("http://example.com/foo/cb/img.acl.ttl")

    val createBin =
      for {
        bin <- rww.execute {
          for {
            ldpc <- createContainer(baseLdpc, Some("cb"), Graph.empty)
            bin <- createBinary(ldpc, Some(binUri.lastPathSegment), MimeType("text/html"))
          } yield bin
        }
        it = bin.write
        newbin <- Enumerator(helloWorldBinary).apply(it)
        newres <-  newbin.run
      } yield {
        bin.location must be(binUri)
        newres.location must be(binUri)
      }

    createBin.getOrFail()

    def getBin(hw: Array[Byte]) = rww.execute(
      for {
        res <- getResource(binUri)
      } yield {
        res match {
          case bin: BinaryResource[Rdf] => bin.reader(400).map {
            bytes =>
              hw must be(bytes)
          }
          case _ => throw new Exception("Object MUST be a binary - given that this test is not running in an open world")
        }
      })

    getBin(helloWorldBinary).getOrFail()

    val editBin =
      for {
      newRes <- rww.execute(getResource(binUri)) // we get the resource, but we don't use that thread to upload the data
      bin <- newRes match { //rather here we should use the client thread to upload the data ( as it could be very large )
        case br: BinaryResource[Rdf] => for {
          it <- Enumerator(helloWorldBinary2) |>> br.write
          newres <- it.run
        } yield newres
        case _ => throw new Exception("Object MUST be binary - given that this test is not running in an open world")
      }
    } yield {
      bin.location must be(binUri)
    }
    editBin.getOrFail()

    getBin(helloWorldBinary2).getOrFail()

    val deleteBin = rww.execute(for {
      _ <- deleteResource(binUri)
      _ <- getResource(binUri)
    } yield {
      "hello"
    })

    val res = Await.result(deleteBin.failed,Duration(1,TimeUnit.SECONDS))
    assert(res.isInstanceOf[ResourceDoesNotExist])

  }
//
//
//  "appendToGraph should be equivalent to graph union" in {
//    val ldpcUri = URI("http://example.com/foo/3/")
//    val ldprUri = URI("http://example.com/foo/3/betehess")
//    val script = rww.execute{
//      for {
//        ldpc <- createContainer(baseLdpc,Some("3"),Graph.empty)
//        rUri <- createLDPR(ldpc, Some(ldprUri.lastPathSegment), graph)
//      } yield {
//        rUri must be(ldprUri)
//      }
//    }
//    script.getOrFail()
//
//    val script2 = rww.execute {
//      for {
//        unionG <- updateLDPR(ldprUri, Iterable.empty, graph2.toIterable).flatMap { _ =>
//          getLDPR(ldprUri)
//        }
//      } yield {
//        assert( unionG.relativize(ldprUri) isIsomorphicWith( graph union graph2) )
//      }
//    }
//
//    script2.getOrFail()
//
//  }



//
//  "access control test" in {
//
//
//  }

//  "patchGraph should delete and insert triples as expected" in {
//    val ldpcUri = URI("http://example.com/foo4")
//    val ldprUri = URI("http://example.com/foo4/betehess")
//todo: need to add PATCH mechanism
//    val r = for {
//      _ <- graphStore.removeGraph(u)
//      _ <- graphStore.appendToGraph(u, foo)
//      _ <- graphStore.patchGraph(u,
//        (URI("http://example.com/foo") -- rdf("foo") ->- "foo").graph.toIterable,
//        (URI("http://example.com/foo") -- rdf("baz") ->- "baz").graph)
//      rGraph <- graphStore.getGraph(u)
//    } yield {
//      val expected = (
//        URI("http://example.com/foo")
//        -- rdf("bar") ->- "bar"
//        -- rdf("baz") ->- "baz"
//      ).graph
//      assert(rGraph isIsomorphicWith expected)
//    }
//    r.getOrFail()
//  }

}
