package org.w3.banana.bigdata.io

import org.w3.banana.bigdata.Bigdata
import org.w3.banana.io._
import scala.util.Try
import org.w3.banana.util.tryInstances._

class BigdataTurtleTests extends TurtleTestSuite[Bigdata, Try]{

  override def testRelative() = {
    println("relative uris are not tested as they are not supported by bigdata")
  }

}

class BigdataRdfXmlTests extends RdfXMLTestSuite[Bigdata,Try]{
  override def testRelative() = {
    println("relative uris are not tested as they are not supported by bigdata")
  }
}

class BigdataNTripleWriterTestSuite extends NTriplesWriterTestSuite[Bigdata]

//HENRY: could you look why it fails?
//class BigdataNTripleReaderTestSuite extends NTriplesReaderTestSuite[Bigdata]

