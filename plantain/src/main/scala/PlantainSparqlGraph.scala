package org.w3.banana.plantain

import org.openrdf.model.{ URI => SesameURI }
import org.w3.banana._

import scala.concurrent.{ ExecutionContext, Future }

class PlantainGraphSparqlEngine(ec: ExecutionContext) extends SparqlEngine[Plantain, Plantain#Graph] {
  implicit val ecc = ec

  /** Executes a Select query. */
  override def executeSelect(graph: Plantain#Graph,
    query: Plantain#SelectQuery,
    bindings: Map[String, Plantain#Node]) = Future {
    PlantainUtil.executeSelect(graph, query, bindings)
  }

  /** Executes a Construct query. */
  override def executeConstruct(graph: Plantain#Graph,
    query: Plantain#ConstructQuery,
    bindings: Map[String, Plantain#Node]) = Future {
    PlantainUtil.executeConstruct(graph, query, bindings)
  }
  /** Executes a Ask query. */
  override def executeAsk(graph: Plantain#Graph,
    query: Plantain#AskQuery,
    bindings: Map[String, Plantain#Node]) = Future {
    PlantainUtil.executeAsk(graph, query, bindings)
  }
}

object PlantainGraphSparqlEngine {
  def apply() = new PlantainGraphSparqlEngine(sameThreadExecutionContext)
}
