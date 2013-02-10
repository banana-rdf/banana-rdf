package org.w3.banana.jena

import org.w3.banana._
import java.net.URL
import scalaz.Id.Id

object JenaSparqlHttp extends JenaSparqlHttp

trait JenaSparqlHttp extends SparqlHttp[Jena, Id] {

  def apply(endpoint: URL): SparqlEngine[Jena, Id] = new JenaSparqlHttpEngine(endpoint.toString)

}
