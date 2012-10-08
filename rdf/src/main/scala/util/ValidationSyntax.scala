package org.w3.banana.util

import org.w3.banana._

import scala.concurrent._
import scalaz.Validation

class ValidationW[F, S](v: Validation[F, S]) {
  def fv(implicit ec: ExecutionContext): FutureValidation[F, S] = FutureValidation(Future.successful(v))
}

class BananaValidationW[S](v: BananaValidation[S]) extends ValidationW[BananaException, S](v) {
  def bf(implicit ec: ExecutionContext): BananaFuture[S] = fv
  def getOrFail: S = v.fold(be => throw be, s => s)
}

trait ValidationSyntax {
  implicit def validationToValidationSyntax[F, S](v: Validation[F, S]): ValidationW[F, S] = new ValidationW(v)
  implicit def bananaValidationToBananaValidationSyntax[S](v: BananaValidation[S]): BananaValidationW[S] = new BananaValidationW(v)
}

object ValidationSyntax extends ValidationSyntax
