Banana RDF
==========

[![Build Status](https://secure.travis-ci.org/w3c/banana-rdf.png?branch=master)](http://travis-ci.org/un-jon/banana-rdf)

An RDF library in Scala
-----------------------

The goals are:

* to follow **carefully** the [RDF model](http://www.w3.org/TR/rdf11-concepts/#section-Graph-syntax)
* to be fully compatible with existing RDF Java libraries like Jena and Sesame
* to be type-safe
* to provide a simple yet powerful API to manipulate RDF

How to start geeking
--------------------

You only need a recent version of Java, that's all:

    $ git clone git@github.com:w3c/banana-rdf.git
    $ cd banana-rdf
    $ ./sbt


Generate documentation
-------------------------

    $ ./sbt doc
    $ open full/target/scala-2.9.1/api/index.html
    or
    $ open full/target/scala-2.9.1/api.sxr/index.html

Licence
-------

This source code is made available under the [W3C Licence](http://opensource.org/licenses/W3C).
