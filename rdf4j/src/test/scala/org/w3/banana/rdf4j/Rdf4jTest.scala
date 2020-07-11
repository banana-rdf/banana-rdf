package org.w3.banana.rdf4j

import org.w3.banana._

class Rdf4jOpsTest extends RDFOpsTest[Rdf4j]

class Rdf4jGraphTest extends GraphTest[Rdf4j]

class Rdf4jMGraphTest extends MGraphTest[Rdf4j]

import org.w3.banana.isomorphism._

class Rdf4jIsomorphismTest extends IsomorphismTest[Rdf4j]

class Rdf4jGraphUnionTest extends GraphUnionTest[Rdf4j]

class Rdf4jPointedGraphTest extends PointedGraphTest[Rdf4j]

import org.w3.banana.diesel._

class Rdf4jDieselGraphConstructTest extends DieselGraphConstructTest[Rdf4j]

class Rdf4jDieselGraphExplorationTest extends DieselGraphExplorationTest[Rdf4j]

class Rdf4jDieselOwlPrimerTest extends DieselOwlPrimerTest[Rdf4j]

import org.w3.banana.binder._

class Rdf4jCommonBindersTest extends CommonBindersTest[Rdf4j]

class Rdf4jRecordBinderTest extends RecordBinderTest[Rdf4j]

class Rdf4jCustomBinderTest extends CustomBindersTest[Rdf4j]

import org.w3.banana.syntax._

class Rdf4jUriSyntaxTest extends UriSyntaxTest[Rdf4j]
