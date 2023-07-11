/*
 *  Copyright (c) 2012 , 2021 W3C Members
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  This program and the accompanying materials are made available under
 *  the W3C Software Notice and Document License (2015-05-13) which is available at
 *  https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document.
 *
 *  SPDX-License-Identifier: W3C-20150513
 */

package org.w3.banana.operations
import io.lemonlabs.uri.AbsoluteUrl
import org.w3.banana.{Ops, RDF}

/** An rNode is like a node except that it could contain relative URLs. (we could allow that it
  * allows literal datatypes with relative URLs too, but there seems to be only very little need for
  * that at present) An rNode is a superset of Node.
  */
trait rNode[Rdf <: RDF](using ops: Ops[Rdf]):
   import ops.given
   import org.w3.banana.RDF

   extension (rnode: RDF.rNode[Rdf])
      def isURI: Boolean
      def isBNode: Boolean
      def isLiteral: Boolean

      def fold[A](
          uriF: RDF.rURI[Rdf] => A,
          bnF: RDF.BNode[Rdf] => A,
          litF: RDF.Literal[Rdf] => A
      ): A =
        if rnode.isURI then uriF(rnode.asInstanceOf[RDF.rURI[Rdf]])
        else if rnode.isBNode then bnF(rnode.asInstanceOf[RDF.BNode[Rdf]])
        else if rnode.isLiteral then litF(rnode.asInstanceOf[RDF.Literal[Rdf]])
        else // we should never get here, but refactorings could happen...
           throw IllegalArgumentException(
             s"node.fold() received `$rnode` which is neither a BNode, URI or Literal. Please report."
           )
      end fold

      /** return the resolved node if it a relative URI or the original URL. Specify in second
        * position is true if a new object was created
        */
      def resolveNodeAgainst(base: AbsoluteUrl): (RDF.Node[Rdf], Boolean) =
        rnode.fold(
          rUri => rUri.resolveAgainst(base),
          bn => (bn, false),
          lit => (lit, false)
        )

end rNode
