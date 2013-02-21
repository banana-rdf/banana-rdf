package org.w3.banana.plantain

import org.w3.banana._
import org.w3.banana.plantain.model._
import java.net.{ URI => jURI }

/** offers an alternative to the default URISyntax implementation in the case of plantain */
object PlantainURISyntax {

  implicit def uriSyntax(uri: Plantain#URI): syntax.URIW[Plantain] = new PlantainURISyntax(uri)

}

class PlantainURISyntax(val uri: URI) extends AnyVal with syntax.URIW[Plantain] {

  def getString(implicit ops: RDFOps[Plantain]): String = uri.underlying.toString

  def fragmentLess(implicit ops: RDFOps[Plantain]): Plantain#URI = {
    import uri.underlying._
    val uriNoFrag = new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, null)
    URI(uriNoFrag)
  }

  def fragment(frag: String)(implicit ops: RDFOps[Plantain]): Plantain#URI = {
    import uri.underlying._
    val uriWithFrag = new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, frag)
    URI(uriWithFrag)
  }

  def fragment(implicit ops: RDFOps[Plantain]): Option[String] = {
    Option(uri.underlying.getFragment)
  }

  def isPureFragment(implicit ops: RDFOps[Plantain]): Boolean = {
    import uri.{ underlying => juri }
    (juri.getScheme == null || juri.getScheme.isEmpty) && (juri.getSchemeSpecificPart == null || juri.getSchemeSpecificPart.isEmpty)
  }

  def /(str: String)(implicit ops: RDFOps[Plantain]): Plantain#URI = {
    URI(new jURI(uri.underlying.toString + "/").resolve(str))
  }

  def newChildUri()(implicit ops: RDFOps[Plantain]): Plantain#URI = this / java.util.UUID.randomUUID().toString.replaceAll("-", "")

  def resolve(str: String)(implicit ops: RDFOps[Plantain]): Plantain#URI = {
    URI(uri.underlying.resolve(str))
  }

  def resolveAgainst(other: Plantain#URI)(implicit ops: RDFOps[Plantain]): Plantain#URI = {
    URI(other.underlying.resolve(uri.underlying.toString))
  }

  def relativize(other: Plantain#URI)(implicit ops: RDFOps[Plantain]): Plantain#URI = {
    URI(uri.underlying.relativize(other.underlying))
  }

  def relativizeAgainst(other: Plantain#URI)(implicit ops: RDFOps[Plantain]): Plantain#URI = {
    URI(other.underlying.relativize(uri.underlying))
  }


  def lastPathSegment(implicit ops: RDFOps[Plantain]): String = {
    val path = uri.underlying.getPath
    val i = path.lastIndexOf('/')
    path.substring(i+1,path.length)
  }

}
