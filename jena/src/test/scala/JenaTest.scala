package org.w3.rdf

import org.junit.Test
import org.junit.Assert._
import java.io._
import com.hp.hpl.jena._
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.graph._
import org.w3.rdf.jena._

class TransformerTest {
  
  @Test()
  def mytest(): Unit = {
    val file = new File("jena/src/test/resources/card.ttl")
    println(file.getAbsolutePath)
    val model = ModelFactory.createDefaultModel()
    model.getReader("TURTLE").read(model, new FileReader("jena/src/test/resources/card.ttl"), "http://www.w3.org/People/Berners-Lee/card")
    
    val jenaGraph = JenaModule.Graph.fromJena(model.getGraph)

    val scalaGraph = JenaToScala.transform(jenaGraph)

    val jenaGraphAgain: JenaModule.Graph = ScalaToJena.transform(scalaGraph)


    val newModel= ModelFactory.createModelForGraph(jenaGraphAgain.jenaGraph)

    newModel.getWriter("TURTLE").write(newModel,System.out,null)

    // assertTrue(jenaGraph.jenaGraph isIsomorphicWith jenaGraphAgain.jenaGraph)
    assertTrue(GraphIsomorphismForJenaModel.isIsomorphicWith(jenaGraph, jenaGraphAgain))
    
  }
  
}

class JenaNTriplesParserTest extends NTriplesParserTest[JenaModule.type](JenaModule) {
  val parser = JenaNTriplesParser
  val isomorphism = GraphIsomorphismForJenaModel
}

