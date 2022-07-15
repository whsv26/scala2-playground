import cats.data.EitherT
import cats.syntax.applicative._
import cats.{Applicative, Functor, Monad}
import shapeless._
import shapeless.ops.coproduct.Inject

case class Red()
case class Green()
case class Blue()
type Colors = Red :+: Green :+: Blue :+: CNil

case class One()
case class Two()
case class Three()
type Numbers = One :+: Two :+: Three :+: CNil

Inject[Colors, Blue]
Inject[Numbers, Two]
Inject[Numbers, Colors]
Inject[Numbers, Red]