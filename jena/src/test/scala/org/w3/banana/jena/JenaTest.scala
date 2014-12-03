package org.w3.banana.jena

import org.w3.banana._

object JenaGraphTest extends GraphTest[Jena]

object JenaMGraphTest extends MGraphTest[Jena]

import org.w3.banana.isomorphism._

object JenaIsomorphismTest extends IsomorphismTest[Jena]

object JenaGraphUnionTest extends GraphUnionTest[Jena]

object JenaPointedGraphTest extends PointedGraphTest[Jena]

import org.w3.banana.diesel._

object JenaDieselGraphConstructTest extends DieselGraphConstructTest[Jena]

object JenaDieselGraphExplorationTest extends DieselGraphExplorationTest[Jena]

import org.w3.banana.binder._

class JenaCommonBindersTest extends CommonBindersTest[Jena]

class JenaRecordBinderTest extends RecordBinderTest[Jena]

class JenaCustomBinderTest extends CustomBindersTest[Jena]

import org.w3.banana.syntax._

class JenaUriSyntaxTest extends UriSyntaxTest[Jena]
