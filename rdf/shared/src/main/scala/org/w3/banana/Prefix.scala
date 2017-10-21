package org.w3.banana

import scala.util._

trait Prefix[Rdf <: RDF] {
  def prefixName: String
  def prefixIri: String
  def apply(value: String): Rdf#URI
  def unapply(iri: Rdf#URI): Option[String]
}

object Prefix {
  def apply[Rdf <: RDF : RDFOps](prefixName: String, prefixIri: String): Prefix[Rdf] =
    new PrefixBuilder(prefixName, prefixIri)
}

class PrefixBuilder[Rdf <: RDF](
  val prefixName: String,
  val prefixIri: String
)(implicit
  ops: RDFOps[Rdf]
) extends Prefix[Rdf] {

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
  def apply[Rdf <: RDF : RDFOps](implicit ops: RDFOps[Rdf]) = new RDFSPrefix(ops)
}

class RDFSPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("rdfs", "http://www.w3.org/2000/01/rdf-schema#")(ops) {
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
  val PlainLiteral = apply("PlainLiteral")
  val Property = apply("Property")
  val Seq = apply("Seq")
  val Statement = apply("Statement")
  val XMLLiteral = apply("XMLLiteral")
  val first = apply("first")
  val langRange = apply("langRange")
  val obj = apply("object")
  val predicate = apply("predicate")
  val rest = apply("rest")
  val subject = apply("subject")
  val `type` = apply("type")
  val value = apply("value")
}

object XSDPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new XSDPrefix[Rdf](ops)
}

class XSDPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("xsd", "http://www.w3.org/2001/XMLSchema#")(ops) {
  import ops._

  // http://www.w3.org/TR/owl-rdf-based-semantics
  // Table 3.3 Datatypes of the OWL 2 RDF-Based Semantics 

  // http://www.w3.org/TR/owl2-syntax/
  // Table 3 Reserved VOcabulary of OWL 2 with Special Treatment

  val anyURI = apply("anyURI")
  val base64Binary = apply("base64Binary")
  val boolean = apply("boolean")
  val `true` = makeLiteral("true", boolean)
  val `false` = makeLiteral("false", boolean)
  val byte = apply("byte")
  val dateTime = apply("dateTime")
  val dateTimeStamp = apply("dateTimeStamp")
  val decimal = apply("decimal")
  val double = apply("double")
  val float = apply("float")
  val hexBinary = apply("hexBinary")
  val int = apply("int")
  val integer = apply("integer")
  val language = apply("language")
  val long = apply("long")
  val maxExclusive = apply("maxExclusive")
  val maxInclusive = apply("maxInclusive")
  val maxLength = apply("maxLength")
  val minExclusive = apply("minExclusive")
  val minInclusive = apply("minInclusive")
  val minLength = apply("minLength")
  val Name = apply("Name")
  val NCName = apply("NCName")
  val negativeInteger = apply("negativeInteger")
  val NMToken = apply("NMToken")
  val nonNegativeInteger = apply("nonNegativeInteger")
  val nonPositiveInteger = apply("nonPositiveInteger")
  val normalizedString = apply("normalizedString")
  val pattern = apply("pattern")
  val PlainLiteral = apply("PlainLiteral")
  val positiveInteger = apply("positiveInteger")
  val short = apply("short")
  val string = apply("string")
  val token = apply("token")
  val unsignedByte = apply("unsignedByte")
  val unsignedInt = apply("unsignedInt")
  val unsignedLong = apply("unsignedLong")
  val unsignedShort = apply("unsignedShort")
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

class LDPPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("ldp", "http://www.w3.org/ns/ldp#")(ops) {
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

object IANALinkPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new IANALinkPrefix(ops)
}

/**
 * The Iana Link Relations are not linked data so these URLs are currently invented ones, and need
 * not reflect what may be used if ever such URIs are coined.
 */
class IANALinkPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("link", "http://www.iana.org/assignments/link-relations/#")(ops) {
  val about = apply("about")
  val acl = apply("acl") //needs to be registered
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
  val meta = apply("meta") //needs to be registered
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

object OWLPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new OWLPrefix(ops)
}

class OWLPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("owl", "http://www.w3.org/2002/07/owl#")(ops) {

