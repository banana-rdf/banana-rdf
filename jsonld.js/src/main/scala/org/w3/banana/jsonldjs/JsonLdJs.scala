package org.w3.banana
package jsonldjs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
import scala.concurrent.{Future, Promise}
import scala.scalajs.js.Dictionary
import scala.scalajs.js.JSConverters._


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
      })
    promise.future
  }

  def toRDF[Rdf <: RDF](input: String, base: String)(implicit ops: RDFOps[Rdf]): Future[Rdf#Graph] = {
    val doc = js.JSON.parse(input)
    if (doc == null)
      Future.failed(ParsingError("input was not valid JSON"))
    else
      jsonldHelper.toRDF(doc, base)
  }

  def fromRDFToDataset[Rdf <: RDF](_graph: Rdf#Graph)(implicit ops: RDFOps[Rdf]): js.Array[Dictionary[Dictionary[String]]] =
    (for(triple <- ops.getTriples(_graph)) yield
      Triple.fromBananaTriple(triple)
    ).toSet.toJSArray

  def fromRDF[Rdf <: RDF](_graph: Rdf#Graph, base: String)(implicit ops: RDFOps[Rdf]): Future[js.Dynamic] = {
    val promise = Promise[js.Dynamic]()
    val dataset = fromRDFToDataset(_graph)
    jsonld.fromRDF(
      dataset,
      js.Dictionary(
        "base" -> base
      ),
      (err: js.Error, data: js.Dynamic) => {
      if (err != null) {
        promise.failure(ParsingError(err))
      } else {
       promise.success(data)
      }
    })
    promise.future
  }
}

@js.native
@JSGlobal
object jsonld extends js.Object {

  def toRDF(
             doc: js.Dynamic,
             format: js.Dictionary[String],
             callback: js.Function2[js.Error, js.Dynamic, Unit]
           ): Unit = js.native

  def fromRDF(
               dataset: js.Array[js.Dictionary[js.Dictionary[String]]],
               options: js.Dictionary[String],
               callback: js.Function2[js.Error, js.Dynamic, Unit]
             ): Unit = js.native

}

case class ParsingError(message: String) extends Exception(message)

object ParsingError {

  def apply(error: js.Error): ParsingError = ParsingError(error.message)

}

object Triple {

  def fromBananaTriple[Rdf <: RDF](triple: Rdf#Triple)(implicit ops: RDFOps[Rdf]): js.Dictionary[js.Dictionary[String]] = {
    import ops._
    Map(
      "subject" -> Node.fromBananaNode(triple.subject),
      "predicate" -> Node.fromBananaNode(triple.predicate),
      "object" -> Node.fromBananaNode(triple.objectt)
    )
  }.toJSDictionary

  def toBananaTriple[Rdf <: RDF](triple: js.Dictionary[js.Dictionary[String]])(implicit ops: RDFOps[Rdf]): Rdf#Triple = {
    ops.makeTriple(
      Node.toBananaNode(triple("subject")),
      ops.makeUri(triple("predicate")("value")),
      Node.toBananaNode(triple("object")))
  }

}

object Node {

  def fromBananaNode[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOps[Rdf]): js.Dictionary[String] = {
    import ops._
    node match {
      case uri@URI(_uri) =>
        Map(
          "type" -> "IRI",
          "value" -> _uri
        )
      case bnode@BNode(_) =>
        Map(
          "type" -> "blank node"
        )
      case literal@Literal(lexical, datatype, lang) =>
        Map(
          "type" -> "literal",
          "value" -> lexical,
          "datatype" -> datatype.toString
        ) ++ lang.map( _lang => Map("lang" -> _lang.toString) ).getOrElse(Map.empty)
    }
  }.toJSDictionary

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
