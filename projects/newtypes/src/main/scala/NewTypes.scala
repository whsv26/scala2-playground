package org.whsv26.playground.newtypes

import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._

object NewTypes extends App {

  @newtype class Email (val value: String)

  object Email {
    private val validEmail = """^\S+@\S+\.\S+$""".r

    def apply(value: String): Option[Email] =
      Option.when(validEmail.matches(value))(value.coerce)
  }

  println(Email("sad"))
}
