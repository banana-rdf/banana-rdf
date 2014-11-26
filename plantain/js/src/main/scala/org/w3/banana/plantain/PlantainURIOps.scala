package org.w3.banana.plantain

import org.w3.banana._
import java.net.{ URI => jURI }

trait PlantainURIOps extends URIOps[Plantain] {

  def getString(uri: Plantain#URI): String = uri.toString

  def withoutFragment(uri: Plantain#URI): Plantain#URI = {
    import uri._
    new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, null)
  }

  def withFragment(uri: Plantain#URI, frag: String): Plantain#URI = {
    import uri._
    new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, frag)
  }

  def getFragment(uri: Plantain#URI): Option[String] = {
    Option(uri.getFragment)
  }

  def isPureFragment(uri: Plantain#URI): Boolean = {
    import uri.{ getFragment => fragment, _ }
    getScheme == null &&
      getUserInfo == null && getAuthority == null &&
      (getPath == null || getPath == "") &&
      getQuery == null && fragment != null
  }

  def resolve(uri: Plantain#URI, other: Plantain#URI): Plantain#URI =
    uri.resolve(other)

  def appendSegment(uri: Plantain#URI, segment: String): Plantain#URI = {
    import uri.{ getPath => path, _ }
    val newpath = if (path.endsWith("/")) path + segment else path + "/" + segment
    new jURI(getScheme, getUserInfo, getHost, getPort, newpath, getQuery, null)
  }

  def relativize(uri: Plantain#URI, other: Plantain#URI): Plantain#URI =
    uri.relativize(other)

  def lastSegment(uri: Plantain#URI): String = {
    import uri.{ getPath => path, _ }
    val i = path.lastIndexOf('/')
    path.substring(i + 1, path.length)
  }

}
