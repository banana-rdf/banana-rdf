package org.w3.banana

import scalaz._

trait Prefix[Rdf <: RDF] {
  def prefixName: String
  def prefixIri: String
  def apply(value: String): Rdf#URI
  def unapply(iri: Rdf#URI): Option[String]
}

object Prefix {
  def apply[Rdf <: RDF](prefixName: String, prefixIri: String)(implicit ops: RDFOperations[Rdf]) =
    new PrefixBuilder(prefixName, prefixIri)(ops)
}

class PrefixBuilder[Rdf <: RDF](val prefixName: String, val prefixIri: String)(implicit ops: RDFOperations[Rdf]) extends Prefix[Rdf] {

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

  def getLocalName(iri: Rdf#URI): Validation[BananaException, String] =
    unapply(iri) match {
      case None => Failure(LocalNameException(this.toString + " couldn't extract localname for " + iri.toString))
      case Some(localname) => Success(localname)
    }

}

object RDFPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOperations[Rdf]) = new RDFPrefix(ops)
}

class RDFPrefix[Rdf <: RDF](ops: RDFOperations[Rdf]) extends PrefixBuilder("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")(ops) {
  val langString = apply("langString")
  val typ = apply("type")
  val first = apply("first")
  val rest = apply("rest")
  val nil = apply("nil")
}

object XSDPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOperations[Rdf]) = new XSDPrefix[Rdf](ops)
}

class XSDPrefix[Rdf <: RDF](ops: RDFOperations[Rdf]) extends PrefixBuilder("xsd", "http://www.w3.org/2001/XMLSchema#")(ops) {
  import ops._

  val string = apply("string")
  val int = apply("int")
  val integer = apply("integer")
  val decimal = apply("decimal")
  val double = apply("double")
  val boolean = apply("boolean")
  val trueLit: Rdf#TypedLiteral = makeTypedLiteral("true", boolean)
  val falseLit: Rdf#TypedLiteral = makeTypedLiteral("false", boolean)
  val dateTime = apply("dateTime")
}

object DCPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOperations[Rdf]) = new DCPrefix(ops)
}

class DCPrefix[Rdf <: RDF](ops: RDFOperations[Rdf]) extends PrefixBuilder("dc", "http://purl.org/dc/elements/1.1/")(ops) {

}

object FOAFPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOperations[Rdf]) = new FOAFPrefix(ops)
}

class FOAFPrefix[Rdf <: RDF](ops: RDFOperations[Rdf]) extends PrefixBuilder("foaf", "http://xmlns.com/foaf/0.1/")(ops) {
  val name = apply("name")
  val title = apply("title")
  val knows = apply("knows")
  val currentProject = apply("currentProject")
  val Person = apply("Person")
  val age = apply("age")
  val height = apply("height")
  val mbox = apply("mbox")
  val publication = apply("publication")
  val wants = apply("wants")
  val author = apply("author")
}
