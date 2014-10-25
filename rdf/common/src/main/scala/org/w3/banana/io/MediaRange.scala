package org.w3.banana
package io

object MediaRange {

  def apply(range: String): MediaRange = {
    if (range == "*/*") AnyMedia
    else MimeType.parse(range) match {
      case None                         => NoMedia
      case Some(MimeType("*", "*", _))  => AnyMedia
      case Some(MimeType(main, sub, p)) => MediaRange(main, sub, p)
    }
  }

}

case class MediaRange protected (range: String, subRange: String, params: Map[String, String] = Map.empty) {
  def matches(mime: MimeType): Boolean =
    (range == mime.mainType) &&
    (subRange == "*" || subRange == mime.subType) &&
    params == mime.params
}

object AnyMedia extends MediaRange("*", "*") {
  override def matches(mime: MimeType): Boolean = true
}

object NoMedia extends MediaRange("-", "-") {
  override def matches(mime: MimeType): Boolean = false
}

