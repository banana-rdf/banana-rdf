package org.w3.banana.binder

import org.w3.banana._
import org.w3.banana.diesel._

import scala.util._

trait PGBinder[Rdf <: RDF, T] extends FromPG[Rdf, T] with ToPG[Rdf, T] { self =>
  // this is here for convenience
  // do we want to move it somewhere else?
  def withClasses(classUris: ClassUrisFor[Rdf, T])(implicit ops: RDFOps[Rdf]): PGBinder[Rdf, T] = new PGBinder[Rdf, T] {
    // Q: do we want to check the classes here?
    def fromPG(pointed: PointedGraph[Rdf]): Try[T] =
      self.fromPG(pointed)

    def toPG(t: T): PointedGraph[Rdf] = {
      var pointed = self.toPG(t)
      classUris.classes foreach { clazz =>
        pointed = pointed.a(clazz)
      }
      pointed
    }
  }

}

object PGBinder {

  def apply[Rdf <: RDF, T](implicit binder: PGBinder[Rdf, T]): PGBinder[Rdf, T] = binder

  implicit def FromPGToPG2PGBinder[Rdf <: RDF, T](implicit from: FromPG[Rdf, T], to: ToPG[Rdf, T]): PGBinder[Rdf, T] =
    new PGBinder[Rdf, T] {
      def fromPG(pointed: PointedGraph[Rdf]): Try[T] = from.fromPG(pointed)
      def toPG(t: T): PointedGraph[Rdf] = to.toPG(t)
    }

}

