
package org.w3.rdf

import org.junit.Test
import org.junit.Assert._
import java.io._
import com.hp.hpl.jena.rdf.model._
import org.w3.rdf.jena._

class TransformerTest {
  
  @Test()
  def mytest(): Unit = {
    val file = new File("jena/src/test/resources/card.ttl")
    println(file.getAbsolutePath)
    val model = ModelFactory.createDefaultModel()
    model.getReader("TURTLE").read(model, new FileReader("jena/src/test/resources/card.ttl"), "http://www.w3.org/People/Berners-Lee/card")
    
    // there is an issue with the way we map Plain Literals to Typed Literals, because Jena still makes a difference
    // this extra round trip erases the differences
    val jenaGraph = SimpleToJena.transform(JenaToSimple.transform(JenaModule.Graph.fromJena(model.getGraph)))

    val scalaGraph = JenaToSimple.transform(jenaGraph)

    val jenaGraphAgain: JenaModule.Graph = SimpleToJena.transform(scalaGraph)


    val newModel= ModelFactory.createModelForGraph(jenaGraphAgain.jenaGraph)

//    newModel.getWriter("TURTLE").write(newModel,System.out,null)

    // assertTrue(jenaGraph.jenaGraph isIsomorphicWith jenaGraphAgain.jenaGraph)
    
    import GraphIsomorphismForJenaModel._
    
    assertTrue(isIsomorphicWith(jenaGraph, jenaGraphAgain))
    
  }
  
}

class JenaNTriplesParserStringTest extends NTriplesParserTest(JenaNTriplesStringParser) {
  val isomorphism = GraphIsomorphismForJenaModel
  
  def toF(string: String) = string
}


class JenaNTriplesParserSeqTest extends NTriplesParserTest(JenaNTriplesSeqParser) {
  val isomorphism = GraphIsomorphismForJenaModel
  
  def toF(string: String) = string.toSeq
}
