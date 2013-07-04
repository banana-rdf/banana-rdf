package org.w3.banana.plantain

import org.w3.banana.RDF
import scala.concurrent.{ExecutionContext, Future}
import org.w3.banana.plantain.{PlantainUtil, Plantain}
import scala.util.Try


trait LDPatch[Rdf<:RDF,M[_]] {
  def executePatch(graph: Rdf#Graph,
                   query: Rdf#UpdateQuery,
                   bindings: Map[String, Rdf#Node]): M[Rdf#Graph]

  def executePatch(graph: Rdf#Graph,
                   query: Rdf#UpdateQuery): M[Rdf#Graph]  = executePatch(graph,query,Map())

}

trait PlantainLDPatch extends LDPatch[Plantain,Try]

object PlantainLDPatch extends PlantainLDPatch {
  def executePatch(graph: Plantain#Graph, query: Plantain#UpdateQuery, bindings: Map[String, Plantain#Node]) =
    PlantainUtil.executeUpdate(graph, query, bindings)
}
