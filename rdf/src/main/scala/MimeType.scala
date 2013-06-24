package org.w3.banana

//All of this was taken from playframework - play.api.http.MediaRange. With small changes related to https://github.com/playframework/Play20/issues/1240

/**
 * Represent a media range as defined by the RFC 2616.
 * @param mediaType Media type (e.g. "audio", "text", etc.)
 * @param mediaSubType Media sub type (e.g. "html", "plain", etc.)
 * @param parameters Media parameters (e.g. "level=1")
 */
case class MediaRange(mediaType: String, mediaSubType: String, parameters: Option[String]) {

  /**
   * @return true if `mimeType` matches this media type, otherwise false
   */
  def accepts(mimeType: String): Boolean = {
    mediaType + "/" + mediaSubType + parameters.map(";" + _).getOrElse("") == mimeType ||
      (mediaSubType == "*" && mediaType == mimeType.takeWhile(_ != '/')) ||
      (mediaType == "*" && mediaSubType == "*")
  }
}

object MediaRange {

  /**
   * Convenient factory method to create a MediaRange from its MIME type followed by the type parameters.
   * {{{
   *   MediaRange("text/html")
   *   MediaRange("text/html;level=1")
   * }}}
   */
  def apply(mediaRange: String) = {
    val mediaType = mediaRange.takeWhile(_ != '/')
    val rhs = mediaRange.dropWhile(_ != '/').tail
    val i = rhs.indexOf(';')
    val (mediaSubType, parameters) =
      if (i > 0) (rhs.take(i), Some(rhs.drop(i + 1)))
      else (rhs, None)
    new MediaRange(mediaType, mediaSubType, parameters)
  }

  implicit val ordering = new Ordering[MediaRange] {
    // “The asterisk "*" character is used to group media types into ranges, with "*/*" indicating all media types
    // and "type/*" indicating all subtypes of that type.”
    // “If more than one media range applies to a given type, the most specific reference has precedence.”
    def compare(a: MediaRange, b: MediaRange) = {
      if (a.mediaType == b.mediaType) {
        if (a.mediaSubType == b.mediaSubType) Ordering[Option[String]].compare(a.parameters, b.parameters)
        else if (a.mediaSubType == "*") -1
        else if (b.mediaSubType == "*") 1
        else 0
      } else if (a.mediaType == "*") -1
      else if (b.mediaType == "*") 1
      else 0
    }
  }

}