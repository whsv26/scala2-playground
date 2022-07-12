package org.whsv26.playground.stdlib

object AuxPattern {
  trait Dep[In] {
    type Out
    val value: Out
  }

  object Dep {
    type Aux[In0, Out0] = Dep[In0] { type Out = Out0 }

    implicit val i1: Dep[Int] { type Out = Double } = new Dep[Int] {
      type Out = Double
      val value: Double = 1.1
    }

    implicit val i2: Dep.Aux[Double, Int] = new Dep[Double] {
      type Out = Int
      val value: Int = 1
    }
  }

  def test1[In](in: In)(implicit D: Dep[In]): D.Out = D.value

  def test2[In, Out](in: In)(implicit D: Dep.Aux[In, Out]): Out = D.value

  // Error! Can't refer to D.Out in the same parameter list.
  // def test3[In](in: In)(implicit D: Dep[In], o: Ordering[D.Out]): D.Out = D.value

  def test4[In, Out](in: In)(implicit D: Dep.Aux[In, Out], o: Ordering[Out]): Out = D.value

  // Technically possible, but much more verbose than aux
  def test5[In, Out0](in: In)(implicit D: Dep[In] { type Out = Out0 }, o: Ordering[Out0]): Out0 = D.value

  val r1: Double = test1(1)
  val r2: Double = test2(1)
  val r3: Int = test1(1.1)
  val r4: Int = test2(1.1)
}
