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
import org.slf4j.{ Logger, LoggerFactory }

object StoreActor {

  case class Script[A](script: Plantain#Script[A])

}

class TMapTripleSource(graphs: TMap[URI, Graph]) extends TripleSource {

  def getValueFactory(): org.openrdf.model.ValueFactory = ???

  def getStatements(subject: Resource, predicate: SesameURI, objectt: Value, contexts: Resource*): CloseableIteration[Statement, QueryEvaluationException] = {
    val iterator: Iterator[Statement] = if (contexts.isEmpty) {
      PlantainStore.logger.warn(s"""_very_ inefficient pattern ($subject, $predicate, $objectt, ANY)""")
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

  def run[A](coordinated: Coordinated, script: Plantain#Script[A])(implicit t: InTxn): A = {
    script.resume fold (
      {
        case Create(uri, a) => {
          graphs.put(uri, Graph.empty)
          run(coordinated, a)
        }
        case Delete(uri, a) => {
          graphs.remove(uri)
          run(coordinated, a)
        }
        case Get(uri, k) => {
          val graph = graphs(uri)
          run(coordinated, k(graph))
        }
        case Append(uri, triples, a) => {
          val current = graphs.get(uri) getOrElse Graph.empty
          val graph = triples.foldLeft(current){ _ + _ }
          graphs.put(uri, graph)
          run(coordinated, a)
        }
        case Remove(uri, tripleMatches, a) => {
          val current = graphs.get(uri) getOrElse Graph.empty
          val graph = tripleMatches.foldLeft(current){ _ - _ }
          graphs.put(uri, graph)
          run(coordinated, a)
        }
        case Select(query, bindings, k) => {
          val solutions = PlantainUtil.executeSelect(tripleSource, query, bindings)
          run(coordinated, k(solutions))
        }
        case Construct(query, bindings, k) => {
          val graph = PlantainUtil.executeConstruct(tripleSource, query, bindings)
          run(coordinated, k(graph))
        }
        case Ask(query, bindings, k) => {
          val b = PlantainUtil.executeAsk(tripleSource, query, bindings)
          run(coordinated, k(b))
        }
      },
      a => a
    )
  }

  def receive = {
    case coordinated @ Coordinated(Script(script)) => coordinated atomic { implicit t =>
      val r = run(coordinated, script)
      sender ! r
    }
  }

}

object PlantainStore {

  val logger = LoggerFactory.getLogger(classOf[PlantainStore])

  def apply(baseUri: URI, root: Path)(implicit timeout: Timeout = Timeout(5000)): PlantainStore =
    new PlantainStore(baseUri, root)

}

class PlantainStore(baseUri: URI, root: Path)(implicit timeout: Timeout = Timeout(5000)) extends RDFStore[Plantain, Future] {

  val system = ActorSystem("plantain")

  val storeActor = system.actorOf(Props(new StoreActor(baseUri, root)), name = "store")

  def shutdown(): Unit = {
    system.shutdown()
  }

  def execute[A](script: Plantain#Script[A]): Future[A] = {
    (storeActor ? Coordinated(Script(script))).asInstanceOf[Future[A]]
  }

}
