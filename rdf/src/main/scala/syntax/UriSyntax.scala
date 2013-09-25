package org.w3.banana.syntax

import org.w3.banana._
import java.net.{ URI => jURI }

trait URISyntax[Rdf <: RDF] { self: Syntax[Rdf] =>

  implicit def uriW(uri: Rdf#URI): URIW[Rdf] = new URIW[Rdf](uri)

}

class URIW[Rdf <: RDF] (val uri: Rdf#URI) extends AnyVal {

  def getString(implicit ops: RDFOps[Rdf]): String = ops.fromUri(uri)

  def fragmentLess(implicit ops: RDFOps[Rdf]): Rdf#URI = {
    val uriString = ops.fromUri(uri)
    val juri = new jURI(uriString)
    import juri._
    val uriNoFrag = new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, null)
    ops.makeUri(uriNoFrag.toString)
  }

  def fragment(frag: String)(implicit ops: RDFOps[Rdf]): Rdf#URI = {
    val uriString = ops.fromUri(uri)
    val juri = new jURI(uriString)
    import juri._
    val uriWithFrag = new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, frag)
    ops.makeUri(uriWithFrag.toString)
  }

  def fragment(implicit ops: RDFOps[Rdf]): Option[String] = {
    val uriString = ops.fromUri(uri)
    val juri = new jURI(uriString)
    Option(juri.getFragment)
  }

  def isPureFragment(implicit ops: RDFOps[Rdf]): Boolean = {
    val uriString = ops.fromUri(uri)
    val juri = new jURI(uriString)
    (juri.getScheme == null || juri.getScheme.isEmpty) && (juri.getSchemeSpecificPart == null || juri.getSchemeSpecificPart.isEmpty)
  }

  def /(str: String)(implicit ops: RDFOps[Rdf]): Rdf#URI = {
    val juri = new jURI(ops.fromUri(uri) + "/").resolve(str)
    ops.makeUri(juri.toString)
  }

  def newChildUri()(implicit ops: RDFOps[Rdf]): Rdf#URI = this / java.util.UUID.randomUUID().toString.replaceAll("-", "")

  def resolve(str: String)(implicit ops: RDFOps[Rdf]): Rdf#URI = URIHelper.resolve(uri, str)(ops)

  def resolveAgainst(other: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#URI = URIHelper.resolve(other, uri.toString)(ops)

  def relativize(other: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#URI = URIHelper.relativize(uri, other)(ops)

  def relativizeAgainst(other: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#URI = URIHelper.relativize(other, uri)(ops)

  def lastPathSegment(implicit ops: RDFOps[Rdf]): String = {
    val path = new jURI(ops.fromUri(uri)).getPath
    val i = path.lastIndexOf('/')
    if (i <0) path
    else path.substring(i+1,path.length)
    //    uri.toString.replaceFirst(".*/([^/?]+).*", "$1")
  }

}

