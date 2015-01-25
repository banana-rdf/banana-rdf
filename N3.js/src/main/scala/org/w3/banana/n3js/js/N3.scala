package org.w3.banana.n3js
package js

import scala.scalajs.js

object N3 extends js.Object {

  def Parser(): Parser = ???

  def Store(): Store = ???

  val Util: Util = ???

}

trait Parser extends js.Object {

  def parse(s: String, callback: js.Function3[js.UndefOr[js.Error], js.UndefOr[Triple], js.UndefOr[js.Dictionary[String]], Unit]): Unit

  def parse(callback: js.Function3[js.UndefOr[js.Error], js.UndefOr[Triple], js.UndefOr[js.Dictionary[String]], Unit]): Unit

  def addChunk(chunk: String): Unit

  def end(): Unit

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
              js.Object.properties(prefixes).forEach((key: String) => prefixCallback(key, prefixes(key)))
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
              js.Object.properties(prefixes).forEach((key: String) => prefixCallback(key, prefixes(key)))
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

  def subject: String = ???
  def predicate: String = ???
  def `object`: String = ???

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

  def isIRI(s: String): Boolean = ???
  def isBlank(s: String): Boolean = ???
  def isLiteral(s: String): Boolean = ???
  def getLiteralValue(s: String): String = ???
  def getLiteralLanguage(s: String): String = ???
  def getLiteralType(s: String): String = ???
  def isPrefixedName(s: String): Boolean = ???
  def expandPrefixedName(s: String, prefixes: Dynamic): String = ???

}

trait Store extends js.Object {

  def addTriple(s: js.String, p: js.String, o: js.String): Unit

  def addTriple(triple: Triple): Unit

  def find(s: js.String, p: js.String, o: js.String): js.Array[Triple]

}

object Store {


  implicit class StoreW(val store: Store) extends AnyVal {

    def findAll(): js.Array[Triple] = store.find(null, null, null)

  }

}
