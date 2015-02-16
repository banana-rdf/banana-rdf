package org.w3.banana.jena

import org.w3.banana._

object JenaOpsTest extends RDFOpsTest[Jena]

object JenaGraphTest extends GraphTest[Jena]

object JenaMGraphTest extends MGraphTest[Jena]

import org.w3.banana.isomorphism._

object JenaIsomorphismTest extends IsomorphismTest[Jena]

object JenaGraphUnionTest extends GraphUnionTest[Jena]

object JenaPointedGraphTest extends PointedGraphTest[Jena]

import org.w3.banana.diesel._

object JenaDieselGraphConstructTest extends DieselGraphConstructTest[Jena]

object JenaDieselGraphExplorationTest extends DieselGraphExplorationTest[Jena]

object JenaDieselOwlPrimerTest extends DieselOwlPrimerTest[Jena]

import org.w3.banana.binder._

object JenaCommonBindersTest extends CommonBindersTest[Jena]

object JenaRecordBinderTest extends RecordBinderTest[Jena]

object JenaCustomBinderTest extends CustomBindersTest[Jena]

import org.w3.banana.syntax._

object JenaUriSyntaxTest extends UriSyntaxTest[Jena]
