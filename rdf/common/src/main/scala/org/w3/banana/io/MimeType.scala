package org.w3.banana
package io

object MimeType {

  val paramRegex = """([^=]+)="?([^"]*)"?""".r

  /** Extracts a [[org.w3.banana.io.MimeType]] from a [[java.lang.String]] following the
    * "type/subtype(;param=value)*" format
    */
  def parse(mime: String): Option[MimeType] = {
    val chunks = mime.split(";")
    chunks(0).split("/") match {
      case Array(tp, subTp) =>
        val params = chunks.drop(1).map { case paramRegex(name, value) =>
          (name.toLowerCase, value)
        }
        val paramMap = Map(params: _*) - "charset"
        Some(MimeType(tp, subTp, paramMap))

      case _ => None
    }
  }

  val ImageJpeg = MimeType("image", "jpeg")
  val ImageGif = MimeType("image", "gif")
  val ImagePng = MimeType("image", "png")
  val RdfTurtle = MimeType("text", "turtle")
  val NTriples = MimeType("application", "ntriples")
  val TextHtml = MimeType("text", "html")
  val RdfXml = MimeType("text", "rdf+xml")
  val SparqlQuery = MimeType("application", "sparql-query")

}

/** A Mime Type (mainType/subType) with optional parameters. */
case class MimeType(mainType: String, subType: String, params: Map[String, String] = Map()) {

  lazy val mime: String = {
    val paramString = {
      val ps = params.map { case (k, v) => s"""$k="$v""""}.mkString("", ";", "")
      if (ps.isEmpty) ""
      else ";"+ps
    }
    s"${mainType}/${subType}${paramString}"
  }

}





object WellKnownMimeExtensions {

  val mimeExt = Map(
    MimeType.ImageJpeg -> "jpg",
    MimeType.ImageGif  -> "gif",
    MimeType.ImagePng  -> "png",
    MimeType.RdfTurtle -> "ttl",
    MimeType.RdfXml    -> "rdf",
    MimeType.TextHtml  -> "html"
  )

  val extMime: Map[String, MimeType] = mimeExt.map(_.swap)

  def extension(mime: MimeType): Option[String] = mimeExt.get(mime)

  def mime(extension: String): Option[MimeType] = extMime.get(extension)

}
