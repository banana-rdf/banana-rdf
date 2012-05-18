package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.Graph

class JenaGraphQueryTest() extends RDFGraphQueryTest[Jena, JenaSPARQL](
  JenaOperations,
  JenaRDFXMLReader,
  JenaGraphIsomorphism,
  JenaSPARQLOperations,
  JenaGraphQuery)
