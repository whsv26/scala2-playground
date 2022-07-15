import shapeless._

case class Red()
case class Green()
case class Blue()
type Colors = Red :+: Green :+: Blue :+: CNil

case class One()
case class Two()
case class Three()
type Numbers = One :+: Two :+: Three :+: CNil

type ColorsOrNumbers = Colors :+: Numbers :+: CNil

object PrintColorHandler extends Poly1 {
  implicit def caseRed   = at[Red](_ => println("red"))
  implicit def caseBlue  = at[Blue](_ => println("blue"))
  implicit def caseGreen = at[Green](_ => println("green"))
}

object PrintNumbersHandler extends Poly1 {
  implicit def caseOne   = at[One](_ => println("one"))
  implicit def caseTwo   = at[Two](_ => println("two"))
  implicit def caseThree = at[Three](_ => println("three"))
}

object PrintColorOrNumberHandler extends Poly1 {
  implicit def caseRed   = at[Red](_ => println("red"))
  implicit def caseBlue  = at[Blue](_ => println("blue"))
  implicit def caseGreen = at[Green](_ => println("green"))
  implicit def caseOne   = at[One](_ => println("one"))
  implicit def caseTwo   = at[Two](_ => println("two"))
  implicit def caseThree = at[Three](_ => println("three"))
}

def printColor(sum: Colors) =
  sum.fold(PrintColorHandler)

def printNumbers(sum: Numbers) =
  sum.fold(PrintNumbersHandler)

def printColorOrNumber(sum: ColorsOrNumbers) =
  sum.adjoined.fold(PrintColorOrNumberHandler)
