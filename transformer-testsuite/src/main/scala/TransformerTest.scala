

class TransformerTest[M< Model](m: M) {
  
  import m._
  
  @Test()
  def going_back_and_forth_between_models(): Unit = {
    val file = new File("jena/src/test/resources/card.ttl")
    println(file.getAbsolutePath)
    val model = ModelFactory.createDefaultModel()
    model.getReader("TURTLE").read(model, new FileReader("jena/src/test/resources/card.ttl"), "http://www.w3.org/People/Berners-Lee/card")
    
    val jenaGraph = JenaModel.Graph.fromJena(model.getGraph)
//    println(jenaGraph)
    
    val scalaGraph = JenaToScala.transform(jenaGraph)
    
    val jenaGraphAgain: JenaModel.Graph = ScalaToJena.transform(scalaGraph)
    
//    println(jenaGraphAgain)
    
    // assertTrue(jenaGraph.jenaGraph isIsomorphicWith jenaGraphAgain.jenaGraph)
    assertTrue(GraphIsomorphismForJenaModel.isIsomorphicWith(jenaGraph, jenaGraphAgain))
    
  }
  
}
