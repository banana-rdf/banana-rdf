Banana RDF
==========

[![Build Status](https://secure.travis-ci.org/w3c/banana-rdf.png?branch=master)](http://travis-ci.org/w3c/banana-rdf)

An RDF library in Scala
-----------------------

`banana-rdf` is a set of libraries for RDF and RDF-related
technologies in Scala, which can be used with existing libraries
without any added cost. There is no wrapping involved: you manipulate
directly the real objects. We currently support Jena, Sesame and
Plantain, a pure Scala implementation.

Features
--------

`banana-rdf` emphasizes type-safety and immutability, so it can come
with some cost when the underlying implementation is very mutable (I'm
looking at you, Jena and Sesame). We try to keep a clear distinction
between the core concepts and the enhanced syntax that Scala can give
us.

[`RDF`](https://github.com/w3c/banana-rdf/blob/master/rdf/src/main/scala/RDF.scala)
itself is defined as a record of types. Implementations just have to
_plug_ their own types. And because types alone are not enough, we
introduce the
[`RDFOps`](https://github.com/w3c/banana-rdf/blob/master/rdf/src/main/scala/RDFOps.scala)
typeclass, which defines the mandatory operations that an RDF
implementation must
implement. [`SparqlOps`](https://github.com/w3c/banana-rdf/blob/master/rdf/src/main/scala/SparqlOps.scala)
does the same for SPARQL.

With `banana-rdf`, you get `Diesel`, a nice DSL to build and navigate
within **pointed graphs** (graphs with a pointer to an inner
node). You also get an abstraction for **graph stores**
(`GraphStore`), which does not have to be **SPARQL engines**
(`SparqlEngine`). Of course, you can serialize and deserialize most of
the RDF syntaxes (RDFa will come soon).

`banana-rdf` introduces the concept of **binders**, which let you
bridge the Scala and RDF worlds. Most of the common datastructures are
already available, and you can even map your own classes. Unlike usual
ORM techniques, this does not rely on annotation or reflection.

How to start geeking
--------------------

You only need a recent version of Java, that's all:

``` bash
$ git clone git@github.com:w3c/banana-rdf.git
$ cd banana-rdf
$ ./sbt
```

Generate documentation
-------------------------

``` bash
$ ./sbt doc
$ open full/target/scala-2.9.1/api/index.html
```

or

``` bash
$ open full/target/scala-2.9.1/api.sxr/index.html
```

Licence
-------

This source code is made available under the [W3C Licence](http://opensource.org/licenses/W3C).
