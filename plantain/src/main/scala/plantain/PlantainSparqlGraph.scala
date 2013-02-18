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

object PlantainSparqlGraph extends PlantainSparqlGraph

trait PlantainSparqlGraph extends SparqlGraph[Plantain] {  

  def apply(graph: Plantain#Graph): SparqlEngine[Plantain, Id] =
    new PlantainSparqlEngine(graph)

  class PlantainSparqlEngine(graph: Plantain#Graph) extends SparqlEngine[Plantain, Id] {

    def executeSelect(query: Plantain#SelectQuery, bindings: Map[String, Plantain#Node]): Plantain#Solutions =
      PlantainUtil.executeSelect(graph, query, bindings)

    def executeConstruct(query: Plantain#ConstructQuery, bindings: Map[String, Plantain#Node]): Plantain#Graph =
      PlantainUtil.executeConstruct(graph, query, bindings)

    def executeAsk(query: Plantain#AskQuery, bindings: Map[String, Plantain#Node]): Boolean =
      PlantainUtil.executeAsk(graph, query, bindings)

  }



}

