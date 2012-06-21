package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.Graph

class JenaGraphQueryTest() extends RDFGraphQueryTest[Jena, JenaSPARQL](
  JenaOperations,
  JenaDiesel,
  JenaRDFReader.RDFXMLReader,
  JenaGraphIsomorphism,
  JenaSPARQLOperations,
  JenaGraphQuery)
