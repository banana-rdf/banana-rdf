package org.w3.banana.jsonldjs.io

import org.scalatest.{AsyncWordSpec, Matchers}
import org.w3.banana.Prefix
import org.w3.banana.plantain._


class JsonLdJsSerializationTest extends AsyncWordSpec with Matchers {

  implicit override def executionContext =
    scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  import PlantainOps._

  // TODO why can't we get rid of that implicit?
  import org.w3.banana.binder.ToURI.URIToURI

  val serializer = new JsonLdJsSerializer[Plantain]

  val schema = Prefix[Plantain]("schema", "http://schema.org/")
  val baseUri = "http://www.example.org"

  val srGraph = (
    URI("http://www.example.org/Manu")
      -- schema("name") ->- "Manu Sporny"
      -- schema("url") ->- URI("http://manu.sporny.org/")
      -- schema("image") ->- URI("http://manu.sporny.org/images/manu.png")
    ).graph

  val jsonldRefString = """[{"@id":"http://www.example.org/Manu","http://schema.org/image":[{"@id":"http://manu.sporny.org/images/manu.png"}],"http://schema.org/url":[{"@id":"http://manu.sporny.org/"}],"http://schema.org/name":[{"@value":"Manu Sporny"}]}]"""

  "From Graph to JSONLd" in {

    val fut = serializer.asString(srGraph, baseUri)
    //fut shouldBe defined

    fut.map { jsonldString =>
      jsonldString shouldEqual jsonldRefString
    }

  }

}
