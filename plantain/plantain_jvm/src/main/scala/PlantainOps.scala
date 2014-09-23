package org.w3.banana.plantain

import akka.http.model.Uri

object PlantainOps extends org.w3.banana.plantain.generic.PlantainOps[Uri, Plantain] with PlantainURIOps {
  def makeUri(uriStr: String): Plantain#URI = model.URI(Uri(uriStr))
}
