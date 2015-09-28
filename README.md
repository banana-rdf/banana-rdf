banana-rdf
==========

[![Build Status](https://secure.travis-ci.org/w3c/banana-rdf.png)](http://travis-ci.org/w3c/banana-rdf) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/w3c/banana-rdf?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

An RDF library in Scala
-----------------------

`banana-rdf` is a library for RDF, SPARQL and Linked Data technologies
in Scala and ScalaJS.

It can be used with existing libraries without any added cost. There
is no wrapping involved: you manipulate directly the real objects. We
currently support Jena, Sesame, N3js and Plantain, a pure Scala
implementation.

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
(`GraphStore`), which do not have to be **SPARQL engines**
(`SparqlEngine`). Of course, you can **serialize** and **deserialize**
most of the RDF syntaxes as well as JSON-LD (RDFa will come soon).

`banana-rdf` introduces the concept of **binders**, which let you
bridge the Scala and RDF worlds. Most of the common datastructures are
already available, and you can even map your own classes. Unlike usual
ORM techniques, this does not rely on annotation or reflection.

Until we write thorough documentation, the best place to understand
what you can do is to go through the [test
suite](https://github.com/w3c/banana-rdf/tree/series/0.8.x/rdf-test-suite) and read [banana-rdf wiki](https://github.com/banana-rdf/banana-rdf/wiki).

Documentation
=============

The documentation is in [banana-rdf wiki](https://github.com/banana-rdf/banana-rdf/wiki)

Community
=========

For discussions that don't fit in the [issues tracker](https://github.com/w3c/banana-rdf/issues), you may try
either 
*  the [w3c banana-rdf mailing list](http://lists.w3.org/Archives/Public/public-banana-rdf/), for longer discussions
*  the banana-rdf irc channel on freenode using a dedicated IRC client connecting to [irc://irc.freenode.net:6667/banana-rdf](irc://irc.freenode.net:6667/banana-rdf) or using the [freenode html interface](http://webchat.freenode.net), for quick real time socialising

Code of Conduct
---------------

**Banana-RDF contributors all agree to follow the [W3C Code of Ethics and Professional Conduct](http://www.w3.org/Consortium/cepc/).**

If you want to take action, feel free to contact Alexandre Bertails <alexandre@bertails.org>. You can also contact W3C Staff as explained in [W3C Procedures](http://www.w3.org/Consortium/pwe/#Procedures).

Licence
-------

This source code is made available under the [W3C Licence](http://opensource.org/licenses/W3C). This is a business friendly license.
