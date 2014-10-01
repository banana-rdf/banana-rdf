package org.w3.banana.plantain

import org.w3.banana._
import org.w3.banana.plantain.model._

trait PlantainURIOps extends URIOps[Plantain] {

  def getString(uri: Plantain#URI): String = uri.underlying.toString

  def withoutFragment(uri: Plantain#URI): Plantain#URI =
    URI(uri.underlying.withoutFragment)

  def withFragment(uri: Plantain#URI, frag: String): Plantain#URI =
    URI(uri.underlying.withFragment(frag))

  def getFragment(uri: Plantain#URI): Option[String] = uri.underlying.fragment

  def isPureFragment(uri: Plantain#URI): Boolean = {
    val u = uri.underlying
    u.scheme.isEmpty && u.authority.isEmpty && u.path.isEmpty && u.query.isEmpty && u.fragment.isDefined
  }

  def resolve(uri: Plantain#URI, other: Plantain#URI): Plantain#URI = {
    URI(other.underlying.resolvedAgainst(uri.underlying))
  }

  def appendSegment(uri: Plantain#URI, segment: String): Plantain#URI = {
    val underlying = uri.underlying
    val path = underlying.path
    if (path.reverse.startsWithSlash)
      URI(underlying.copy(path = path + segment))
    else
      URI(underlying.copy(path = path / segment))
  }

  def relativize(uri: Plantain#URI, other: Plantain#URI): Plantain#URI = {
    // TODO should rely on a spray.http.Uri when https://github.com/spray/spray/issues/818 is addressed
    // for implementation algorithm, see https://github.com/stain/cxf/blob/trunk/rt/frontend/jaxrs/src/main/java/org/apache/cxf/jaxrs/utils/HttpUtils.java
    import java.net.{ URI => jURI }
    val juri = new jURI(uri.underlying.toString).relativize(new jURI(other.underlying.toString))
    PlantainOps.makeUri(juri.toString)
  }

  def lastSegment(uri: Plantain#URI): String =
    uri.underlying.path.reverse.head.toString

}
