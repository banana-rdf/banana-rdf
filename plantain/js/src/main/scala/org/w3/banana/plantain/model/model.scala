package org.w3.banana.plantain.model

import org.w3.banana._
import java.net.URI

final class MGraph[S, P, O](var graph: IntHexastoreGraph[S, P, O])

final case class BNode(label: String)

final case class Literal(
  lexicalForm: String,
  datatype: URI,
  langOpt: /*Optional*/String
)
