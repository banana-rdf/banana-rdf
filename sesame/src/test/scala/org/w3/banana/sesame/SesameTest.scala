package org.w3.banana.sesame

import org.w3.banana._

object SesameOpsTest extends RDFOpsTest[Sesame]

object SesameGraphTest extends GraphTest[Sesame]

object SesameMGraphTest extends MGraphTest[Sesame]

import org.w3.banana.io._

object SesameRDFLoaderTestSuite extends RDFLoaderTestSuite with SesameModule

import org.w3.banana.isomorphism._

object SesameIsomorphismTest extends IsomorphismTest[Sesame]

object SesameGraphUnionTest extends GraphUnionTest[Sesame]

object SesamePointedGraphTest extends PointedGraphTest[Sesame]

import org.w3.banana.diesel._

object SesameDieselGraphConstructTest extends DieselGraphConstructTest[Sesame]

object SesameDieselGraphExplorationTest extends DieselGraphExplorationTest[Sesame]

object SesameDieselOwlPrimerTest extends DieselOwlPrimerTest[Sesame]

import org.w3.banana.binder._

object SesameCommonBindersTest extends CommonBindersTest[Sesame]

object SesameRecordBinderTest extends RecordBinderTest[Sesame]

object SesameCustomBinderTest extends CustomBindersTest[Sesame]

import org.w3.banana.syntax._

object SesameUriSyntaxTest extends UriSyntaxTest[Sesame]
