package org.w3.banana.util

import org.w3.banana.BananaException

import akka.dispatch.Promise
import scalaz.Validation

class ValidationW[F, S](v: Validation[F, S]) {
  def fv: FutureValidation[F, S] = FutureValidation(Promise.successful(v))
}

trait ValidationSyntax {
  implicit def validationToValidationSyntax[F, S](v: Validation[F, S]): ValidationW[F, S] = new ValidationW(v)
}

object ValidationSyntax extends ValidationSyntax
