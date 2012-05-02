package org.w3.rdf.sesame

import org.w3.rdf.diesel._

class SesameDieselGraphConstructTest extends DieselGraphConstructTest[Sesame](SesameOperations, SesameDiesel, SesameGraphIsomorphism)

class SesameDieselGraphExplorationTest extends DieselGraphExplorationTest[Sesame](SesameOperations, SesameDiesel, SesameGraphIsomorphism)
