package org.w3.rdf.jena

import org.w3.rdf._

object ScalaToJena extends Transformer[ScalaModel.type, JenaModel.type](ScalaModel, JenaModel)
  
object JenaToScala extends Transformer[JenaModel.type, ScalaModel.type](JenaModel, ScalaModel)
