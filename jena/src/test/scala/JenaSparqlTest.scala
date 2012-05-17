package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.Graph

class JenaSparqlTest() extends SparqlTest[Jena, JenaSPARQL](
  JenaOperations,
  JenaRDFXMLReader,
  JenaGraphIsomorphism,
  JenaSPARQLOperations,
  graph => JenaSPARQLEngine(graph))
