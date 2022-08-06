package org.whsv26.playground.newtypes

import io.estatico.newtype.Coercible
import io.estatico.newtype.macros.newtype
import org.whsv26.playground.macroses.MacroImplementation.{HasId, Entity}

object MacroUsage extends App {

  case class Foo(id: String, x: Int) extends HasId
  @newtype case class BarId(value: String)
  case class Bar(id: BarId, y: Int)
  case class BazId(value: String)
  case class Baz(id: BazId, z: Int)

  val foo = Foo("foo id", 1)
  val bar = Bar(BarId("bar id"), 0)
  val baz = Baz(BazId("baz id"), 0)

  def extract[T: Entity](t: T): String = Entity[T].id(t)

  val fooId = extract(foo)
  val barId = extract(bar)
//  val bazId = extract(baz)

  println(fooId)
  println(barId)
//  println(bazId)

}
