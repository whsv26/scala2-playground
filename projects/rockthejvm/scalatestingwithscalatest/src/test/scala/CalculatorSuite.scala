import org.scalatest.funsuite.AnyFunSuite

class CalculatorSuite extends AnyFunSuite {
  val calculator = new Calculator

  test("multiplication by 0 should always be 0") {
    assert(calculator.multiply(65123123, 0) == 0)
    assert(calculator.multiply(-65123123, 0) == 0)
    assert(calculator.multiply(0, 0) == 0)
  }

  test("dividing by 0 should throw some math error") {
    assertThrows[ArithmeticException](calculator.divide(12312, 0))
  }
}
