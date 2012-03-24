package org.w3.rdf

sealed trait RDFSerialization

trait RDFXML extends RDFSerialization
case object RDFXML extends RDFXML

trait RDFXMLAbbrev extends RDFSerialization
case object RDFXMLAbbrev extends RDFXMLAbbrev

trait Turtle extends RDFSerialization
case object Turtle extends Turtle

trait N3 extends RDFSerialization
case object N3 extends N3

trait RDFa extends RDFSerialization
case object RDFa extends RDFa

trait RDFaXHTML extends RDFSerialization
case object RDFaXHTML extends RDFa

trait RDFaHTML extends RDFSerialization
case object RDFaHTML extends RDFa
