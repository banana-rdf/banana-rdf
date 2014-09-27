package org.w3.banana

trait URIOps[Rdf <: RDF] {

  def getString(uri: Rdf#URI): String

  /** returns `uri` without its fragment part if it had one */
  def withoutFragment(uri: Rdf#URI): Rdf#URI

  /** returns `uri` with `frag` as its fragment part, newly added or replaced */
  def withFragment(uri: Rdf#URI, frag: String): Rdf#URI

  /** returns the fragment part of `uri` */
  def getFragment(uri: Rdf#URI): Option[String]

  /** */
  def isPureFragment(uri: Rdf#URI): Boolean

  /** */
  def resolve(uri: Rdf#URI, other: Rdf#URI): Rdf#URI

  /** */
  def appendSegment(uri: Rdf#URI, segment: String): Rdf#URI

  /** */
  def relativize(uri: Rdf#URI, other: Rdf#URI): Rdf#URI

  /** */
  def lastSegment(uri: Rdf#URI): String

}

trait DefaultURIOps[Rdf <: RDF] extends URIOps[Rdf] { ops: RDFOps[Rdf] =>

  import java.net.{ URI => jURI }

  def getString(uri: Rdf#URI): String = ops.fromUri(uri)

  def withoutFragment(uri: Rdf#URI): Rdf#URI = {
    val uriString = ops.fromUri(uri)
    val juri = new jURI(uriString)
    import juri._
    val uriNoFrag = new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, null)
    ops.makeUri(uriNoFrag.toString)
  }

  def withFragment(uri: Rdf#URI, frag: String): Rdf#URI = {
    val uriString = ops.fromUri(uri)
    val juri = new jURI(uriString)
    import juri._
    val uriWithFrag = new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, frag)
    ops.makeUri(uriWithFrag.toString)
  }

  def getFragment(uri: Rdf#URI): Option[String] = {
    val uriString = ops.fromUri(uri)
    val juri = new jURI(uriString)
    Option(juri.getFragment)
  }

  def isPureFragment(uri: Rdf#URI): Boolean = {
    val uriString = ops.fromUri(uri)
    val juri = new jURI(uriString)
    (juri.getScheme == null || juri.getScheme.isEmpty) && (juri.getSchemeSpecificPart == null || juri.getSchemeSpecificPart.isEmpty)
  }

  def resolve(uri: Rdf#URI, against: Rdf#URI): Rdf#URI = {
    val againstUri = against.toString
    val juri =
      if (againstUri.isEmpty) new jURI(uri.toString.split("#")(0))
      else new jURI(uri.toString).resolve(againstUri)
    ops.makeUri(juri.toString)
  }

  def appendSegment(uri: Rdf#URI, segment: String): Rdf#URI = {
    val juri = new jURI(ops.fromUri(uri) + "/").resolve(segment)
    ops.makeUri(juri.toString)
  }

  def relativize(uri: Rdf#URI, other: Rdf#URI): Rdf#URI = {
    val juri = new jURI(uri.toString).relativize(new jURI(other.toString))
    ops.makeUri(juri.toString)
  }

  def lastSegment(uri: Rdf#URI): String = {
    val path = new jURI(ops.fromUri(uri)).getPath
    val i = path.lastIndexOf('/')
    if (i < 0)
      path
    else
      path.substring(i + 1, path.length)
  }

}

