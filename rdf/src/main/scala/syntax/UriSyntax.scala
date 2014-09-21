package org.w3.banana.syntax

import java.net.{ URI => jURI }

import org.w3.banana._

/** all syntax enhancement are directly derived from URIOps */
trait URISyntax[Rdf <: RDF] { self: RDFSyntax[Rdf] =>

  implicit def uriW(uri: Rdf#URI): URIW[Rdf] = new URIW[Rdf](uri)

}

class URIW[Rdf <: RDF](val uri: Rdf#URI) extends AnyVal {

  def getString(implicit ops: URIOps[Rdf]): String = ops.getString(uri)

  def fragmentLess(implicit ops: URIOps[Rdf]): Rdf#URI = ops.withoutFragment(uri)

  def withFragment(fragment: String)(implicit ops: URIOps[Rdf]): Rdf#URI = ops.withFragment(uri, fragment)

  def fragment(implicit ops: URIOps[Rdf]): Option[String] = ops.getFragment(uri)

  def isPureFragment(implicit ops: URIOps[Rdf]): Boolean = ops.isPureFragment(uri)

  def /(segment: String)(implicit ops: URIOps[Rdf]): Rdf#URI = ops.appendSegment(uri, segment)

  def resolve(other: Rdf#URI)(implicit ops: URIOps[Rdf]): Rdf#URI = ops.resolve(uri, other)

  def relativize(other: Rdf#URI)(implicit ops: URIOps[Rdf]): Rdf#URI = ops.relativize(uri, other)

  def relativizeAgainst(other: Rdf#URI)(implicit ops: URIOps[Rdf]): Rdf#URI = ops.relativize(other, uri)

  def lastPathSegment(implicit ops: URIOps[Rdf]): String = ops.lastSegment(uri)

}

