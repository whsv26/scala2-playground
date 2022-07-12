package org.whsv26.playground.stdlib

object TypeFamily extends App {
  object f1 {
    trait BFamily
    trait SubB extends BFamily

    trait AFamily {
      def doSomething(v: BFamily): Unit
    }

    trait SubA extends AFamily {
      // override def doSomething(v: SubB): Unit
    }
  }

  object f2 {
    trait BFamily
    trait SubB extends BFamily

    trait AFamily[B <: BFamily] {
      def doSomething(v: B): Unit
    }

    trait SubA extends AFamily[SubB] {
      override def doSomething(v: SubB): Unit
    }
  }

  object f3 {
    trait BFamily
    trait SubB extends BFamily

    trait AFamily {
      type B <: BFamily
      def doSomething(v: B): Unit
    }

    trait SubA extends AFamily {
      type B = SubB
      override def doSomething(v: SubB): Unit
    }
  }
}