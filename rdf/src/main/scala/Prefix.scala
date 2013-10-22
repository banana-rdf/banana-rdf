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

object RDFSPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new RDFSPrefix(ops)
}

class RDFSPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("rdf", "http://www.w3.org/2000/01/rdf-schema#")(ops) {
  val Class = apply("Class")
  val Container = apply("Container")
  val ContainerMembershipProperty = apply("ContainerMembershipProperty")
  val Datatype = apply("Datatype")
  val Literal = apply("Literal")
  val Resource = apply("Resource")
  val comment = apply("comment")
  val domain = apply("domain")
  val isDefinedBy = apply("isDefinedBy")
  val label = apply("label")
  val member = apply("member")
  val range = apply("range")
  val seeAlso = apply("seeAlso")
  val subClassOf = apply("subClassOf")
  val subPropertyOf = apply("subPropertyOf")
}

object RDFPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new RDFPrefix(ops)
}

class RDFPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")(ops) {
  val langString = apply("langString") //todo: does not exist in ontology
  val nil = apply("nil")
  val typ = apply("type")
  val Alt = apply("Alt")
  val Bag = apply("Bag")
  val List = apply("List")
  val Property = apply("Property")
  val Seq = apply("Seq")
  val Statement = apply("Statement")
  val first = apply("first")
  val obj = apply("object")
  val predicate = apply("predicate")
  val rest = apply("rest")
  val subject = apply("subject")
  val value = apply("value")
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
  //todo: remove these relations as they don't exist in foaf
  val height = apply("height")
  val publication = apply("publication")
  val wants = apply("wants")
  val author = apply("author")

  //resources that actually exist in foaf
  val Agent = apply("Agent")
  val Document = apply("Document")
  val Group = apply("Group")
  val Image = apply("Image")
  val LabelProperty = apply("LabelProperty")
  val OnlineAccount = apply("OnlineAccount")
  val OnlineChatAccount = apply("OnlineChatAccount")
  val OnlineEcommerceAccount = apply("OnlineEcommerceAccount")
  val OnlineGamingAccount = apply("OnlineGamingAccount")
  val Organization = apply("Organization")
  val Person = apply("Person")
  val PersonalProfileDocument = apply("PersonalProfileDocument")
  val Project = apply("Project")
  val account = apply("account")
  val accountName = apply("accountName")
  val accountServiceHomepage = apply("accountServiceHomepage")
  val age = apply("age")
  val aimChatID = apply("aimChatID")
  val based_near = apply("based_near")
  val birthday = apply("birthday")
  val currentProject = apply("currentProject")
  val depiction = apply("depiction")
  val depicts = apply("depicts")
  val dnaChecksum = apply("dnaChecksum")
  val familyName = apply("familyName")
  val family_name = apply("family_name")
  val firstName = apply("firstName")
  val focus = apply("focus")
  val fundedBy = apply("fundedBy")
  val geekcode = apply("geekcode")
  val gender = apply("gender")
  val givenName = apply("givenName")
  val givenname = apply("givenname")
  val holdsAccount = apply("holdsAccount")
  val homepage = apply("homepage")
  val icqChatID = apply("icqChatID")
  val img = apply("img")
  val interest = apply("interest")
  val isPrimaryTopicOf = apply("isPrimaryTopicOf")
  val jabberID = apply("jabberID")
  val knows = apply("knows")
  val lastName = apply("lastName")
  val logo = apply("logo")
  val made = apply("made")
  val maker = apply("maker")
  val mbox = apply("mbox")
  val mbox_sha1sum = apply("mbox_sha1sum")
  val member = apply("member")
  val membershipClass = apply("membershipClass")
  val msnChatID = apply("msnChatID")
  val myersBriggs = apply("myersBriggs")
  val name = apply("name")
  val nick = apply("nick")
  val openid = apply("openid")
  val page = apply("page")
  val pastProject = apply("pastProject")
  val phone = apply("phone")
  val plan = apply("plan")
  val primaryTopic = apply("primaryTopic")
  val publications = apply("publications")
  val schoolHomepage = apply("schoolHomepage")
  val sha1 = apply("sha1")
  val skypeID = apply("skypeID")
  val status = apply("status")
  val surname = apply("surname")
  val theme = apply("theme")
  val thumbnail = apply("thumbnail")
  val tipjar = apply("tipjar")
  val title = apply("title")
  val topic = apply("topic")
  val topic_interest = apply("topic_interest")
  val weblog = apply("weblog")
  val workInfoHomepage = apply("workInfoHomepage")
  val workplaceHomepage = apply("workplaceHomepage")
  val yahooChatID = apply("yahooChatID")

}

object LDPPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new LDPPrefix(ops)
}

