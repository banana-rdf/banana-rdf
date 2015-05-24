package org.w3.banana

import com.inthenow.zcheck.SpecLite

trait SpecLiteExtra { self: SpecLite =>

  class AnyOps2[A](actual: => A) {

    def must_not_==(expected: A): Unit = {
      val act = actual
      def test = expected != act
      def koMessage = "%s == %s".format(act, expected)
      if (!test)
        fail(koMessage)
    }

  }

  implicit def enrichAny2[A](actual: => A): AnyOps2[A] = new AnyOps2(actual)

}
