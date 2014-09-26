package org.w3.banana.plantain

import java.net.{ URI => jURI }

import org.w3.banana.plantain.model

object PlantainOps extends org.w3.banana.plantain.generic.PlantainOps[jURI, Plantain] with PlantainURIOps {
  def makeUri(uriStr: String): Plantain#URI = org.w3.banana.plantain.model.URI(new jURI(uriStr))
  private var i: Long = 0

  override def makeBNode(): model.BNode = {
    i += 1
    BNode("" + i)
  }
}
