package org.w3.banana.plantain.model

import akka.http.scaladsl.model.Uri

final class MGraph[S, P, O](var graph: Graph[S, P, O])

final case class BNode(label: String)

final case class Literal(
  lexicalForm: String,
  datatype: Uri,
  langOpt: /*Optional*/String
)
