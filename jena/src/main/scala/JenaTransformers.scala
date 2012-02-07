package org.w3.rdf.jena

import org.w3.rdf._

object ScalaToJena extends Transformer[ScalaModule.type, JenaModule.type](ScalaModule, JenaModule)
  
object JenaToScala extends Transformer[JenaModule.type, ScalaModule.type](JenaModule, ScalaModule)
