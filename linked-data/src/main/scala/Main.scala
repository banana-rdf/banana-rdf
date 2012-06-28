package org.w3.linkeddata

import org.w3.banana._
import org.w3.banana.sesame._
import org.w3.banana.jena._

object Main {

  def main(args: Array[String]): Unit = {

    //    import SesameOperations._
    //    
    //    val ld = new LinkedData(SesameOperations, SesameProjections, SesameRDFUtils, SesameTurtleReader, SesameTurtleWriter)

    import JenaOperations._
    import Jena.diesel._

    val ld = LinkedData.inMemoryImpl[Jena](Jena.diesel, JenaRDFReader.ReaderSelector)
    import ld._

//    val namesLD = for {
//      barack ← goto(URI("http://dbpedia.org/resource/Barack_Obama"))
//      family ← barack.follow(URI("http://dbpedia.org/ontology/child"))
//      members ← family.follow(URI("http://dbpedia.org/property/members"))
//      names ← members.follow(URI("http://dbpedia.org/property/name")).asStrings
//    } yield names
//
//    val names = namesLD.timbl()
//
//    println(names)




    val cert = Prefix("cert", "http://www.w3.org/ns/auth/cert#", JenaOperations)
    val foaf = Prefix("foaf", "http://xmlns.com/foaf/0.1/", JenaOperations)

    val resultLD = for {
      bblfish ← goto(URI("http://bblfish.net/people/henry/card#me"))
      person ← bblfish.follow(foaf("knows"))
      name <- person.follow(foaf("firstName"))
    } yield ((person, name))

    val result = resultLD.timbl()

    println(result)

    val foo = for {
      bblfish ← goto(URI("http://bblfish.net/people/henry/card#me"))
      person ← bblfish.follow(foaf("knows"))
    } yield {
      for {
        p <- person
      } {
        val name = person.follow(foaf("firstName")).timbl()
        println(p + " - " + name)
      }
    }

    foo.timbl()


    ld.shutdown()

  }

}
