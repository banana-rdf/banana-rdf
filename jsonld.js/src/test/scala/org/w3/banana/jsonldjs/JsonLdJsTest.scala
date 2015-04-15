package org.w3.banana
package jsonldjs

import com.inthenow.zcheck.SpecLite
import scala.scalajs.js

import scalajs.concurrent.JSExecutionContext.Implicits.runNow

// the async stuff doesn't get properly tested by zcheck. Need to wait
// for scala-js 0.6. Look for [error] in the output in the meantime...
object JsonLdJsTest extends SpecLite {

  val input = """{
  "http://schema.org/name": "Manu Sporny",
  "http://schema.org/url": {"@id": "http://manu.sporny.org/"},
  "http://schema.org/image": {"@id": "http://manu.sporny.org/images/manu.png"}
}"""

  // parse() returns the internal representation
  "parsejsonld.toRDF" in {

    val doc = js.JSON.parse(input)

    jsonld.toRDF(doc, js.Dictionary(), { (err: js.Error, data: js.Dynamic) =>
      check(err == null)
      check(data.selectDynamic("@default") != null)
      ()
    })

  }

//  @betehess: I was not able to get a (err != null)
//  "parsejsonld.toRDF -- error" in {
//
//    val input = """{
//  "http://schema.org/url": {"@foo": "baz"}
//}"""
//
//    val doc = js.JSON.parse(input)
//
//    jsonld.toRDF(doc, js.Dictionary(), { (err: js.Error, data: js.Dynamic) =>
//      check(err == null)
//      check(data.selectDynamic("@default") != null)
//      println(js.JSON.stringify(data))
//      ()
//    })
//
//  }

}
