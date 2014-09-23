package org.w3.banana.pome

import java.net.{ URI => jURI }

import org.w3.banana._
import org.w3.banana.plantain.model.URI

trait PomeURIOps extends URIOps[Pome] {

  def getString(uri: Pome#URI): String = uri.underlying.toString

  def withoutFragment(uri: Pome#URI): Pome#URI = {
    import uri.underlying._
    URI(new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, null))
  }

  def withFragment(uri: Pome#URI, frag: String): Pome#URI = {
    import uri.underlying._
    URI(new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, frag))
  }

  def getFragment(uri: Pome#URI): Option[String] = {
    Option(uri.underlying.getFragment)
  }

  def isPureFragment(uri: Pome#URI): Boolean = {
    import uri.underlying.{ getFragment => fragment, _ }
    getScheme == null &&
      getUserInfo == null && getAuthority == null &&
      (getPath == null || getPath == "") &&
      getQuery == null && fragment != null
  }

  def resolve(uri: Pome#URI, other: Pome#URI): Pome#URI = {
    URI(uri.underlying.resolve(other.underlying))
  }

  def appendSegment(uri: Pome#URI, segment: String): Pome#URI = {
    val u = uri.underlying
    val path = u.getPath
    val newpath = if (path.endsWith("/")) path + segment else path + "/" + segment
    import u._
    val res = new jURI(getScheme, getUserInfo, getHost, getPort, newpath, getQuery, null)
    URI(res)
  }

  def relativize(uri: Pome#URI, other: Pome#URI): Pome#URI = {
    URI(uri.underlying.relativize(other.underlying))
  }

  def lastSegment(uri: Pome#URI): String = {
    val path = uri.underlying.getPath
    val i = path.lastIndexOf('/')
    path.substring(i + 1, path.length)
  }
}