class LDPPrefix[Rdf<:RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("ldp", "http://www.w3.org/ns/ldp#")(ops) {
  val AggregateContainer = apply("AggregateContainer")
  val CompositeContainer = apply("CompositeContainer")
  val Container = apply("Container")
  val Page = apply("Page")
  val Resource = apply("Resource")
  val containerSortPredicates = apply("containerSortPredicates")
  val membershipPredicate = apply("membershipPredicate")
  val membershipSubject = apply("membershipSubject")
  val nextPage = apply("nextPage")
  val created = apply("created")
  val pageOf = apply("pageOf")
}

object STATPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new STATPrefix(ops)
}

class STATPrefix[Rdf<:RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("stat", "http://www.w3.org/ns/posix/stat#")(ops) {
  val atime = apply("atime")
  val blksize = apply("blksize")
  val blocks = apply("blocks")
  val ctime = apply("ctime")
  val dev = apply("dev")
  val gid = apply("gid")
  val ino = apply("ino")
  val mode = apply("mode")
  val mtime = apply("mtime")
  val nlink = apply("nlink")
  val rdev = apply("rdev")
  val size = apply("size")
  val uid = apply("uid")
}

object IANALinkPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new IANALinkPrefix(ops)
}

/**
 * The Iana Link Relations are not linked data so these URLs are currently invented ones, and need
 * not reflect what may be used if ever such URIs are coined.
 * @param ops
 * @tparam Rdf
 */
class IANALinkPrefix[Rdf<:RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("link", "http://www.iana.org/assignments/link-relations/#")(ops) {
  val about = apply("about")
  val acl = apply("acl")               //needs to be registered
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
  val meta = apply("meta")       //needs to be registered
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

object SiocPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new SiocPrefix(ops)
}

class SiocPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("sioc","http://rdfs.org/sioc/ns#")(ops) {
  val Community = apply("Community")
  val Container = apply("Container")
  val Forum = apply("Forum")
  val Item = apply("Item")
  val Post = apply("Post")
  val Role = apply("Role")
  val Space = apply("Space")
  val Site = apply("Site")
  val Thread = apply("Thread")
  val UserAccount = apply("UserAccount")
  val Usergroup = apply("Usergroup")
  val about = apply("about")
  val account_of = apply("account_of")
  val addressed_to = apply("addressed_to")
  val administrator_of = apply("administrator_of")
  val attachment = apply("attachment")
  val avatar = apply("avatar")
  val container_of = apply("container_of")
  val content = apply("content")
  val creator_of = apply("creator_of")
  val earlier_version = apply("earlier_version")
  val email = apply("email")
  val email_sha1 = apply("email_sha1")
  val embeds_knowledge = apply("embeds_knowledge")
  val feed = apply("feed")
  val follows = apply("follows")
  val function_of = apply("function_of")
  val has_administrator = apply("has_administrator")
  val has_container = apply("has_container")
  val has_creator = apply("has_creator")
  val has_discussion = apply("has_discussion")
  val has_function = apply("has_function")
  val has_host = apply("has_host")
  val has_member = apply("has_member")
  val has_moderator = apply("has_moderator")
  val has_modifier = apply("has_modifier")
  val has_owner = apply("has_owner")
  val has_parent = apply("has_parent")
  val has_reply = apply("has_reply")
  val has_scope = apply("has_scope")
  val has_space = apply("has_space")
  val has_subscriber = apply("has_subscriber")
  val has_usergroup = apply("has_usergroup")
  val host_of = apply("host_of")
  val id = apply("id")
  val ip_address = apply("ip_address")
  val last_activity_date = apply("last_activity_date")
  val last_item_date = apply("last_item_date")
  val last_reply_date = apply("last_reply_date")
  val later_version = apply("later_version")
  val latest_version = apply("latest_version")
  val link = apply("link")
  val links_to = apply("links_to")
  val member_of = apply("member_of")
  val moderator_of = apply("moderator_of")
  val modifier_of = apply("modifier_of")
  val name = apply("name")
  val next_by_date = apply("next_by_date")
  val next_version = apply("next_version")
  val note = apply("note")
  val num_authors = apply("num_authors")
  val num_items = apply("num_items")
  val num_replies = apply("num_replies")
  val num_threads = apply("num_threads")
  val num_views = apply("num_views")
  val owner_of = apply("owner_of")
  val parent_of = apply("parent_of")
  val previous_by_date = apply("previous_by_date")
  val previous_version = apply("previous_version")
  val related_to = apply("related_to")
  val reply_of = apply("reply_of")
  val scope_of = apply("scope_of")
  val sibling = apply("sibling")
  val space_of = apply("space_of")
  val subscriber_of = apply("subscriber_of")
  val topic = apply("topic")
  val usergroup_of = apply("usergroup_of")
  val User = apply("User")
  val title = apply("title")
  val content_encoded = apply("content_encoded")
  val created_at = apply("created_at")
  val description = apply("description")
  val first_name = apply("first_name")
  val group_of = apply("group_of")
  val has_group = apply("has_group")
  val has_part = apply("has_part")
  val last_name = apply("last_name")
  val modified_at = apply("modified_at")
  val part_of = apply("part_of")
  val reference = apply("reference")
  val subject = apply("subject")

}