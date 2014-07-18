package org.w3.banana.rdfstorew

import org.w3.banana._

//class RDFStoreWTurtleTest extends TurtleTestSuite[RDFStore]

class RDFStoreWGraphUnionTest extends GraphUnionTest[RDFStore]

class RDFStoreWPointedGraphTest extends PointedGraphTester[RDFStore]

import org.w3.banana.diesel._

class RDFStoreWDieselGraphConstructTest extends DieselGraphConstructTest[RDFStore]

class RDFStoreWDieselGraphExplorationTest extends DieselGraphExplorationTest[RDFStore]

import org.w3.banana.binder._

class RDFStoreWCommonBindersTest extends CommonBindersTest[RDFStore]

class RDFStoreWRecordBinderTest extends RecordBinderTest[RDFStore]

import org.w3.banana.syntax._

class RDFStoreWUriSyntaxTest extends UriSyntaxTest[RDFStore]
