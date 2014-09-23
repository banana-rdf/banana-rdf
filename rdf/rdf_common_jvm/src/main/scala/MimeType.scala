package org.w3.banana

object MimeType {

  /**
   * clean a mime header.
   * This is a  separate function, as many http libraries calculate it for the user
   * @param mimeValue the value in an HTTP Accept or Content-Type header
   * @return the normalised mime string
   */
  def extract(mimeValue: String) = normalize(mimeValue.split(";")(0))

  /**
   * given a mime header this normalises it  to lower case and removes all extra white spaces
   * @param mime a plain mime strong, eg: text/html
   * @return normalised (lowercased and without white spaces) version of the mime
   */
  def normalize(mime: String) = mime.trim.toLowerCase

}

/**
 * a mime type
 * @param mime the string should be in "tpe/subtype" format, but this is not checked
 */
case class MimeType(mime: String) {
  lazy val Array(tpe, subType) = {
    val res = mime.split("/")
    if (res.size != 2) Array("broken", "mime")
    else res
  }
}

object ImageJpegMime extends MimeType("image/jpeg")
object ImageGifMime extends MimeType("image/gif")
object ImagePngMime extends MimeType("image/png")
object RdfTurtleMime extends MimeType("text/turtle")
object TextHtmlMime extends MimeType("text/html")

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
  def apply(range: String) = {
    if (range == "*/*") AnyMedia
    else {
      val res = range.split("/")
      if (res.size != 2) NoMedia
      else if ("*" == res(0)) AnyMedia
      else {
        new MediaRange(res(0), res(1))
      }
    }
  }
}

object AnyMedia extends MediaRange("*", "*") {
  override def matches(mime: MimeType) = true
}

object NoMedia extends MediaRange("-", "-") {
  override def matches(mime: MimeType) = false
}

class MediaRange protected (val range: String, val subRange: String) {
  def matches(mime: MimeType) =
    (range == mime.tpe) && ((subRange == "*") || (subRange == mime.subType))
}
