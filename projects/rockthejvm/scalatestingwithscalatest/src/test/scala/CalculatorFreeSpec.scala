import org.scalatest.freespec.AnyFreeSpec

class CalculatorFreeSpec extends AnyFreeSpec {
  val calculator = new Calculator

  "A calculator" - { // anything you want
    "give back 0 if multiplying by 0" in {
      assert(calculator.multiply(65123123, 0) == 0)
      assert(calculator.multiply(-65123123, 0) == 0)
      assert(calculator.multiply(0, 0) == 0)
    }

    "throw math error if dividing by 0" in {
      assertThrows[ArithmeticException](calculator.divide(12312, 0))
    }
  }
}