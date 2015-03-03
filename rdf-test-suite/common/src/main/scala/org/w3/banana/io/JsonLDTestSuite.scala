package org.w3.banana
package io

import scalaz._

abstract class JsonLDTestSuite[Rdf <: RDF, M[+_]: Monad: Comonad, JsonLdOut](implicit
  ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, M, JsonLd],
  writer: RDFWriter[Rdf, M, JsonLdOut]
) extends SerialisationTestSuite[Rdf, M, JsonLd, JsonLdOut]("JSON-LD", "jsonld") {

  val referenceGraphSerialisedForSyntax = """
  [
  {
    "@id": "http://www.w3.org/2001/sw/RDFCore/ntriples/",
    "http://purl.org/dc/elements/1.1/creator": [
    {
      "@value": "Art Barstow"
    },
    {
      "@value": "Dave Beckett"
    }
    ],
    "http://purl.org/dc/elements/1.1/publisher": [
    {
      "@id": "http://www.w3.org/"
    }
    ]
  }
  ]
   """

}
