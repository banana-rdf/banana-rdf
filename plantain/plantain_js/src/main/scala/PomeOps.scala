package org.w3.banana.pome

import java.net.{ URI => jURI }

import org.w3.banana.plantain.model

object PomeOps extends org.w3.banana.plantain.generic.PlantainOps[jURI, Pome] with PomeURIOps {
  def makeUri(uriStr: String): Pome#URI = org.w3.banana.plantain.model.URI(new jURI(uriStr))
  private var i: Long = 0

  override def makeBNode(): model.BNode = {
    i += 1
    BNode("" + i)
  }
}
