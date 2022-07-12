package org.whsv26.playground.stdlib

import scala.language.implicitConversions

object ExistentialForSome {
  object WithoutUpperBound {
    // Short version
    type Either1[B] = Either[_, B]
    val x1: Either1[Int] = Left("err")
    val x2: Either1[Int] = Left(1)

    // Long version
    type Either2[B] = Either[A, B] forSome { type A }
    val x3: Either2[Int] = Left("err")
    val x4: Either2[Int] = Left(1)
  }

  object WithUpperBound {
    // Short version
    type Either3[B] = Either[_ <: AnyRef, B]
    val x1: Either3[Int] = Left("err")
    // val x2: Either3[Int] = Left(1) // ERROR!

    // Long version
    type Either4[B] = Either[A, B] forSome { type A <: AnyRef }
    val x3: Either4[Int] = Left("err")
    // val x4: Either4[Int] = Left(1) // ERROR!
  }
}
