package org.w3.banana.plantain

import org.w3.banana._
import org.openrdf.model.{ URI => SesameURI, _ }
import org.openrdf.model.impl._
import scala.collection.JavaConversions._
import info.aduna.iteration.CloseableIteration
import org.openrdf.query._
import org.openrdf.query.impl._
import scalaz.Id._
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl
import PlantainUtil._
import scala.concurrent.Future
import scala.concurrent.Future.successful

object PlantainSparqlGraph extends PlantainSparqlGraph

trait PlantainSparqlGraph extends SparqlGraph[Plantain] {  

  def apply(graph: Plantain#Graph): SparqlEngine[Plantain] =
    new PlantainSparqlEngine(graph)

  class PlantainSparqlEngine(graph: Plantain#Graph) extends SparqlEngine[Plantain] {

    def executeSelect(query: Plantain#SelectQuery, bindings: Map[String, Plantain#Node]): Future[Plantain#Solutions] = successful {
      PlantainUtil.executeSelect(graph, query, bindings)
    }

    def executeConstruct(query: Plantain#ConstructQuery, bindings: Map[String, Plantain#Node]): Future[Plantain#Graph] = successful {
      PlantainUtil.executeConstruct(graph, query, bindings)
    }

    def executeAsk(query: Plantain#AskQuery, bindings: Map[String, Plantain#Node]): Future[Boolean] = successful {
      PlantainUtil.executeAsk(graph, query, bindings)
    }

  }



}

