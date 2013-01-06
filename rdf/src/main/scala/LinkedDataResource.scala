package org.w3.banana

/** An RDF Resource that can be located on the Web.
  * 
  * @param location the location on the Web where `resource` can be found
  * @param resource the [[PointedResource]] whose `graph` was found at `location`, and `pointer` exists in it
  */
case class LinkedDataResource[Rdf <: RDF](location: Rdf#URI, resource: PointedGraph[Rdf])
