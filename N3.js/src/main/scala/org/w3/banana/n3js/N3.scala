package org.w3.banana
package n3js

import scala.scalajs.js

object N3 extends js.Object {

  def Parser(): Parser = js.native

  def Store(): Store = js.native

  val Util: Util = js.native

}

trait Parser extends js.Object {

  def parse(s: String, callback: js.Function3[js.UndefOr[js.Error], js.UndefOr[Triple], js.UndefOr[js.Dictionary[String]], Unit]): Unit = js.native

  def parse(callback: js.Function3[js.UndefOr[js.Error], js.UndefOr[Triple], js.UndefOr[js.Dictionary[String]], Unit]): Unit = js.native

  def addChunk(chunk: String): Unit = js.native

  def end(): Unit = js.native

}

object Parser {

  implicit class ParserW(val parser: Parser) extends AnyVal {

    import scala.concurrent._
    import scala.util.Success

    /** Parses the `input` and call the `tripleCallback` and
      * `prefixCallback` functions when a triple or a prefix are
      * found.
      *       
      * Note: according to N3.js documentation, `prefixCallback` is
      * actually called at the end of the parsing.
      * 
      * @return a [[scala.concurrent.Future]] that signals that parsing is done.
      */
    def parse[T](
      input: String)(
      tripleCallback: Triple => Unit,
      prefixCallback: (String, String) => Unit = (_: String, _: String) => ()
    ): Future[Unit] = {
      val promise = Promise[Unit]()
      parser.parse(
        input,
        (error: js.UndefOr[js.Error], triple: js.UndefOr[Triple], prefixes: js.UndefOr[js.Dictionary[String]]) => {
          if (triple != null && triple.isDefined)
            tripleCallback(triple.get)
          else if (triple != null && error.isDefined)
            promise.failure(ParsingError(error.get))
          else {
            prefixes.foreach { prefixes =>
              prefixes.foreach { case (key, prefix) => prefixCallback(key, prefix) }
            }
            promise.complete(Success(()))
          }
          ()
        }
      )
      promise.future
    }

    /** Allows parsing in chunks. The callbacks are registered and the
      * termination is notified through the return
      * [[scala.concurrent.Future]].
      * 
      * @return A [[scala.concurrent.Future]] that resolves when the parsing is done.
      */
    def parseChunks(
      tripleCallback: Triple => Unit,
      prefixCallback: (String, String) => Unit = (_: String, _: String) => ()
    ): Future[Unit] = {
      val promise = Promise[Unit]()
      parser.parse(
        (error: js.UndefOr[js.Error], triple: js.UndefOr[Triple], prefixes: js.UndefOr[js.Dictionary[String]]) => {
          if (triple != null && triple.isDefined)
            tripleCallback(triple.get)
          else if (error != null && error.isDefined)
            promise.failure(ParsingError(error.get))
          else {
            prefixes.foreach { prefixes =>
              prefixes.foreach { case (key, prefix) => prefixCallback(key, prefix) }
            }
            promise.complete(Success(()))
          }
          ()
        }
      )
      promise.future
    }



  }

}

case class ParsingError(message: String) extends Exception(message)

object ParsingError {

  def apply(error: js.Error): ParsingError = ParsingError(error.message)

}

trait Triple extends js.Object {

  def subject: String = js.native
  def predicate: String = js.native
  def `object`: String = js.native

}

object Triple {

  implicit class TripleW(val triple: Triple) extends AnyVal {
    import triple._
    def s: String = s"{ $subject $predicate $objekt }"
    def objekt = `object`
  }

  import org.w3.banana._
  import N3.Util

  def toBananaTriple[Rdf <: RDF](t: Triple)(implicit ops: RDFOps[Rdf]): Rdf#Triple = {
    val s = Node.toBananaNode(t.subject)
    val p = ops.makeUri(t.predicate)
    val o = Node.toBananaNode(t.`object`)
    ops.makeTriple(s, p, o)
  }

}

object Node {

  import org.w3.banana._
  import N3.Util

  def toBananaNode[Rdf <: RDF](s: String)(implicit ops: RDFOps[Rdf]): Rdf#Node = {
    import ops._
    if (Util.isIRI(s)) {
      makeUri(s)
    } else if (Util.isBlank(s)) {
      makeBNodeLabel(s.substring(2))
    } else {
      val lexicalForm = Util.getLiteralValue(s)
      val lang = Util.getLiteralLanguage(s)
      if (lang.isEmpty)
        makeLiteral(lexicalForm, makeUri(Util.getLiteralType(s)))
      else
        makeLangTaggedLiteral(lexicalForm, makeLang(lang))
    }
  }

}

trait Util extends js.Object {

  def isIRI(s: String): Boolean = js.native
  def isBlank(s: String): Boolean = js.native
  def isLiteral(s: String): Boolean = js.native
  def getLiteralValue(s: String): String = js.native
  def getLiteralLanguage(s: String): String = js.native
  def getLiteralType(s: String): String = js.native
  def isPrefixedName(s: String): Boolean = js.native
  def expandPrefixedName(s: String, prefixes: Dynamic): String = js.native

  def createIRI(iri: String): String = js.native
  def createLiteral(lexicalForm: String, langOrIri: String): String = js.native
  def createLiteral(i: Int): String = js.native
  def createLiteral(b: Boolean): String = js.native

}

trait Store extends js.Object {

  def addTriple(s: String, p: String, o: String): Unit = js.native

  def addTriple(triple: Triple): Unit = js.native

  def find(s: String, p: String, o: String): js.Array[Triple] = js.native

}

object Store {

  implicit class StoreW(val store: Store) extends AnyVal {

    def findAll(): js.Array[Triple] = store.find(null, null, null)

  }

}