  // http://www.w3.org/TR/owl2-rdf-based-semantics/
  // table 3.2: OWL 2 RDF-Based Vocabulary

  val AllDifferent = apply("AllDifferent")
  val AllDisjointClasses = apply("AllDisjointClasses")
  val AllDisjointProperties = apply("AllDisjointProperties")
  val allValuesFrom = apply("allValuesFrom")
  val annotatedProperty = apply("annotatedProperty")
  val annotatedSource = apply("annotatedSource")
  val annotatedTarget = apply("annotatedTarget")
  val Annotation = apply("Annotation")
  val AnnotationProperty = apply("AnnotationProperty")
  val assertionProperty = apply("assertionProperty")
  val AsymmetricProperty = apply("AsymmetricProperty")
  val Axiom = apply("Axiom")
  val backwardCompatibleWith = apply("backwardCompatibleWith")
  val bottomDataProperty = apply("bottomDataProperty")
  val bottomObjectProperty = apply("bottomObjectProperty")
  val cardinality = apply("cardinality")
  val Class = apply("Class")
  val complementOf = apply("complementOf")
  val DataRange = apply("DataRange")
  val datatypeComplementOf = apply("datatypeComplementOf")
  val DatatypeProperty = apply("DatatypeProperty")
  val deprecated = apply("deprecated")
  val DeprecatedClass = apply("DeprecatedClass")
  val DeprecatedProperty = apply("DeprecatedProperty")
  val differentFrom = apply("differentFrom")
  val disjointUnionOf = apply("disjointUnionOf")
  val disjointWith = apply("disjointWith")
  val distinctMembers = apply("distinctMembers")
  val equivalentClass = apply("equivalentClass")
  val equivalentProperty = apply("equivalentProperty")
  val FunctionalProperty = apply("FunctionalProperty")
  val hasKey = apply("hasKey")
  val hasSelf = apply("hasSelf")
  val hasValue = apply("hasValue")
  val imports = apply("imports")
  val incompatibleWith = apply("incompatibleWith")
  val intersectionOf = apply("intersectionOf")
  val InverseFunctionalProperty = apply("InverseFunctionalProperty")
  val inverseOf = apply("inverseOf")
  val IrreflexiveProperty = apply("IrreflexiveProperty")
  val maxCardinality = apply("maxCardinality")
  val maxQualifiedCardinality = apply("maxQualifiedCardinality")
  val members = apply("members")
  val minCardinality = apply("minCardinality")
  val minQualifiedCardinality = apply("minQualifiedCardinality")
  val NamedIndividual = apply("NamedIndividual")
  val NegativePropertyAssertion = apply("NegativePropertyAssertion")
  val Nothing = apply("Nothing")
  val ObjectProperty = apply("ObjectProperty")
  val onClass = apply("onClass")
  val onDataRange = apply("onDataRange")
  val onDatatype = apply("onDatatype")
  val oneOf = apply("oneOf")
  val onProperty = apply("onProperty")
  val onProperties = apply("onProperties")
  val Ontology = apply("Ontology")
  val OntologyProperty = apply("OntologyProperty")
  val priorVersion = apply("priorVersion")
  val propertyChainAxiom = apply("propertyChainAxiom")
  val propertyDisjointWith = apply("propertyDisjointWith")
  val qualifiedCardinality = apply("qualifiedCardinality")
  val ReflexiveProperty = apply("ReflexiveProperty")
  val Restriction = apply("Restriction")
  val sameAs = apply("sameAs")
  val someValuesFrom = apply("someValuesFrom")
  val sourceIndividual = apply("sourceIndividual")
  val SymmetricProperty = apply("SymmetricProperty")
  val targetIndividual = apply("targetIndividual")
  val targetValue = apply("targetValue")
  val Thing = apply("Thing")
  val topDataProperty = apply("topDataProperty")
  val topObjectProperty = apply("topObjectProperty")
  val TransitiveProperty = apply("TransitiveProperty")
  val unionOf = apply("unionOf")
  val versionInfo = apply("versionInfo")
  val versionIRI = apply("versionIRI")
  val withRestrictions = apply("withRestrictions")
}
