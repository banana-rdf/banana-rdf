banana-rdf
==========

[![Build Status](https://secure.travis-ci.org/w3c/banana-rdf.png)](http://travis-ci.org/w3c/banana-rdf) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/w3c/banana-rdf?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

The current published version 0.8.4 for scala 2.12 is to be found on the [http://bblfish.net/work/repo/releases](http://bblfish.net/work/repo/releases) repository.

```scala
val banana = (name: String) => "org.w3" %% name % "0.8.4" excludeAll (ExclusionRule(organization = "org.scala-stm"))

//add the bblfish-snapshots repository to the resolvers

resolvers += "bblfish-snapshots" at "http://bblfish.net/work/repo/releases"

//choose the packages you need for your dependencies
val bananaDeps = Seq("banana", "banana-rdf", "banana-sesame").map(banana)
```

An RDF library in Scala
-----------------------

`banana-rdf` is a library for RDF, SPARQL and Linked Data technologies
in Scala.

It can be used with existing libraries without any added cost. There
is no wrapping involved: you manipulate directly the real objects. We
currently support Jena, Sesame and Plantain, a pure Scala
implementation.

Features
--------

`banana-rdf` emphasizes type-safety and immutability, so it can come
with some cost when the underlying implementation is very mutable (I'm
looking at you, Jena and Sesame). We try to keep a clear distinction
between the core concepts and the enhanced syntax that Scala can give
us.

[`RDF`](rdf/shared/src/main/scala/org/w3/banana/RDF.scala)
itself is defined as a record of types. Implementations just have to
_plug_ their own types. And because types alone are not enough, we
introduce the
[`RDFOps`](rdf/shared/src/main/scala/org/w3/banana/RDFOps.scala)
typeclass, which defines the mandatory operations that an RDF
implementation must
implement. [`SparqlOps`](rdf/shared/src/main/scala/org/w3/banana/SparqlOps.scala)
does the same for SPARQL.

With `banana-rdf`, you get _Diesel_, a nice DSL to build and navigate
within **pointed graphs** (graphs with a pointer to an inner
node). You also get an abstraction for **graph stores**
([`GraphStore`](rdf/shared/src/main/scala/org/w3/banana/GraphStore.scala)), 
which do not have to be **SPARQL engines**
([`SparqlEngine`](rdf/shared/src/main/scala/org/w3/banana/SparqlEngine.scala)). 
Of course, you can **serialize** and **deserialize**
most of the RDF syntaxes as well as JSON-LD (RDFa will come soon).

`banana-rdf` introduces the concept of **binders**, which let you
bridge the Scala and RDF worlds. Most of the common datastructures are
already available, and you can even map your own classes. Unlike usual
ORM techniques, this does not rely on annotation or reflection.

Until we write thorough documentation, the best place to understand
what you can do is to go through the [test
suite](https://github.com/w3c/banana-rdf/tree/series/0.8.x/rdf-test-suite).

How to start geeking
--------------------

To get going with banana-rdf  and get a feel for how to use it the easiest and
fastest way may well be to use it directly in the Ammonite shell as explained in the
[Scripting with Ammonite wiki page](https://github.com/banana-rdf/banana-rdf/wiki/Scripting-with-Ammonite).

It always helps to have the code available, as there are a lot of useful examples in 
the test suite. You only need a recent version of Java, that's all:

``` bash
$ git clone git@github.com:w3c/banana-rdf.git
$ cd banana-rdf
$ sbt
```

It's also easy to just build specific target platforms:
    
``` bash
$ sbt +banana_js/test    # for javascript only 
$ sbt +banana_jvm/test   # for jvm only
```

( note: scala-js compilation uses more memory. see [travis.yml](.travis.yml) )

IDE Setup
=========

`banana-rdf` works with both [eclipse](https://www.eclipse.org/) and [IntelliJ IDEA](http://www.jetbrains.com/idea/).

global.sbt
----------
Independent of your preferred IDE, optionally the add the following line to `~/.sbt/0.13/global.sbt` to prevent the 
generation of empty source directories:

```
    unmanagedSourceDirectories in Compile ~= { _.filter(_.exists) }
```

Eclipse
-------
Eclipse should work "out of the box" with the addition of the following global settings:

In `~/.sbt/0.13/global.sbt`:

```
    unmanagedSourceDirectories in Compile ~= { _.filter(_.exists) }
```

In `~/.sbt/0.13/plugins/build.sbt`

```
    addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")
```

To generate eclipse project files, just run the command:

``` bash
$ sbt eclipse
```

IntelliJ IDEA
-------------

IntelliJ IDEA works out of the box since 2016.


Community
=========

For discussions that don't fit in the [issues tracker](https://github.com/w3c/banana-rdf/issues), you may try
either 
*  the [w3c banana-rdf mailing list](http://lists.w3.org/Archives/Public/public-banana-rdf/), for longer discussions
*  the [banana-rdf gitter channel](https://gitter.im/banana-rdf/banana-rdf), for quick real time socialising

Code of Conduct
---------------

**Banana-RDF contributors all agree to follow the [W3C Code of Ethics and Professional Conduct](http://www.w3.org/Consortium/cepc/).**

If you want to take action, feel free to contact Alexandre Bertails <alexandre@bertails.org>. You can also contact W3C Staff as explained in [W3C Procedures](http://www.w3.org/Consortium/pwe/#Procedures).

Licence
-------

This source code is made available under the [W3C Licence](http://opensource.org/licenses/W3C). This is a business friendly license.
