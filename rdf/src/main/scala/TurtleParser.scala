package org.w3.rdf

import java.io._

abstract class TurtleParser[M <: RDFModule](val m: M) {
  
  import m._
  
  def read(is: InputStream, base: String): Graph
  
  def read(reader: Reader, base: String): Graph
  
  def read(file: File, base: String): Graph = {
    val fis = new BufferedInputStream(new FileInputStream(file))
    read(fis, base)
  }
  
  def read(s: String, base: String): Graph = {
    val reader = new StringReader(s)
    read(s, base)
  }
  
}