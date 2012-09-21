package org.w3.banana.syntax

import org.w3.banana._

trait RDFSyntax[Rdf <: RDF]
    extends syntax.GraphSyntax[Rdf]
    with syntax.NodeSyntax[Rdf]
    with syntax.URISyntax[Rdf]
    with syntax.LiteralSyntax[Rdf]
    with syntax.TypedLiteralSyntax[Rdf]
    with syntax.LangLiteralSyntax[Rdf]
    with syntax.StringSyntax[Rdf]
    with syntax.AnySyntax[Rdf]
