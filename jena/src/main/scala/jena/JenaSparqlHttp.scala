package org.w3.banana.jena

import org.w3.banana._
import java.net.URL

object JenaSparqlHttp extends JenaSparqlHttp

trait JenaSparqlHttp extends SparqlHttp[Jena] {

  def apply(endpoint: URL): SparqlEngine[Jena] = new JenaSparqlHttpEngine(endpoint.toString)

}
