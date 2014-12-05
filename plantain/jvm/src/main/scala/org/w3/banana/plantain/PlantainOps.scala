package org.w3.banana.plantain

import akka.http.model.Uri

object PlantainOps extends org.w3.banana.plantain.generic.PlantainOps[Uri, Plantain] with PlantainURIOps {

  def makeUri(uriStr: String): Plantain#URI = model.URI(Uri(uriStr))

  def makeBNode(): Plantain#BNode = model.BNode(java.util.UUID.randomUUID().toString)

  /**
   * It is overridden because it fails on Generic plantain URI's
   * @param node RDFNode to be folded
   * @param funURI functions that works on URI
   * @param funBNode function that works on BlankNode
   * @param funLiteral function that works on Literal
   * @tparam T
   * @return
   */
  override def foldNode[T](node: Plantain#Node)(funURI: Plantain#URI => T, funBNode: Plantain#BNode => T, funLiteral: Plantain#Literal => T): T = node match {
    case uri: Plantain#URI => funURI(uri)
    case bnode: Plantain#BNode => funBNode(bnode)
    case literal: Plantain#Literal => funLiteral(literal)
  }

}
