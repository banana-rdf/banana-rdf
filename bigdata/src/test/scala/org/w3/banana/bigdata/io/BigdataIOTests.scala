package org.w3.banana.bigdata.io

import org.w3.banana.bigdata.Bigdata
import org.w3.banana.io._
import scala.util.Try
import org.w3.banana.util.tryInstances._

class BigdataTurtleTests extends TurtleTestSuite[Bigdata, Try]{

  override def testRelative() = {
    println("relative uris are not tested as they are not supported")
  }
}



//HENRY your help is needed to make it work
//class BigdataNTripleReaderTestSuite extends NTriplesReaderTestSuite[Bigdata]

//class BigdataNTripleWriterTestSuite extends NTriplesWriterTestSuite[Bigdata]
