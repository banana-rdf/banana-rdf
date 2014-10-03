package org.w3.banana.binder

import org.w3.banana._
import org.w3.banana.diesel._
import scala.util._
import java.security.interfaces.RSAPublicKey
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import java.math.BigInteger

class ObjectExamples[Rdf <: RDF]()(implicit ops: RDFOps[Rdf], recordBinder: RecordBinder[Rdf]) {

  import ops._
  import recordBinder._

  val foaf = FOAFPrefix[Rdf]
  val cert = CertPrefix[Rdf]

  case class Person(name: String, nickname: Option[String] = None)

  object Person {

    val clazz = URI("http://example.com/Person#class")
    implicit val classUris = classUrisFor[Person](clazz)

    val name = property[String](foaf.name)
    val nickname = optional[String](foaf("nickname"))
    val address = property[Address](foaf("address"))

    implicit val container = URI("http://example.com/persons/")
    implicit val binder = pgb[Person](name, nickname)(Person.apply, Person.unapply)

  }

  sealed trait Address

  object Address {

    val clazz = URI("http://example.com/Address#class")
    implicit val classUris = classUrisFor[Address](clazz)

    // not sure if this could be made more general, nor if we actually want to do that
    implicit val binder: PGBinder[Rdf, Address] = new PGBinder[Rdf, Address] {
      def fromPG(pointed: PointedGraph[Rdf]): Try[Address] =
        Unknown.binder.fromPG(pointed) orElse VerifiedAddress.binder.fromPG(pointed)

      def toPG(address: Address): PointedGraph[Rdf] = address match {
        case va: VerifiedAddress => VerifiedAddress.binder.toPG(va)
        case Unknown => Unknown.binder.toPG(Unknown)
      }
    }

  }

  case object Unknown extends Address {

    val clazz = URI("http://example.com/Unknown#class")
    implicit val classUris = classUrisFor[Unknown.type](clazz, Address.clazz)

    // there is a question about constants and the classes they live in
    implicit val binder: PGBinder[Rdf, Unknown.type] = constant(this, URI("http://example.com/Unknown#thing")) withClasses classUris

  }

  case class VerifiedAddress(label: String, city: City) extends Address

  object VerifiedAddress {

    val clazz = URI("http://example.com/VerifiedAddress#class")
    implicit val classUris = classUrisFor[VerifiedAddress](clazz, Address.clazz)

    val label = property[String](foaf("label"))
    val city = property[City](foaf("city"))

    implicit val ci = classUrisFor[VerifiedAddress](clazz)

    implicit val binder = pgb[VerifiedAddress](label, city)(VerifiedAddress.apply, VerifiedAddress.unapply) withClasses classUris

  }

  case class City(cityName: String, otherNames: Set[String] = Set.empty)

  object City {

    val clazz = URI("http://example.com/City#class")
    implicit val classUris = classUrisFor[City](clazz)

    val cityName = property[String](foaf("cityName"))
    val otherNames = set[String](foaf("otherNames"))

    implicit val binder: PGBinder[Rdf, City] =
      pgbWithId[City](t => URI("http://example.com/" + t.cityName))
        .apply(cityName, otherNames)(City.apply, City.unapply) withClasses classUris

  }

  case class Me(name: String)

  object Me {
    val clazz = URI("http://example.com/Me#class")
    implicit val classUris = classUrisFor[Me](clazz)

    val name = property[String](foaf.name)

    implicit val binder: PGBinder[Rdf, Me] =
      pgbWithConstId[Me]("http://example.com#me")
        .apply(name)(Me.apply, Me.unapply) withClasses classUris
  }

  object Cert {
    import org.w3.banana.syntax._

    implicit val rsaClassUri = classUrisFor[RSAPublicKey](cert.RSAPublicKey)
    val factory = KeyFactory.getInstance("RSA")
    val exponent = property[BigInteger](cert.exponent)
    val modulus = property[Array[Byte]](cert.modulus)

    implicit val binder: PGBinder[Rdf, RSAPublicKey] =
      pgb[RSAPublicKey](modulus, exponent)(
        (m, e) => factory.generatePublic(new RSAPublicKeySpec(new BigInteger(m), e)).asInstanceOf[RSAPublicKey],
        key => Some((key.getModulus.toByteArray, key.getPublicExponent))
      ) // withClasses rsaClassUri

  }

}
