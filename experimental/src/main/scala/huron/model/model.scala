package org.w3.banana.huron.model

import org.w3.banana.huron.Huron

sealed trait NodeMatch

case class PlainNode(node: Huron#Node) extends NodeMatch

case object ANY extends NodeMatch
