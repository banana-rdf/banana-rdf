package org.w3.rdf.sesame

import org.w3.rdf

import org.openrdf.model.Statement
import org.openrdf.model.impl.GraphImpl

import java.io._
import java.util.Collection

import scala.collection.immutable.List

object TurtleParser extends rdf.TurtleParser(SesameModule) {
  
  import SesameModule._
  
  def read(is: InputStream, base: String): Either[Throwable, Graph] =
    try {
      val turtleParser = new org.openrdf.rio.turtle.TurtleParser()
      var triples = List[Statement]().asInstanceOf[Collection[Statement]]
      var collector = new org.openrdf.rio.helpers.StatementCollector(triples);
      turtleParser.setRDFHandler(collector);
      try {
        turtleParser.parse(is, base);
      } catch {
        case t => Left(t)
      }
      Right(new Graph(new GraphImpl(triples)))
    } catch {
      case t => Left(t)
    }
  
  def read(reader: Reader, base: String): Either[Throwable, Graph] =
    try {
      val turtleParser = new org.openrdf.rio.turtle.TurtleParser()
      var triples = List[Statement]().asInstanceOf[Collection[Statement]]
      var collector = new org.openrdf.rio.helpers.StatementCollector(triples);
      turtleParser.setRDFHandler(collector);
      try {
        turtleParser.parse(reader, base);
      } catch {
        case t => Left(t)
      }
      Right(new Graph(new GraphImpl(triples)))
    } catch {
      case t => Left(t)
    }
  
  
}