package org.w3.rdf.jena

import org.w3.rdf.diesel._

class JenaDieselGraphConstructTest extends DieselGraphConstructTest[Jena](JenaOperations, JenaDiesel, JenaGraphIsomorphism)

class JenaDieselGraphExplorationTest extends DieselGraphExplorationTest[Jena](JenaOperations, JenaDiesel, JenaGraphIsomorphism)
