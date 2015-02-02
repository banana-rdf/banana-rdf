package org.w3.banana.meta

import org.scalatest.{Matchers, WordSpec}
import org.w3.banana.{RDFOps, RDF}
import org.w3.banana.annotations.{Mappable, prop, vocab}
import scala.language.experimental.macros

@vocab(base = "http://example.org")
class Person(first:String,second:String) //annotation of constructor params not yet works
{

  @prop val name:String = first //if no name is provided then vocab + property name is used
  @prop val surname:String = second

  @prop(name = "mutant") var war = "mutable value"

  @prop(name = "http://scala-lang.org/immutable") val vi = "immutable value"

}
