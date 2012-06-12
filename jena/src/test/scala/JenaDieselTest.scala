package org.w3.banana.jena

import org.w3.banana._

class JenaDieselGraphConstructTest extends DieselGraphConstructTest[Jena](JenaDiesel, JenaGraphIsomorphism)

class JenaDieselGraphExplorationTest extends DieselGraphExplorationTest[Jena](JenaDiesel, JenaGraphIsomorphism)
