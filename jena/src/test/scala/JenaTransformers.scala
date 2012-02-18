package org.w3.rdf.jena

import org.w3.rdf.Transformer
import org.w3.rdf.simple.SimpleModule

object SimpleToJena extends Transformer[SimpleModule.type, JenaModule.type](SimpleModule, JenaModule)
  
object JenaToSimple extends Transformer[JenaModule.type, SimpleModule.type](JenaModule, SimpleModule)
