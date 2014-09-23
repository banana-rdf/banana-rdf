package org.w3.banana.ldpatch

import org.w3.banana._
import org.w3.banana.jena._
import java.io.InputStream

object JenaPatch {

  import Jena.ops._

  private val ldpatch = new Grammar[Jena] with Semantics[Jena] { implicit val ops: RDFOps[Jena] = Jena.ops }

  def fromString(input: String, baseURI: String): model.LDPatch[Jena] =
    ldpatch.grammar.parseLDPatch(input, URI(baseURI)).get

  def fromInputStream(input: InputStream, baseURI: String): model.LDPatch[Jena] =
    fromString(io.Source.fromInputStream(input).mkString, baseURI)

  def apply(patch: model.LDPatch[Jena], graph: Jena#Graph): Jena#Graph =
    ldpatch.semantics.LDPatch(patch, graph)

}
