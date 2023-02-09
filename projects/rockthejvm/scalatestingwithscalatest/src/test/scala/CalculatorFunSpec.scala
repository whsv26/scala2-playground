import org.scalatest.funspec.AnyFunSpec

// BDD
class CalculatorFunSpec extends AnyFunSpec {
  val calculator = new Calculator

  describe("multiplication") {
    it("should give back 0 if multiplying by 0") {
      assert(calculator.multiply(65123123, 0) == 0)
      assert(calculator.multiply(-65123123, 0) == 0)
      assert(calculator.multiply(0, 0) == 0)
    }
  }

  describe("division") {
    it("should throw a math error if dividing by 0 ") {
      assertThrows[ArithmeticException](calculator.divide(12312, 0))
    }
  }
}