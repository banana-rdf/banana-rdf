package org.w3.banana.pome

import org.w3.banana._
import model._

trait PlantainURIOps extends URIOps[Plantain] {

  def getString(uri: Plantain#URI): String = uri.string.toString

  def withoutFragment(uri: Plantain#URI): Plantain#URI =  new LazyURI(uri.parsed.withoutFragment)

  def withFragment(uri: Plantain#URI, frag: String): Plantain#URI = new LazyURI(uri.parsed.withFragment(frag))

  def getFragment(uri: Plantain#URI): Option[String] = uri.parsed.fragment

  def isPureFragment(uri: Plantain#URI): Boolean = {
    val u = uri.parsed
    u.scheme.isEmpty && u.authority.isEmpty && u.path.isEmpty && u.query.isEmpty && u.fragment.isDefined
  }

  def resolve(uri: Plantain#URI, other: Plantain#URI): Plantain#URI = {
    new LazyURI(other.parsed.resolvedAgainst(uri.string))
  }

  def appendSegment(uri: Plantain#URI, segment: String): Plantain#URI = {
    val underlying = uri.parsed
    val path = underlying.path
    if (path.reverse.startsWithSlash)
      new LazyURI(underlying.copy(path = path + segment))
    else
      new LazyURI(underlying.copy(path = path / segment))
  }

  def relativize(uri: Plantain#URI, other: Plantain#URI): Plantain#URI = {
    // TODO should rely on a spray.http.Uri when https://github.com/spray/spray/issues/818 is addressed
    // for implementation algorithm, see https://github.com/stain/cxf/blob/trunk/rt/frontend/jaxrs/src/main/java/org/apache/cxf/jaxrs/utils/HttpUtils.java
//    import java.net.{ URI => jURI }
//    val juri = new jURI(uri.string.toString).relativize(new jURI(other.string.toString))
//    PlantainOps.makeUri(juri.toString)
    ???
  }

  def newChildUri(uri: Plantain#URI): Plantain#URI = {
    val segment = java.util.UUID.randomUUID().toString.replace("-", "")
    appendSegment(uri, segment)
  }

  def lastSegment(uri: Plantain#URI): String =
    uri.parsed.path.reverse.head.toString

}
