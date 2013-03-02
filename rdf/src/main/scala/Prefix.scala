package org.w3.banana

import scala.util._

trait Prefix[Rdf <: RDF] {
  def prefixName: String
  def prefixIri: String
  def apply(value: String): Rdf#URI
  def unapply(iri: Rdf#URI): Option[String]
}

object Prefix {
  def apply[Rdf <: RDF](prefixName: String, prefixIri: String)(implicit ops: RDFOps[Rdf]) =
    new PrefixBuilder(prefixName, prefixIri)(ops)
}

class PrefixBuilder[Rdf <: RDF](val prefixName: String, val prefixIri: String)(implicit ops: RDFOps[Rdf]) extends Prefix[Rdf] {

  import ops._

  override def toString: String = "Prefix(" + prefixName + ")"

  def apply(value: String): Rdf#URI = makeUri(prefixIri + value)

  def unapply(iri: Rdf#URI): Option[String] = {
    val uriString = fromUri(iri)
    if (uriString.startsWith(prefixIri))
      Some(uriString.substring(prefixIri.length))
    else
      None
  }

  def getLocalName(iri: Rdf#URI): Try[String] =
    unapply(iri) match {
      case None => Failure(LocalNameException(this.toString + " couldn't extract localname for " + iri.toString))
      case Some(localname) => Success(localname)
    }

}

object RDFPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new RDFPrefix(ops)
}

class RDFPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")(ops) {
  val langString = apply("langString")
  val typ = apply("type")
  val first = apply("first")
  val rest = apply("rest")
  val nil = apply("nil")
}

object XSDPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new XSDPrefix[Rdf](ops)
}

class XSDPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("xsd", "http://www.w3.org/2001/XMLSchema#")(ops) {
  import ops._

  val string = apply("string")
  val int = apply("int")
  val integer = apply("integer")
  val decimal = apply("decimal")
  val double = apply("double")
  val hexBinary = apply("hexBinary")
  val boolean = apply("boolean")
  val trueLit: Rdf#TypedLiteral = makeTypedLiteral("true", boolean)
  val falseLit: Rdf#TypedLiteral = makeTypedLiteral("false", boolean)
  val dateTime = apply("dateTime")
}

object DCPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new DCPrefix(ops)
}

class DCPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("dc", "http://purl.org/dc/elements/1.1/")(ops) {
   val language = apply("language")

}

object DCTPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new DCTPrefix(ops)
}

class DCTPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("dc", "http://purl.org/dc/terms/")(ops) {
  val title = apply("title")

}


object FOAFPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FOAFPrefix(ops)
}

class FOAFPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("foaf", "http://xmlns.com/foaf/0.1/")(ops) {
  val name = apply("name")
  val title = apply("title")
  val knows = apply("knows")
  val currentProject = apply("currentProject")
  val Agent = apply("Agent")
  val Person = apply("Person")
  val age = apply("age")
  val height = apply("height")
  val mbox = apply("mbox")
  val publication = apply("publication")
  val homepage = apply("homepage")
  val wants = apply("wants")
  val author = apply("author")
  val member = apply("member")
}

object IANALinkPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new IANALinkPrefix(ops)
}

class IANALinkPrefix[Rdf<:RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("link", "http://www.iana.org/assignments/link-relations/#")(ops) {
  val about = apply("about")
  val alternate = apply("alternate")
  val appendix = apply("appendix")
  val archives = apply("archives")
  val author = apply("author")
  val bookmark = apply("bookmark")
  val canonical = apply("canonical")
  val chapter = apply("chapter")
  val collection = apply("collection")
  val contents = apply("contents")
  val copyright = apply("copyright")
  val create_form = apply("create-form")
  val current = apply("current")
  val describedby = apply("describedby")
  val describes = apply("describes")
  val disclosure = apply("disclosure")
  val duplicate = apply("duplicate")
  val edit = apply("edit")
  val edit_form = apply("edit-form")
  val edit_media = apply("edit-media")
  val enclosure = apply("enclosure")
  val first = apply("first")
  val glossary = apply("glossary")
  val help = apply("help")
  val hosts = apply("hosts")
  val hub = apply("hub")
  val icon = apply("icon")
  val index = apply("index")
  val item = apply("item")
  val last = apply("last")
  val latest_version = apply("latest-version")
  val license = apply("license")
  val lrdd = apply("lrdd")
  val monitor = apply("monitor")
  val monitor_group = apply("monitor-group")
  val next = apply("next")
  val next_archive = apply("next-archive")
  val nofollow = apply("nofollow")
  val noreferrer = apply("noreferrer")
  val payment = apply("payment")
  val predecessor_version = apply("predecessor-version")
  val prefetch = apply("prefetch")
  val prev = apply("prev")
  val prev_archive = apply("prev-archive")
  val preview = apply("preview")
  val previous = apply("previous")
  val privacy_policy = apply("privacy-policy")
  val profile = apply("profile")
  val related = apply("related")
  val replies = apply("replies")
  val search = apply("search")
  val section = apply("section")
  val self = apply("self")
  val service = apply("service")
  val start = apply("start")
  val stylesheet = apply("stylesheet")
  val subsection = apply("subsection")
  val successor_version = apply("successor-version")
  val tag = apply("tag")
  val terms_of_service = apply("terms-of-service")
  val tpe = apply("type")
  val up = apply("up")
  val version_history = apply("version-history")
  val via = apply("via")
  val working_copy = apply("working-copy")
  val working_copy_of = apply("working-copy-of")

}

trait CommonPrefixes[Rdf <: RDF] { this: RDFOps[Rdf] =>

  lazy val xsd = XSDPrefix(this)
  lazy val rdf = RDFPrefix(this)

}


object WebACLPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new WebACLPrefix(ops)
}

class WebACLPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("acl", "http://www.w3.org/ns/auth/acl#")(ops) {
  val Authorization = apply("Authorization")
  val agent = apply("agent")
  val agentClass = apply("agentClass")
  val accessTo = apply("accessTo")
  val accessToClass = apply("accessToClass")
  val defaultForNew = apply("defaultForNew")
  val mode = apply("mode")
  val Access = apply("Access")
  val Read = apply("Read")
  val Write = apply("Write")
  val Append = apply("Append")
  val accessControl = apply("accessControl")
  val Control = apply("Control")
  val owner = apply("owner")
  val WebIDAgent = apply("WebIDAgent")

  //not officially supported:
  val include = apply("include")
  val regex = apply("regex")
}

object CertPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new CertPrefix(ops)
}

class CertPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("cert", "http://www.w3.org/ns/auth/cert#")(ops) {
   val key = apply("key")
   val RSAKey = apply("RSAKey")
   val RSAPublicKey = apply("RSAPublicKey")
   val exponent = apply("exponent")
   val modulus = apply("modulus")
}