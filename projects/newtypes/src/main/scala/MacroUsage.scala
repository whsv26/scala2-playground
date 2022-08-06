package org.whsv26.playground.newtypes

import org.whsv26.playground.macroses.MacroImplementation.{IdExtractor, HasId}

object MacroUsage extends App {

  trait Entity[T] {
    def id(t: T): String
  }

  object Entity {
    def apply[T: Entity]: Entity[T] = implicitly[Entity[T]]

    implicit def derive[T](implicit extractor: IdExtractor[T]): Entity[T] =
      new Entity[T] {
        override def id(t: T): String = extractor.extract(t)
      }
  }

  case class Foo(id: String, x: Int) extends HasId
  case class BarId(value: String)
  case class Bar(id: BarId, y: Int)
  case class Baz(z: Int)

  val foo = Foo("foo id", 1)
  val bar = Bar(BarId("bar id"), 0)
  val baz = Baz(0)

  def extract[T: Entity](t: T): String = Entity[T].id(t)

  val fooId = extract(foo)
  val barId = extract(bar)
//  val bazId = extract(baz)

  println(fooId)
  println(barId)

}
