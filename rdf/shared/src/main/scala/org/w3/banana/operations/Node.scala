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

import org.w3.banana.RDF

trait Node[Rdf <: RDF]:

   extension (node: RDF.Node[Rdf])
      // todo: could we do with just the rNode fold?
      def fold[A](
          uriF: RDF.URI[Rdf] => A,
          bnF: RDF.BNode[Rdf] => A,
          litF: RDF.Literal[Rdf] => A
      ): A =
        if node.isURI then uriF(node.asInstanceOf[RDF.URI[Rdf]])
        else if node.isBNode then bnF(node.asInstanceOf[RDF.BNode[Rdf]])
        else if node.isLiteral then litF(node.asInstanceOf[RDF.Literal[Rdf]])
        else // we should never get here, but refactorings could happen...
           throw IllegalArgumentException(
             s"node.fold() received `$node` which is neither a BNode, URI or Literal. Please report."
           )

      def isURI: Boolean
      def isBNode: Boolean
      def isLiteral: Boolean

end Node
