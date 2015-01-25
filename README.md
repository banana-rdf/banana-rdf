banana-rdf
==========

[![Build Status](https://travis-ci.org/w3c/banana-rdf.svg?branch=master)](https://travis-ci.org/w3c/banana-rdf) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/w3c/banana-rdf?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

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
suite](https://github.com/w3c/banana-rdf/tree/master/rdf-test-suite).

How to start geeking
--------------------

You only need a recent version of Java, that's all:

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

IntelliJ IDEA works with just one global change:

In `~/.sbt/0.13/plugins/build.sbt`

```
    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")
```

To generate IntelliJ project files, just run the command:

``` bash
$ sbt gen-idea
```

Community
=========

For discussions that don't fit in the [issues tracker](https://github.com/w3c/banana-rdf/issues), you may try
either 
*  the [w3c banana-rdf mailing list](http://lists.w3.org/Archives/Public/public-banana-rdf/), for longer discussions
*  the banana-rdf irc channel on freenode using a dedicated IRC client connecting to [irc://irc.freenode.net:6667/banana-rdf](irc://irc.freenode.net:6667/banana-rdf) or using the [freenode html interface](http://webchat.freenode.net), for quick real time socialising

Contributions
-------------

All contributions are welcome. By contributing, you accept to give the
ownership of your contribution to the [World Wide Web
Consortium](http://www.w3.org). They are a _nonprofit organization_
and just want to simplify the governance of this opensource project.

Before contributing please make sure to copy the [bin/pre-commit](bin/pre-commit)
shell script to the `.git/hooks/` directory of your clone.  This will ensure that all 
your commits are formatted in a consistent way before you push, making it easier
to see the real diffs in a project. On Unix you can do this with the command

```
$ cp bin/pre-commit .git/hooks/
```

Code of Conduct
---------------

**Banana-RDF contributors all agree to follow the [W3C Code of Ethics and Professional Conduct](http://www.w3.org/Consortium/cepc/).**

If you want to take action, feel free to contact Alexandre Bertails <alexandre@bertails.org>. You can also contact W3C Staff as explained in [W3C Procedures](http://www.w3.org/Consortium/pwe/#Procedures).

Licence
-------

This source code is made available under the [W3C Licence](http://opensource.org/licenses/W3C). This is a business friendly license.
