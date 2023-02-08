import org.scalatest.refspec.RefSpec

class CalculatorRefSpec extends RefSpec { // based on reflection
  object `A calculator` {
    // test suite
    val calculator = new Calculator

    def `multiply by zero should be 0`(): Unit = {
      assert(calculator.multiply(65123123, 0) == 0)
      assert(calculator.multiply(-65123123, 0) == 0)
      assert(calculator.multiply(0, 0) == 0)
    }

    def `should throw math error if dividing by 0`(): Unit = {
      assertThrows[ArithmeticException](calculator.divide(12312, 0))
    }
  }
}