package org.w3.banana

import akka.dispatch._
import akka.actor._
import akka.pattern.ask
import akka.routing._
import akka.util.Timeout

trait AsyncGraphStore[Rdf <: RDF] {

  def addNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): Future[Unit]

  def appendToNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): Future[Unit]

  def getNamedGraph(uri: Rdf#URI): Future[Rdf#Graph]

  def removeGraph(uri: Rdf#URI): Future[Unit]

}
