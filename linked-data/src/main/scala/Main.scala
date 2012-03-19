package org.w3.linkeddata

import org.w3.rdf._
import org.w3.rdf.sesame._
import org.w3.rdf.jena._

object Main {

  def main(args: Array[String]): Unit = {

    //    import SesameOperations._
    //    
    //    val ld = new LinkedData(SesameOperations, SesameProjections, SesameRDFUtils, SesameTurtleReader, SesameTurtleWriter)

    import JenaOperations._

    val ld = LinkedData.inMemoryImpl(JenaOperations, JenaProjections, JenaRDFUtils, JenaTurtleReader, JenaTurtleWriter)
    import ld._

    val namesLD = for {
      barack: IRI ← goto(IRI("http://dbpedia.org/resource/Barack_Obama"))
      family ← barack.follow(IRI("http://dbpedia.org/ontology/child")).asURIs()
      members ← family.follow(IRI("http://dbpedia.org/property/members")).asURIs()
      names ← members.follow(IRI("http://dbpedia.org/property/name"))
    } yield names

    val names = namesLD.timbl()

    println(names)

    ld.shutdown()

  }

}
