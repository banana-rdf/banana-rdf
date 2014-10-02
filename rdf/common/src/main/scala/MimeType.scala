package org.w3.banana

object MimeType {
  val paramRegex = """([^=]+)="?([^"]*)"?""".r

  /**
   * a mime type
   * @param mime the string should be in "type/subtype(;param=value)*" format
   */
  def apply(mime: String): Option[MimeType] = {
    val chunks = mime.split(";")
    val tpe = chunks(0).split("/")
    if (tpe.size != 2) None
    else {
      val params = chunks.drop(1).map { paramStr =>
        val paramRegex(name, value) = paramStr
        (name.toLowerCase, value)
      }
      val paramMap = Map(params: _*) - "charset"
      Some(MimeType(tpe(0), tpe(1), paramMap))
    }
  }

}

/**
 * mainType/subType with optional parameters
 */
case class MimeType(val mainType: String, val subType: String, val params: Map[String, String] = Map()) {
  lazy val mime = {
    s"$mainType/$subType" + params.toSeq.map { case (k, v) => s"""$k="$v"""" }.mkString(";", ";", "")
  }
}

object ImageJpegMime extends MimeType("image", "jpeg")
object ImageGifMime extends MimeType("image", "gif")
object ImagePngMime extends MimeType("image", "png")
object RdfTurtleMime extends MimeType("text", "turtle")
object TextHtmlMime extends MimeType("text", "html")

trait MimeExtensions {
  def extension(mime: MimeType): Option[String]
  def mime(extension: String): Option[MimeType]
}

object WellKnownMimeExtensions extends MimeExtensions {
  val mimeExt = collection.immutable.Map(
    ImageJpegMime -> ".jpg",
    ImageGifMime -> ".gif",
    ImagePngMime -> ".png",
    RdfTurtleMime -> ".ttl",
    TextHtmlMime -> ".html"
  )
  val extMime = {
    val content = for {
      (k, v) <- mimeExt.toSeq
    } yield (v, k)
    collection.immutable.Map(content: _*)
  }

  def extension(mime: MimeType) = mimeExt.get(mime)
  def mime(extension: String) = extMime.get(extension)

}

object MediaRange {
  def apply(range: String): MediaRange = {
    if (range == "*/*") return AnyMedia
    MimeType(range) match {
      case None => NoMedia
      case Some(MimeType("*", "*", _)) => AnyMedia
      case Some(MimeType(main, sub, p)) => new MediaRange(main, sub, p)
    }
  }
}

object AnyMedia extends MediaRange("*", "*") {
  override def matches(mime: MimeType) = true
}

object NoMedia extends MediaRange("-", "-") {
  override def matches(mime: MimeType) = false
}

case class MediaRange protected (val range: String, val subRange: String, val params: Map[String, String] = Map()) {
  def matches(mime: MimeType): Boolean =
    (range == mime.mainType) && ((subRange == "*") || (subRange == mime.subType)) && params == mime.params
}
