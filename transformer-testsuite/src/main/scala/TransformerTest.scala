import com.hp.hpl.jena.rdf.model.impl.RDFWriterFImpl
import com.hp.hpl.jena.rdf.model.ModelFactory
import java.io.{FileReader, File}
import org.junit.Test
import org.w3.rdf
import rdf.jena.{JenaModule, ScalaToJena, JenaToScala}
import rdf.{GraphIsomorphismForJenaModel, Module}

class TransformerTest[M<: Module](m: M) {
  
  import m._
  import org.junit.Assert._

  @Test()
  def going_back_and_forth_between_models(): Unit = {
    val file = new File("jena/src/test/resources/card.ttl")
    println(file.getAbsolutePath)
    val model = ModelFactory.createDefaultModel()
    model.getReader("TURTLE").read(model, new FileReader("jena/src/test/resources/card.ttl"), "http://www.w3.org/People/Berners-Lee/card")
    
    val jenaGraph = JenaModule.Graph.fromJena(model.getGraph)
//    println(jenaGraph)
    
    val scalaGraph = JenaToScala.transform(jenaGraph)
    
    val jenaGraphAgain: JenaModule.Graph = ScalaToJena.transform(scalaGraph)

    val newModel= ModelFactory.createModelForGraph(jenaGraphAgain.jenaGraph)

    newModel.getWriter("TURTLE").write(model,System.out,null)
//    println(jenaGraphAgain)
    
    // assertTrue(jenaGraph.jenaGraph isIsomorphicWith jenaGraphAgain.jenaGraph)
    assertTrue(GraphIsomorphismForJenaModel.isIsomorphicWith(jenaGraph, jenaGraphAgain))
    
  }
  
}
