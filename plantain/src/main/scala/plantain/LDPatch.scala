package org.w3.banana.plantain

import org.w3.banana.RDF
import scala.concurrent.{ExecutionContext, Future}
import org.w3.banana.plantain.{PlantainUtil, Plantain}


trait LDPatch[Rdf<:RDF] {
  def executePatch(graph: Rdf#Graph,
                   query: Rdf#UpdateQuery,
                   bindings: Map[String, Rdf#Node])
                  (implicit ec: ExecutionContext): Future[Rdf#Graph]

  def executePatch(graph: Rdf#Graph,
                   query: Rdf#UpdateQuery)
                  (implicit ec: ExecutionContext): Future[Rdf#Graph]  = executePatch(graph,query,Map())

}

trait PlantainLDPatch extends LDPatch[Plantain]

object PlantainLDPatch extends PlantainLDPatch {
  def executePatch(graph: Plantain#Graph, query: Plantain#UpdateQuery, bindings: Map[String, Plantain#Node])
                  (implicit ec: ExecutionContext) =
    PlantainUtil.executeUpdate(graph, query, bindings)
}
