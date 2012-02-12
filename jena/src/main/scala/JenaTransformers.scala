package org.w3.rdf.jena

import org.w3.rdf._

object SimpleToJena extends Transformer[SimpleModule.type, JenaModule.type](SimpleModule, JenaModule)
  
object JenaToSimple extends Transformer[JenaModule.type, SimpleModule.type](JenaModule, SimpleModule)
