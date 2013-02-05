package org.w3.banana.binder

import org.w3.banana._

object `Does it compile?` {

  def foo[Rdf <: RDF](implicit ops: RDFOps[Rdf]): Unit = {

    implicitly[PGBinder[Rdf, PointedGraph[Rdf]]]

    implicitly[PGBinder[Rdf, Rdf#Node]]

    implicitly[NodeBinder[Rdf, Rdf#Node]]

    implicitly[ToNode[Rdf, Rdf#URI]]

    implicitly[URIBinder[Rdf, Rdf#URI]]

    // not sure if this one makes sense...
    implicitly[NodeBinder[Rdf, PointedGraph[Rdf]]]

    implicitly[PGBinder[Rdf, Rdf#URI]]

    implicitly[PGBinder[Rdf, Rdf#Literal]]

    implicitly[LiteralBinder[Rdf, Rdf#Literal]]

  }

  // if there are too many implicitly, got into a compiler bug:
  // At the end of the day, could not inline @inline-marked method implicitly

  def bar[Rdf <: RDF](implicit ops: RDFOps[Rdf]): Unit = {

    implicitly[TypedLiteralBinder[Rdf, Rdf#TypedLiteral]]

    implicitly[PGBinder[Rdf, Rdf#TypedLiteral]]

    implicitly[LangLiteralBinder[Rdf, Rdf#LangLiteral]]

    implicitly[PGBinder[Rdf, Rdf#LangLiteral]]

    implicitly[PGBinder[Rdf, String]]

    implicitly[PGBinder[Rdf, Boolean]]

    implicitly[PGBinder[Rdf, Int]]

    implicitly[PGBinder[Rdf, Double]]

    implicitly[PGBinder[Rdf, org.joda.time.DateTime]]

  }

}
