package org.w3.banana.jena

import org.w3.banana._
import java.net.URL

class JenaSparqlHttp(ops: RDFOps[Jena]) extends SparqlHttp[Jena] {

  def apply(endpoint: URL): SparqlEngine[Jena] = new JenaSparqlHttpEngine(ops, endpoint.toString)

}
