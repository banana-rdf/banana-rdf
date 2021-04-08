package org.w3.banana.plantain

import akka.http.scaladsl.model.Uri
import org.w3.banana._

trait PlantainURIOps extends URIOps[Plantain] {

  def getString(uri: Plantain#URI): String = uri.toString

  def withoutFragment(uri: Plantain#URI): Plantain#URI =
    uri.withoutFragment

  def withFragment(uri: Plantain#URI, frag: String): Plantain#URI =
    uri.withFragment(frag)

  def getFragment(uri: Plantain#URI): Option[String] =
    uri.fragment

  def isPureFragment(u: Plantain#URI): Boolean =
    u.scheme.isEmpty && u.authority.isEmpty && u.path.isEmpty && u.query().isEmpty && u.fragment.isDefined

  def resolve(uri: Plantain#URI, other: Plantain#URI): Plantain#URI =
    other.resolvedAgainst(uri)

  def appendSegment(uri: Plantain#URI, segment: String): Plantain#URI =
    uri.withPath(uri.path ?/ segment)

  /** TODO: avoid going through Java
   * for implementation algorithm, see [[https://github.com/stain/cxf/blob/trunk/rt/frontend/jaxrs/src/main/java/org/apache/cxf/jaxrs/utils/HttpUtils.java  HttpUtils.java]]
   */
  def relativize(uri: Plantain#URI, other: Plantain#URI): Plantain#URI = {
    import java.net.{URI => jURI}
    val juri = new jURI(uri.toString).relativize(new jURI(other.toString))
    Uri(juri.toString)
  }

  def lastSegment(uri: Plantain#URI): String = {
    uri.path.reverse.head.toString
  }

}
