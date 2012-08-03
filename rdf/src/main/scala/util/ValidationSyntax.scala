package org.w3.banana.util

import org.w3.banana.BananaException

import akka.dispatch.Promise
import scalaz.Validation

class ValidationW[F, S](v: Validation[F, S]) {
  def fv: FutureValidation[F, S] = FutureValidation(Promise.successful(v))
}

class BananaValidationW[S](v: Validation[BananaException, S]) extends ValidationW[BananaException, S](v) {
  def bf: BananaFuture[S] = fv
  def getOrFail: S = v.fold(be => throw be, s => s)
}

trait ValidationSyntax {
  implicit def validationToValidationSyntax[F, S](v: Validation[F, S]): ValidationW[F, S] = new ValidationW(v)
  implicit def bananaValidationToBananaValidationSyntax[S](v: Validation[BananaException, S]): BananaValidationW[S] = new BananaValidationW(v)
}

object ValidationSyntax extends ValidationSyntax
