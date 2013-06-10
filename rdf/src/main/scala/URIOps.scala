package org.w3.banana

trait URIOps[Rdf <: RDF] {

  /** returns `uri` without its fragment part if it had one */
  def fragmentLess(uri: Rdf#URI): Rdf#URI

  /** returns `uri` with `frag` as its fragment part, newly added or replaced */
  def fragment(uri: Rdf#URI, frag: String): Rdf#URI

  /** returns the fragment part of `uri` */
  def getFragment(uri: Rdf#URI): Option[String]

  /** basically `parent` / `child` */
  def appendPathname(parent: Rdf#URI, child: String): Rdf#URI

  /** appends a randomly generated pathname to `uri` */
  def newChildUri(uri: Rdf#URI): Rdf#URI

  /** constructs a new URI by parsing the given string and then resolving it against `str` */
  def resolve(uri: Rdf#URI, against: Rdf#URI): Rdf#URI

  /** */
  def relativize(uri: Rdf#URI, against: Rdf#URI): Rdf#URI

  /** */
  def lastPathSegment(uri: Rdf#URI): String

}

