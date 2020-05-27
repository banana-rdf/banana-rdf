package org.w3.banana.jena

import org.w3.banana._

class JenaOpsTest extends RDFOpsTest[Jena]

class JenaGraphTest extends GraphTest[Jena]

class JenaMGraphTest extends MGraphTest[Jena]

import org.w3.banana.isomorphism._

class JenaIsomorphismTest extends IsomorphismTest[Jena]

class JenaGraphUnionTest extends GraphUnionTest[Jena]

class JenaPointedGraphTest extends PointedGraphTest[Jena]

import org.w3.banana.diesel._

class JenaDieselGraphConstructTest extends DieselGraphConstructTest[Jena]

class JenaDieselGraphExplorationTest extends DieselGraphExplorationTest[Jena]

class JenaDieselOwlPrimerTest extends DieselOwlPrimerTest[Jena]

import org.w3.banana.binder._

class JenaCommonBindersTest extends CommonBindersTest[Jena]

class JenaRecordBinderTest extends RecordBinderTest[Jena]

class JenaCustomBinderTest extends CustomBindersTest[Jena]

import org.w3.banana.syntax._

class JenaUriSyntaxTest extends UriSyntaxTest[Jena]
