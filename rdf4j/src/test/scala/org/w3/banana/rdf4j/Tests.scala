package org.w3.banana.rdf4j

import org.w3.banana.rdf4j.Rdf4j.{R,given}

class Rdf4jGraphTest extends org.w3.banana.GraphTest[R]
class Rdf4jGraphSearchTest extends org.w3.banana.GraphSearchTest[R]

class Rdf4jStoreTest extends org.w3.banana.StoreTest[R]


class Rdf4jPGTest extends org.w3.banana.PGTest[R]

class Rdf4PrefixTest extends org.w3.banana.PrefixTest[R]

class Rdf4TripleTest extends org.w3.banana.TripleTest[R]

class Rdf4jIsomorphismTest extends org.w3.banana.isomorphism.IsomorphismTest[R]