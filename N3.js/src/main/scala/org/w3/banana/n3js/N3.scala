package org.w3.banana.n3js

import scala.scalajs.js

object N3 extends js.Object {

  def Parser(): Parser = ???

  def Store(): Store = ???

  val Util: Util = ???

}

trait Parser extends js.Object {

  def parse(s: String, callback: js.Function3[js.Error, js.UndefOr[Triple], js.UndefOr[js.Dynamic], Unit]): Unit

  def parse(callback: js.Function3[js.Any, js.UndefOr[Triple], js.UndefOr[js.Any], Unit]): Unit

  def addChunk(chunk: String): Unit

  def end(): Unit

}

object Parser {

  implicit class ParserW(val parser: Parser) extends AnyVal {

    import scala.concurrent._
    import scala.util.Success

    def parse[T](
      input: String)(
      tripleCallback: Triple => Unit,
      prefixCallback: (String, String) => Unit = (_:String, _:String) => ()
    ): Future[Unit] = {
      val promise = Promise[Unit]()
      parser.parse(
        input,
        (error: js.Error, triple: js.UndefOr[Triple], prefixes: js.UndefOr[js.Dynamic]) => {
          if (triple.isDefined)
            tripleCallback(triple.get)
          else if (error != null)
            promise.failure(ParsingError(error))
          else {
            if (prefixes.isDefined) {
              // it's supposed to be an array but I can't find a better way...
              js.Object.properties(prefixes.get.asInstanceOf[js.Object]).forEach((key: String) => {
                val value = prefixes.get.selectDynamic(key).asInstanceOf[String]
                prefixCallback(key, value)
              })
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
