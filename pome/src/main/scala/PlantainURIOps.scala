package org.w3.banana.pome

import org.w3.banana._
import model._
import java.net.{URI=>jURI}
import akka.http.model.Uri.Path

trait PlantainURIOps extends URIOps[Plantain] {

  def getString(uri: Plantain#URI): String = uri.underlying.toString

  def withoutFragment(uri: Plantain#URI): Plantain#URI =  {
    import uri.underlying._
    URI(new jURI(getScheme,getUserInfo,getHost,getPort,getPath,getQuery,null))
  }

  def withFragment(uri: Plantain#URI, frag: String): Plantain#URI = {
    import uri.underlying._
    URI(new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, frag))
  }

  def getFragment(uri: Plantain#URI): Option[String] = {
    Option(uri.underlying.getFragment)
  }

  def isPureFragment(uri: Plantain#URI): Boolean = {
    import uri.underlying.{getFragment=>fragment,_}
    getScheme == null &&
      getUserInfo == null && getAuthority == null &&
      (getPath == null || getPath == "" ) &&
      getQuery == null && fragment != null
  }

  def resolve(uri: Plantain#URI, other: Plantain#URI): Plantain#URI = {
    URI(uri.underlying.resolve(other.underlying))
  }

  def appendSegment(uri: Plantain#URI, segment: String): Plantain#URI = {
    val path = Path(uri.underlying.getPath)
    val newPath = if (path.reverse.startsWithSlash) {
      path + segment
    } else {
      path / segment
    }
    import uri.underlying.{getFragment=>fragment,_}
    URI(new jURI(getScheme, getUserInfo, getHost, getPort, newPath.toString(), getQuery, fragment))
  }

  def relativize(uri: Plantain#URI, other: Plantain#URI): Plantain#URI = {
    URI(uri.underlying.relativize(other.underlying))
  }

  def newChildUri(uri: Plantain#URI): Plantain#URI = {
    val segment = java.util.UUID.randomUUID().toString.replace("-", "")
    appendSegment(uri, segment)
  }

  def lastSegment(uri: Plantain#URI): String = {
    Path(uri.underlying.getPath).reverse.head.toString
  }
}
