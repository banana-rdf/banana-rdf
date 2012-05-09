package org.w3.banana.jena

import org.w3.banana.diesel._

class JenaDieselGraphConstructTest extends DieselGraphConstructTest[Jena](JenaOperations, JenaDiesel, JenaGraphIsomorphism)

class JenaDieselGraphExplorationTest extends DieselGraphExplorationTest[Jena](JenaOperations, JenaDiesel, JenaGraphIsomorphism)
