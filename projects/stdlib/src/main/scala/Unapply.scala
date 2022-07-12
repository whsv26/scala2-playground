package org.whsv26.playground.stdlib

object Unapply extends App {
  class Foo(val x: Int, val y: Int)

  object + {
    def unapply(arg: Foo): Option[(Int, Int)] =
      Some(arg.x -> arg.y)

    def unapply(arg: Int): Option[(Int, Int)] =
      Some(arg / 2 -> arg / 2)
  }

  val res1 = new Foo(4, 2) match {
    case +(x + y, z) => x + y + z
    case (x + y) + z => x + y + z
    case x + y + z => x + y + z

    case +(x, y) => x + y
    case x + y => x + y
  }

  println(res1)

  case class Bar(val x: Int, val y: Int)

  val res2 = Bar(4, 2) match {
    case x Bar y => x / y
  }

  println(res2)
}
