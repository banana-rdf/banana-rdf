
package org.w3.rdf.test

import org.junit.Test
import org.junit.Assert._
import java.io._
import com.hp.hpl.jena.rdf.model._
import org.w3.rdf.jena._
import org.w3.rdf.GraphIsomorphismForJenaModel

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

object JenaNTriplesParserTest extends NTriplesParserTest(JenaModule, JenaNTriplesParser) {
  val isomorphism = GraphIsomorphismForJenaModel
}

