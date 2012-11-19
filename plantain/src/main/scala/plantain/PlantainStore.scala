package org.w3.banana.plantain

import org.w3.banana._
import java.nio.file._
import scala.concurrent._
import scala.concurrent.stm._
import akka.actor._
import akka.util._
import akka.pattern.{ ask, pipe }
import akka.transactor._
import scalaz.Free
import org.openrdf.model.{ URI => SesameURI, _ }
import org.openrdf.model.impl._
import org.openrdf.query.algebra.evaluation.TripleSource
import org.openrdf.query.QueryEvaluationException
import info.aduna.iteration.CloseableIteration
import PlantainUtil._

object StoreActor {

  case class Script[A](script: Free[({ type l[+x] = Command[Plantain, x] })#l, A])

}

class TMapTripleSource(graphs: TMap[URI, Graph]) extends TripleSource {

  def getValueFactory(): org.openrdf.model.ValueFactory = ???

  def getStatements(subject: Resource, predicate: SesameURI, objectt: Value, contexts: Resource*): CloseableIteration[Statement, QueryEvaluationException] = {
    val iterator: Iterator[Statement] = if (contexts.isEmpty) {
      for {
        (uri, graph) <- graphs.single.iterator
        statement <- graph.getStatements(subject, predicate, objectt).toIterator
      } yield {
        statement.withContext(uri.asSesame.asInstanceOf[Resource])
      }
    } else {
      for {
        context <- contexts.iterator
        if context.isInstanceOf[SesameURI]
        uri = context.asInstanceOf[SesameURI]
        graph <- graphs.single.lift(Node.fromSesame(uri)).toIterator
        statement <- graph.getStatements(subject, predicate, objectt).toIterator
      } yield {
        statement.withContext(uri)
      }
    }
    iterator.toCloseableIteration[QueryEvaluationException]
  }


}

import StoreActor._

class StoreActor(baseUri: URI, root: Path) extends Actor {

  val graphs = TMap.empty[URI, Graph]

  def tripleSource: TripleSource = new TMapTripleSource(graphs)

  def receive = {
    case coordinated @ Coordinated(Script(script)) => ???
  }

}

class PlantainStore(baseUri: URI, root: Path)(implicit timeout: Timeout = Timeout(5000)) extends RDFStore[Plantain, Future] {

  val system = ActorSystem("plantain")

  val storeActor = system.actorOf(Props(new StoreActor(baseUri, root)), name = "store")

  def shutdown(): Unit = {
    system.shutdown()
  }

  def execute[A](script: Free[({ type l[+x] = Command[Plantain, x] })#l, A]): Future[A] = {
    (storeActor ? Coordinated(Script(script))).asInstanceOf[Future[A]]
  }

}
