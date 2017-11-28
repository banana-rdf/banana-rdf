package org.w3.banana.jsonldjs.io

import org.scalatest.{AsyncWordSpec, Matchers}
import org.w3.banana.Prefix
import org.w3.banana.plantain._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object JsonLdJsSerializationTest extends AsyncWordSpec with Matchers {

  import PlantainOps._

  // TODO why can't we get rid of that implicit?
  import org.w3.banana.binder.ToURI.URIToURI

  val serializer = new JsonLdJsSerializer[Plantain]

  val schema = Prefix[Plantain]("schema", "http://schema.org/")
  val baseUri = "http://www.example.org"

  val srGraph = (
    BNode()
      -- schema("name") ->- "Manu Sporny"
      -- schema("url") ->- URI("http://manu.sporny.org/")
      -- schema("image") ->- URI("http://manu.sporny.org/images/manu.png")
    ).graph

  "From Graph to JSONLd" in {

    val fut = serializer.asString(srGraph, baseUri)
    //fut shouldBe defined

    fut.map { jsonldString =>
      println(jsonldString)
      jsonldString shouldEqual """{
"http://schema.org/name": "Manu Sporny",
"http://schema.org/url": {"@id": "http://manu.sporny.org/"},
"http://schema.org/image": {"@id": "http://manu.sporny.org/images/manu.png"}
}"""
    }

  }

}
