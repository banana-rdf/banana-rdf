/*
 *  Copyright (c) 2012 , 2021 W3C Members
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  This program and the accompanying materials are made available under
 *  the W3C Software Notice and Document License (2015-05-13) which is available at
 *  https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document.
 *
 *  SPDX-License-Identifier: W3C-20150513
 */

package org.w3.banana

import _root_.io.lemonlabs.uri.*
import org.w3.banana.RDF.*
import org.w3.banana.TestConstants.*

open class URITest[Rdf <: RDF](using ops: Ops[Rdf]) extends munit.FunSuite:
   import ops.given

   test("relative URLs and incomplete ones should Fail") {
     assert(ops.URI.mkUri("people").isFailure)
     assert(ops.URI.mkUri("/people/henry").isFailure)
     assert(ops.URI.mkUri("/people/henry/card#me").isFailure)
     assert(ops.URI.mkUri("//bblfish.net/people/henry/card").isFailure)
     assert(ops.URI.mkUri("//bblfish.net:80/people/henry/card").isFailure)
     assert(
       ops.URI.mkUri("https://").isSuccess
     ) // <- bizzarely enough this is an absolute url. Should we not accept it?
   }

   test("relative URLs and incomplete ones should throw exceptions when using apply(") {
     // todo: narrow it down to a banana-exception
     intercept[Exception] { ops.URI("people") }
     intercept[Exception] { ops.URI("/people/henry") }
     intercept[Exception] { ops.URI("/people/henry/card#me") }
     intercept[Exception] { ops.URI("//bblfish.net/people/henry/card") }
     intercept[Exception] { ops.URI("//bblfish.net:80/people/henry/card") }
     assert(
       ops.URI("https://").isInstanceOf[RDF.URI[Rdf]]
     ) // <- bizzarely enough this is an absolute url. Should we not accept it?
   }

   test("absolute URLs and URNs should be accepted") {
     assert(ops.URI.mkUri("https://bblfish.net").isSuccess)
     assert(ops.URI.mkUri("https://bblfish.net:80/").isSuccess)
     assert(ops.URI.mkUri("https://bblfish.net/people/henry/").isSuccess)
     assert(ops.URI.mkUri("https://bblfish.net/people/henry/card#me").isSuccess)
     // urn examples taken from wikipedia page https://en.wikipedia.org/wiki/Uniform_Resource_Name
     assert(ops.URI.mkUri("urn:isbn:0451450523").isSuccess)
     assert(ops.URI.mkUri("urn:isan:0000-0000-2CEA-0000-1-0000-0000-Y").isSuccess)
     assert(ops.URI.mkUri("urn:ISSN:0167-6423").isSuccess)
     assert(ops.URI.mkUri("urn:mpeg:mpeg7:schema:2001").isSuccess)
     // did spec: https://www.w3.org/TR/did-core/
     assert(ops.URI.mkUri("did:example:123456789abcdefghi").isSuccess)
     assert(ops.URI.mkUri("did:key:z6MkhaXgBZDvotDkL5257faiztiGiC2QtKLGpbnnEGta2doK").isSuccess)
   }
   import ops.given

   test("rURLs are both absolute and relative ones") {
     assert(ops.rURI.mkUri("https://bblfish.net").isSuccess)
     assert(ops.rURI.mkUri("https://bblfish.net:80/").isSuccess)
     assert(ops.rURI.mkUri("https://bblfish.net/people/henry/").isSuccess)
     assert(ops.rURI.mkUri("https://bblfish.net/people/henry/card#me").isSuccess)
     // urn examples taken from wikipedia page https://en.wikipedia.org/wiki/Uniform_Resource_Name
     assert(ops.rURI.mkUri("urn:isbn:0451450523").isSuccess)
     assert(ops.rURI.mkUri("urn:isan:0000-0000-2CEA-0000-1-0000-0000-Y").isSuccess)
     assert(ops.rURI.mkUri("urn:ISSN:0167-6423").isSuccess)
     assert(ops.rURI.mkUri("urn:mpeg:mpeg7:schema:2001").isSuccess)
     // did spec: https://www.w3.org/TR/did-core/
     assert(ops.rURI.mkUri("did:example:123456789abcdefghi").isSuccess)
     assert(ops.rURI.mkUri("did:key:z6MkhaXgBZDvotDkL5257faiztiGiC2QtKLGpbnnEGta2doK").isSuccess)
     assert(ops.rURI.mkUri("people").isSuccess)
     assert(ops.rURI.mkUri("/people/henry").isSuccess)
     assert(ops.rURI.mkUri("/people/henry/card#me").isSuccess)
     assert(ops.rURI.mkUri("//bblfish.net/people/henry/card").isSuccess)
     assert(ops.rURI.mkUri("//bblfish.net:80/people/henry/card").isSuccess)

   }

   test("relativize URL") {
     assertEquals(
       URI("https://bblfish.net/people/henry/card#me").relativizeAgainst(
         AbsoluteUrl.parse("https://bblfish.net/people/")
       ),
       (rURI("henry/card#me"), true)
     )
     assertEquals(
       URI("https://bblfish.net/people/henry/card#me").relativizeAgainst(
         AbsoluteUrl.parse("https://bblfish.net")
       ),
       (
         rURI("people/henry/card#me"),
         true
       ) // <-- should that not start with / ? (not with jURI backing it seems)
     )
     assertEquals(
       URI("https://bblfish.net/people/henry/card#me").relativizeAgainst(
         AbsoluteUrl.parse("https://bblfish.net:443")
       ),
       (rURI("people/henry/card#me"), true)
     )
     assertEquals(
       URI("https://bblfish.net/people/henry/card#me").relativizeAgainst(
         AbsoluteUrl.parse("https://bblfish.net:443/people/./")
       ),
       (rURI("henry/card#me"), true)
     )
     assertEquals(
       URI("https://bblfish.net/people/henry/card#me").relativizeAgainst(
         AbsoluteUrl.parse("https://bblfish.net:443/people/gordana/../henry/")
       ),
       (rURI("card#me"), true)
     )
     assertEquals(
       URI("https://co-operating.systems/2019/04/01/").relativizeAgainst(
         AbsoluteUrl.parse("https://bblfish.net:443/people/gordana/../henry/")
       ),
       (rURI("https://co-operating.systems/2019/04/01/"), false)
     )
     assertEquals(
       URI("https://co-operating.systems/2019/./04/01/").relativizeAgainst(
         AbsoluteUrl.parse("https://bblfish.net:443/people/gordana/../henry/")
       ),
       (rURI("https://co-operating.systems/2019/04/01/"), true)
     )
   }

   test("resolve relativeURLs to absolute ones") {
     val bbls = "https://bblfish.net"
     val bbl = AbsoluteUrl.parse(bbls)
     assertEquals(ops.rURI("people").resolveAgainst(bbl), (ops.URI(bbls + "/people"), true))
     assertEquals(
       ops.rURI("/people/henry").resolveAgainst(bbl),
       (ops.URI(bbls + "/people/henry"), true)
     )
     assertEquals(
       ops.rURI("/people/henry/card#me").resolveAgainst(bbl),
       (ops.URI(bbls + "/people/henry/card#me"), true)
     )
     // sadly java.net.URI does not normalize default port numbers away, so we have this for the moment...
     assertEquals(
       ops.rURI("https://bblfish.net:443/people/henry/card").resolveAgainst(bbl),
       (ops.URI("https://bblfish.net:443/people/henry/card"), false)
     )
   }

   test("remove fragments from URLs") {
     val bblStr = "https://bblfish.net/people/henry/card#me"
     val bblDocStr = "https://bblfish.net/people/henry/card"

     assertEquals(
       ops.URI(bblStr).fragmentLess,
       ops.URI(bblDocStr)
     )
   }

   test("remove fragments from relative URLs") {
     val bblAbsStr = "https://bblfish.net/people/henry/card#me"
     val bblAbsDocStr = "https://bblfish.net/people/henry/card"

     assertEquals(
       ops.rURI(bblAbsStr).fragmentLess,
       ops.rURI(bblAbsDocStr)
     )

     val bblStr = "/people/henry/card#me"
     val bblDocStr = "/people/henry/card"

     assertEquals(
       ops.rURI(bblStr).fragmentLess,
       ops.rURI(bblDocStr)
     )

     val bblShort = "card#me"
     val bblShortDoc = "card"
     assertEquals(
       ops.rURI(bblShort).fragmentLess,
       ops.rURI(bblShortDoc)
     )

   }

end URITest
