package org.w3.banana

/**
 * typeclass for a selector of RDFReader based on the provided mimetype
 */
trait ReaderSelector[Answer] extends (MimeType => Option[BlockingReader[Answer, Any]]) {

  def combineWith(other: ReaderSelector[Answer]): ReaderSelector[Answer] = ReaderSelector2.combine(this, other)

}

/**
 * ReaderSelector2 proposes some helpers to build selectors
 * ( The '2' is there because I had difficulties compiling witohut )
 * */
object ReaderSelector2 {

  def apply[Result, SyntaxType](implicit syntax: Syntax[SyntaxType],
                                         reader: BlockingReader[Result, SyntaxType]): ReaderSelector[Result] =
    new ReaderSelector[Result] {
      def apply(mime: MimeType): Option[BlockingReader[Result, Any]] =
        if (syntax.mimeTypes.list contains mime)
          Some(reader)
        else
          None
    }

  def combine[Result](selector1: ReaderSelector[Result], selector2: ReaderSelector[Result]): ReaderSelector[Result] =
    new ReaderSelector[Result] {
      def apply(mime: MimeType): Option[BlockingReader[Result, Any]] = selector1(mime) orElse selector2(mime)
    }

}
