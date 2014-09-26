package org.w3.banana.plantain

import java.net.{ URI => jURI }

import org.w3.banana._
import org.w3.banana.plantain.model.URI

trait PlantainURIOps extends URIOps[Plantain] {

  def getString(uri: Plantain#URI): String = uri.underlying.toString

  def withoutFragment(uri: Plantain#URI): Plantain#URI = {
    import uri.underlying._
    URI(new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, null))
  }

  def withFragment(uri: Plantain#URI, frag: String): Plantain#URI = {
    import uri.underlying._
    URI(new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, frag))
  }

  def getFragment(uri: Plantain#URI): Option[String] = {
    Option(uri.underlying.getFragment)
  }

  def isPureFragment(uri: Plantain#URI): Boolean = {
    import uri.underlying.{ getFragment => fragment, _ }
    getScheme == null &&
      getUserInfo == null && getAuthority == null &&
      (getPath == null || getPath == "") &&
      getQuery == null && fragment != null
  }

  def resolve(uri: Plantain#URI, other: Plantain#URI): Plantain#URI = {
    URI(uri.underlying.resolve(other.underlying))
  }

  def appendSegment(uri: Plantain#URI, segment: String): Plantain#URI = {
    val u = uri.underlying
    val path = u.getPath
    val newpath = if (path.endsWith("/")) path + segment else path + "/" + segment
    import u._
    val res = new jURI(getScheme, getUserInfo, getHost, getPort, newpath, getQuery, null)
    URI(res)
  }

  def relativize(uri: Plantain#URI, other: Plantain#URI): Plantain#URI = {
    URI(uri.underlying.relativize(other.underlying))
  }

  def lastSegment(uri: Plantain#URI): String = {
    val path = uri.underlying.getPath
    val i = path.lastIndexOf('/')
    path.substring(i + 1, path.length)
  }
}
