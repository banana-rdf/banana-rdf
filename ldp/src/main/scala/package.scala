package org.w3.banana.ldp

import org.w3.banana.plantain._

package object ldp {

//  type Rdf = Plantain
//
//  implicit val ops: RDFOps[Rdf] = PlantainOps
//
//  implicit val diesel: Diesel[Rdf] = PlantainDiesel
//
//  implicit val sparqlOps: SparqlOps[Rdf] = PlantainSparqlOps

  type PlantainScript[+A] = scalaz.Free[({ type l[+x] = LDPCommand[Plantain, x] })#l, A]

}
