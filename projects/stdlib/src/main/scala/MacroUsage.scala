package org.whsv26.playground.stdlib

import org.whsv26.playground.macroses.MacroImplementation
import org.whsv26.playground.macroses.MacroImplementation.{HasId, macros}

object MacroUsage extends App {

  trait Entity[T] {
    def id(t: T): String
  }

  object Entity {
    def apply[T: Entity]: Entity[T] = implicitly[Entity[T]]

//    implicit def auto[T]: Entity[T] = new Entity[T] {
//      override def id(t: T): String = macros[T](t)
//    }
  }

  case class Foo(id: String, x: Int) extends HasId
  case class BarId(value: String)
  case class Bar(id: BarId, y: Int)

  new Function[String, String] {
    override def apply(v1: String): String = "asdasd"
  }

  val foo = Foo("foo id", 1)
  val bar = Bar(BarId("bar id"), 0)

  println(macros(foo))
  println(macros(bar))

  def extract[T: Entity](t: T): String = Entity[T].id(t)

//  println(extract(foo))
//  println(extract(bar))

}
