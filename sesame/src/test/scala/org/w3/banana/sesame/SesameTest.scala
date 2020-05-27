package org.w3.banana.sesame

import org.w3.banana._

class SesameOpsTest extends RDFOpsTest[Sesame]

class SesameGraphTest extends GraphTest[Sesame]

class SesameMGraphTest extends MGraphTest[Sesame]

import org.w3.banana.isomorphism._

class SesameIsomorphismTest extends IsomorphismTest[Sesame]

class SesameGraphUnionTest extends GraphUnionTest[Sesame]

class SesamePointedGraphTest extends PointedGraphTest[Sesame]

import org.w3.banana.diesel._

class SesameDieselGraphConstructTest extends DieselGraphConstructTest[Sesame]

class SesameDieselGraphExplorationTest extends DieselGraphExplorationTest[Sesame]

class SesameDieselOwlPrimerTest extends DieselOwlPrimerTest[Sesame]

import org.w3.banana.binder._

class SesameCommonBindersTest extends CommonBindersTest[Sesame]

class SesameRecordBinderTest extends RecordBinderTest[Sesame]

class SesameCustomBinderTest extends CustomBindersTest[Sesame]

import org.w3.banana.syntax._

class SesameUriSyntaxTest extends UriSyntaxTest[Sesame]
