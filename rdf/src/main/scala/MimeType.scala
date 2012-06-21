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

case class MimeType(value: String)
