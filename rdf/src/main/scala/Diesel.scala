package org.w3.banana

import scala.util._

object Diesel {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]): Diesel[Rdf] = new Diesel
}

// TODO: split Diesel and remove the implicit val ops
class Diesel[Rdf <: RDF](implicit val ops: RDFOps[Rdf])
    extends CommonBinders[Rdf]
    with ListBinder[Rdf]
    with OptionBinder[Rdf]
    with TupleBinder[Rdf]
    with MapBinder[Rdf]
    with EitherBinder[Rdf]
    with RecordBinder[Rdf]
