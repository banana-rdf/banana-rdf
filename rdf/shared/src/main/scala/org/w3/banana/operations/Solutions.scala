package org.w3.banana.operations

import org.w3.banana.RDF

trait Solutions[Rdf <: RDF]:
   extension (solutions: RDF.Solutions[Rdf])
     def iterator: Iterator[RDF.Solution[Rdf]]
end Solutions
