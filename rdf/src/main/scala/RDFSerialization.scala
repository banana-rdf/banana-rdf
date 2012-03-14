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
