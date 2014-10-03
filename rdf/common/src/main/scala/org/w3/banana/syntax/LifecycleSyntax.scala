package org.w3.banana.syntax

import org.w3.banana._

final class LifecycleSyntax[Rdf <: RDF, A] {

  implicit def lifecycleW(a: A) = new LifecycleW[Rdf, A](a)

}

final class LifecycleW[Rdf <: RDF, A](val a: A) extends AnyVal {

  def start()(implicit lifecycle: Lifecycle[Rdf, A]) =
    lifecycle.start(a)

  def stop()(implicit lifecycle: Lifecycle[Rdf, A]) =
    lifecycle.stop(a)

}
