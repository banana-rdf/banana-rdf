package org.w3.banana.rdfstorew

import scala.scalajs.js.{ RegExp, Dynamic, JSApp }
import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

import org.w3.banana._
import org.w3.banana.{ RDFStore => RDFStoreInterface }
import org.w3.banana.syntax._
import org.w3.banana.diesel._
import org.w3.banana.binder._
import scala.util._

class ObjectExamplesJasmine[Rdf <: RDF]()(implicit ops: RDFOps[Rdf], recordBinder: RecordBinder[Rdf]) {

  import ops._
  import recordBinder._

  val foaf = FOAFPrefix[Rdf]
  val cert = CertPrefix[Rdf]

  case class Person(name: String, nickname: Option[String] = None)

  object Person {

    val clazz = URI("http://example.com/Person#class")
    implicit val classUris = classUrisFor[Person](clazz)

    val name = property[String](foaf.name)
    val nickname = optional[String](foaf("nickname"))
    val address = property[Address](foaf("address"))

    implicit val container = URI("http://example.com/persons/")
    implicit val binder = pgb[Person](name, nickname)(Person.apply, Person.unapply)

  }

  sealed trait Address

  object Address {

    val clazz = URI("http://example.com/Address#class")
    implicit val classUris = classUrisFor[Address](clazz)

    // not sure if this could be made more general, nor if we actually want to do that
    implicit val binder: PGBinder[Rdf, Address] = new PGBinder[Rdf, Address] {
      def fromPG(pointed: PointedGraph[Rdf]): Try[Address] =
        Unknown.binder.fromPG(pointed) orElse VerifiedAddress.binder.fromPG(pointed)

      def toPG(address: Address): PointedGraph[Rdf] = address match {
        case va: VerifiedAddress => VerifiedAddress.binder.toPG(va)
        case Unknown => Unknown.binder.toPG(Unknown)
      }
    }

  }

  // We need to get rid of all cryptographic code as it is not supported in JS
  case object Unknown extends Address {

    val clazz = URI("http://example.com/Unknown#class")
    implicit val classUris = classUrisFor[Unknown.type](clazz, Address.clazz)

    // there is a question about constants and the classes they live in
    implicit val binder: PGBinder[Rdf, Unknown.type] = constant(this, URI("http://example.com/Unknown#thing")) withClasses classUris

  }

  case class VerifiedAddress(label: String, city: City) extends Address

  object VerifiedAddress {

    val clazz = URI("http://example.com/VerifiedAddress#class")
    implicit val classUris = classUrisFor[VerifiedAddress](clazz, Address.clazz)

    val label = property[String](foaf("label"))
    val city = property[City](foaf("city"))

    implicit val ci = classUrisFor[VerifiedAddress](clazz)

    implicit val binder = pgb[VerifiedAddress](label, city)(VerifiedAddress.apply, VerifiedAddress.unapply) withClasses classUris

  }

  case class City(cityName: String, otherNames: Set[String] = Set.empty)

  object City {

    val clazz = URI("http://example.com/City#class")
    implicit val classUris = classUrisFor[City](clazz)

    val cityName = property[String](foaf("cityName"))
    val otherNames = set[String](foaf("otherNames"))

    implicit val binder: PGBinder[Rdf, City] =
      pgbWithId[City](t => URI("http://example.com/" + t.cityName))
        .apply(cityName, otherNames)(City.apply, City.unapply) withClasses classUris

  }

  case class Me(name: String)

  object Me {
    val clazz = URI("http://example.com/Me#class")
    implicit val classUris = classUrisFor[Me](clazz)

    val name = property[String](foaf.name)

    implicit val binder: PGBinder[Rdf, Me] =
      pgbWithConstId[Me]("http://example.com#me")
        .apply(name)(Me.apply, Me.unapply) withClasses classUris
  }

}

class TestApp[Rdf <: RDF](store: RDFStoreInterface[Rdf])(implicit ops: RDFOps[Rdf]) extends JSApp with JSUtils {

  import ops._

  val foaf = FOAFPrefix[Rdf]

  val graphStore = GraphStore[Rdf](store)

  val graph: Rdf#Graph = (
    bnode("betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.title ->- "Mr"
  ).graph

  val graph2: Rdf#Graph = (
    bnode("betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.knows ->- (
      URI("http://bblfish.net/#hjs")
      -- foaf.name ->- "Henry Story"
      -- foaf.currentProject ->- URI("http://webid.info/")
    )
  ).graph

  val foo: Rdf#Graph = (
    URI("http://example.com/foo")
    -- rdf("foo") ->- "foo"
    -- rdf("bar") ->- "bar"
  ).graph

  def main(): Unit = {

    val u1 = URI("http://example.com/graph")
    val u2 = URI("http://example.com/graph2")
    val r = for {
      _ <- graphStore.removeGraph(u1)
      _ <- graphStore.removeGraph(u2)
      _ <- graphStore.appendToGraph(u1, graph)
      _ <- graphStore.appendToGraph(u2, graph2)
      rGraph <- graphStore.getGraph(u1)
      rGraph2 <- graphStore.getGraph(u2)
    } yield {
      println(rGraph isIsomorphicWith graph)
      println(rGraph2 isIsomorphicWith graph2)
    }

    /*
    val objects = new ObjectExamplesJasmine[RDFStore]()

    import objects._

    val city = City("Paris", Set("Panam", "Lutetia"))
    val verifiedAddress = VerifiedAddress("32 Vassar st", city)
    val person = Person("Alexandre Bertails")
    val personWithNickname = person.copy(nickname = Some("betehess"))
    val me = Me("Name")

    val res = verifiedAddress.toPG.as[VerifiedAddress]
    */

    /*


    // Building the store
    val options = Map("name" -> "hey")
    val store = RDFStoreW(options)
    println("FOUND STORE:")
    println(store)

    val data = "<http://test.com/something#me> <http://test.com/something/name> \"antonio\" ."
    val graph = "http://test.com/test_graph"


    store.load("text/n3",data).flatMap { _ =>
      store.execute("SELECT ?s ?p ?o WHERE { ?s ?p ?o }")
    } map { results =>
      println("RESULTS AT THE END")
      println(results)
    }

     */

    /*
    println("** building uri")
    val uri = RDFStoreOps.makeUri("http://test.com/something#mytype")
    println("** building literal")
    val literal = RDFStoreOps.makeLiteral("this is a test", uri)
    log("*** THE LITERAL")
    log(literal)
    println(literal)
    println("datatype")
    println(literal.datatype)
    println("language")
    println(literal.language)

    val triple = RDFStoreOps.makeTriple(
      RDFStoreOps.makeUri("http://test.com/me"),
      RDFStoreOps.makeUri("foaf:name"),
      RDFStoreOps.makeLiteral("Antonio",null)
    )
    log("** THE TRIPLE")
    log(triple)
    println(triple)
*/

  }

}

object TestApp extends TestApp[RDFStore](RDFStoreW(Map()))