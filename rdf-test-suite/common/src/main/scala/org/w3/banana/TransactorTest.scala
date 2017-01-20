package org.w3.banana

import com.inthenow.zcheck.SpecLite

import scala.util.{Failure, Try}
import scalaz.{Comonad, Monad}

abstract class TransactorTest[Rdf <: RDF, M[+_] : Monad: Comonad, Connection](implicit
  val ops: RDFOps[Rdf],
  val transactor:Transactor[Rdf,Connection]
) extends SpecLite with SpecLiteExtra {

  import ops._

  val foaf = FOAFPrefix[Rdf]

  /** note: I prefer to use URI in this test as some weird sesame databases do not preserve bnode's ids */
  val betehess = ops.makeUri("https://github.com/betehess")

  val expectedGraph:Rdf#Graph =
    Graph(
      Triple(betehess, foaf.name, Literal.tagged("Alexandre", Lang("fr"))),
      Triple(betehess, foaf.title, Literal("Mr"))
    )

  val readException =  new Exception("this read is broken!")

  val writeException = new Exception("this write is broken!")

  def newConnection():Connection

  protected def successfulRead(con:Connection):Try[Rdf#Graph]

  protected def brokenRead(con:Connection):Try[Rdf#Graph]

  protected def successfulWrite(con:Connection):Try[Unit]

  protected def brokenWrite(con:Connection):Try[Unit]

  "transactor must read triples safely" in {
    val result1 = this.successfulRead(newConnection())
    check(result1.isSuccess)

    val result2 = this.brokenRead(newConnection())
    result2 mustMatch {
      case Failure(this.readException) => true
    }
  }

  "transactor must write triples safely" in {
    val resultW1 = this.brokenWrite(newConnection())
    resultW1 mustMatch {
      case Failure(this.writeException) => true
    }
    val resultR1 = this.successfulRead(newConnection()).get
    /** checks that unsuccessful commit has been rolled back*/
    check(!resultR1.isIsomorphicWith(expectedGraph))

    val resultW2 = this.successfulWrite(newConnection())
    check(resultW2.isSuccess)
    val resultR2 = this.successfulRead(newConnection()).get
    check(resultR2.isIsomorphicWith(expectedGraph))
  }

}
