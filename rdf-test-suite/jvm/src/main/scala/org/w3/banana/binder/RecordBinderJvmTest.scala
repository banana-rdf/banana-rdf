package org.w3.banana.binder

import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey

import org.scalatest.{Matchers, WordSpec}
import org.w3.banana._

import scala.util._

class RecordBinderJvmTest[Rdf <: RDF](implicit
                                   ops: RDFOps[Rdf],
                                   recordBinder: RecordBinder[Rdf]
                                    ) extends WordSpec with Matchers {

  import ops._

  val objects = new ObjectExamples

  import objects._

  val keyGen = KeyPairGenerator.getInstance("RSA")
  val rsa: RSAPublicKey = {
    keyGen.initialize(512); keyGen.genKeyPair().getPublic().asInstanceOf[RSAPublicKey]
  }


  "serializing and deserializing a public key" in {
    import Cert._
    val rsaPg = rsa.toPG
    //todo: there is a bug below. The isomorphism does not work, even though it should.
    //    System.out.println(s"rsag=${rsaPg.graph}")
    //    val expectedGraph = (
    //      URI("#k") -- cert.modulus ->- rsa.getModulus.toByteArray
    //              -- cert.exponent ->- rsa.getPublicExponent
    //      ).graph
    //    System.out.println(s"expectedGraph=${expectedGraph}")
    //    rsaPg.graph.isIsomorphicWith(expectedGraph) must be(true)
    rsaPg.as[RSAPublicKey] shouldEqual Success(rsa)
  }
}