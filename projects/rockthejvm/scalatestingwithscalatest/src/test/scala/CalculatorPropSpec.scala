import org.scalatest.propspec.AnyPropSpec

// property-style checking
class CalculatorPropSpec extends AnyPropSpec {
  val calculator = new Calculator
  val multiplyByZeroExamples = List((61236, 0), (-61236, 0), (0, 0))

  property("Calculator multiply by zero should be 0") {
    assert(multiplyByZeroExamples.forall {
      case (a, b) => calculator.multiply(a, b) == 0
    })
  }

  property("Calculator divide by zero throw some math error") {
    assertThrows[ArithmeticException](calculator.divide(1, 0))
  }
}