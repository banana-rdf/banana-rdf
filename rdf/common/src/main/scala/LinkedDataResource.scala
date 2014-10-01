package org.w3.banana

/**
 * An RDF Resource that can be located on the Web.
 *
 * @param location
 * @param resource
 * @param meta
 */
trait LinkedDataResource[Rdf <: RDF] {

  /** the location on the Web where `resource` can be found */
  def location: Rdf#URI

  /** the [[org.w3.banana.PointedGraph]] whose `graph` was found at `location`, and `pointer` exists in it */
  def resource: PointedGraph[Rdf]
}

object LinkedDataResource {

  def apply[Rdf <: RDF](_location: Rdf#URI, _resource: PointedGraph[Rdf]): LinkedDataResource[Rdf] =
    new LinkedDataResource[Rdf] {
      val location = _location
      val resource = _resource
    }

  def unapply[Rdf <: RDF](ldr: LinkedDataResource[Rdf]): Option[(Rdf#URI, PointedGraph[Rdf])] = Some((ldr.location, ldr.resource))

}
