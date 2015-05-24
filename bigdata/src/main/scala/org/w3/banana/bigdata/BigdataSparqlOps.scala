package org.w3.banana.bigdata

/*

class BigdataSparqlOps extends SparqlOps[Bigdata]{

  private val p: QueryParser = new SPARQLParserFactory().getParser()



  override def parseSelect(query: String, prefixes: Seq[Prefix[Bigdata]]): Try[Bigdata#SelectQuery] = ???

  override def solutionIterator(solutions: Bigdata#Solutions): Iterator[Bigdata#Solution] = ???

  override def parseConstruct(query: String, prefixes: Seq[Prefix[Bigdata]]): Try[Bigdata#ConstructQuery] = ???

  override def varnames(solution: Bigdata#Solution): Set[String] = ???

  override def parseQuery(query: String, prefixes: Seq[Prefix[Bigdata]]): Try[Bigdata#Query] = ???

  override def fold[T](query: Bigdata#Query)(select: (Bigdata#SelectQuery) => T, construct: (Bigdata#ConstructQuery) => T, ask: (Bigdata#AskQuery) => T): T = ???

  override def parseAsk(query: String, prefixes: Seq[Prefix[Bigdata]]): Try[Bigdata#AskQuery] = ???

  override def getNode(solution: Bigdata#Solution, v: String): Try[Bigdata#Node] = ???

  override def parseUpdate(query: String, prefixes: Seq[Prefix[Bigdata]]): Try[Bigdata#UpdateQuery] = ???
}
*/
