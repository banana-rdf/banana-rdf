package org.w3.banana.n3js.io
import java.io.OutputStream

import org.w3.banana.RDFOps
import org.w3.banana.io.{RDFWriter, Turtle}
import org.w3.banana.n3js.{N3Writer, N3js}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSName


class N3jsTurtleWriter extends RDFWriter[N3js, Future, Turtle]
{

  override def write(graph: N3js#Graph, os: OutputStream, base: String): Future[Unit] = {
    Future.successful(Unit)
  }

  override def asString(graph: N3js#Graph, base: String): Future[String] = {

    def onParse(some:Any):Unit = { /*just a mandatory handler*/  }
    val promise = Promise[String]()
    /**
     * Finish callback
     * @param errors
     * @param output string with turtle
     */
    def finishedWriting(errors:Any,output:String):Unit = {
      promise.success(output)
    }

    val writer: N3Writer  = new N3Writer()
    lazy val fun:js.Function1[Any,Unit] = onParse _

    graph.triples.foreach{
      case (sub,pred,obj)=>
        writer.addTriple(sub,pred,obj,fun)
    }

    writer.end(finishedWriting _)
    promise.future
  }
}
