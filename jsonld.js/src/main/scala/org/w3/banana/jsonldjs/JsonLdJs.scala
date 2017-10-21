package org.w3.banana
package jsonldjs

import scala.scalajs.js
import scala.concurrent.{ Future, Promise }

// the content of jsonldHelper should be in jsonld but it doesn't
// work! Don't know why...
object jsonldHelper {

  def toRDF[Rdf <: RDF](doc: js.Dynamic, base: String)(implicit ops: RDFOps[Rdf]): Future[Rdf#Graph] = {
    import ops.mgraphW
    val promise = Promise[Rdf#Graph]()
    val mgraph = ops.makeEmptyMGraph()
    jsonld.toRDF(
      doc,
      js.Dictionary("base" -> base),
      (err: js.Error, data: js.Dynamic) => {
        if (err != null) {
          promise.failure(ParsingError(err))
        } else {
          val triples =
            data.selectDynamic("@default")
                .asInstanceOf[js.Array[js.Dictionary[js.Dictionary[String]]]]
          triples.foreach { (triple: js.Dictionary[js.Dictionary[String]]) =>
            mgraph += Triple.toBananaTriple(triple)
          }
          promise.success(mgraph.makeIGraph())
        }
        ()
      }
    )
    promise.future
  }

  def toRDF[Rdf <: RDF](input: String, base: String)(implicit ops: RDFOps[Rdf]): Future[Rdf#Graph] = {
    val doc = js.JSON.parse(input)
    if (doc == null)
      Future.failed(ParsingError("input was not valid JSON"))
    else
      jsonldHelper.toRDF(doc, base)
  }

}

@js.native
object jsonld extends js.Object {

  def toRDF(
    doc: js.Dynamic,
    format: js.Dictionary[String],
    callback: js.Function2[js.Error, js.Dynamic, Unit]
  ): Unit = js.native

}

case class ParsingError(message: String) extends Exception(message)

object ParsingError {

  def apply(error: js.Error): ParsingError = ParsingError(error.message)

}

object Triple {

  def toBananaTriple[Rdf <: RDF](triple: js.Dictionary[js.Dictionary[String]])(implicit ops: RDFOps[Rdf]): Rdf#Triple = {
    ops.makeTriple(
      Node.toBananaNode(triple("subject")),
      ops.makeUri(triple("predicate")("value")),
      Node.toBananaNode(triple("object"))
    )
  }

}

object Node {

  def toBananaNode[Rdf <: RDF](node: js.Dictionary[String])(implicit ops: RDFOps[Rdf]): Rdf#Node = {
    node("type") match {
      case "blank node" =>
        val label = node("value").substring(2)
        ops.makeBNodeLabel("value")
      case "IRI" =>
        val iriS = node("value")
        ops.makeUri(iriS)
      case "literal" =>
        val lexicalForm = node("value")
        val lang = node.get("lang")
        if (lang.isDefined) {
          ops.makeLangTaggedLiteral(lexicalForm, ops.makeLang(lang.get))
        } else {
          val datatype = ops.makeUri(node("datatype"))
          ops.makeLiteral(lexicalForm, datatype)
        }
    }
  }

}
