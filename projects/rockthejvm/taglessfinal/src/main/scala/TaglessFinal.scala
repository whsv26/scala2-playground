
object TaglessFinal{
  object ExpressionProblem {
    sealed trait Expr
    case class B(boolean: Boolean) extends Expr
    case class Or(left: Expr, right: Expr) extends Expr
    case class And(left: Expr, right: Expr) extends Expr
    case class Not(expr: Expr) extends Expr

    val aGiantBoolean: Expr = Or(
      And(
        Or(
          B(true),
          B(false)
        ),
        B(false)
      ),
      B(false)
    )

    def eval(expr: Expr) : Boolean = expr match {
      case B(boolean) => boolean
      case Or(left, right) => eval(left) || eval(right)
      case And(left, right) => eval(left) && eval(right)
      case Not(expr) => !eval(expr)
    }

    // include ints
    case class I(int: Int) extends Expr
    case class Sum(left: Expr, right: Expr) extends Expr

    // casts everywhere
    // runtime crashes
    def evalV2(expr: Expr): Any = expr match {
      case B(boolean) => boolean
      case Or(left, right) => evalV2(left).asInstanceOf[Boolean] || evalV2(right).asInstanceOf[Boolean]
      case And(left, right) => evalV2(left).asInstanceOf[Boolean] && evalV2(right).asInstanceOf[Boolean]
      case Not(expr) => !evalV2(expr).asInstanceOf[Boolean]
      case I(int) => int
      case Sum(left, right) => evalV2(left).asInstanceOf[Int] + evalV2(right).asInstanceOf[Int]
    }
  }

  object TaggingSolution {
    sealed abstract class Expr(val tag: String)
    case class B(boolean: Boolean) extends Expr("bool")

    case class Or(left: Expr, right: Expr) extends Expr("bool") {
      assert(left.tag == "bool" && right.tag == "bool")
    }

    case class And(left: Expr, right: Expr) extends Expr("bool")
    case class Not(expr: Expr) extends Expr("bool")
    case class I(int: Int) extends Expr("int")
    case class Sum(left: Expr, right: Expr) extends Expr("int")

    def eval(expr: Expr): Any = expr match {
      case B(boolean) => boolean
      case Or(left, right) =>
        if (left.tag != "bool" || right.tag != "bool")
          throw new IllegalArgumentException("improper argument type")
        else eval(left).asInstanceOf[Boolean] || eval(right).asInstanceOf[Boolean]
      // same
    }
  }

  // compile time check
  // tag is a type parameter
  // requires case class wrappers
  object TaglessInitialSolution {
    sealed trait Expr[A]
    case class B(boolean: Boolean) extends Expr[Boolean]
    case class Or(left: Expr[Boolean], right: Expr[Boolean]) extends Expr[Boolean]
    case class And(left: Expr[Boolean], right: Expr[Boolean]) extends Expr[Boolean]
    case class Not(expr: Expr[Boolean]) extends Expr[Boolean]
    case class I(int: Int) extends Expr[Int]
    case class Sum(left: Expr[Int], right: Expr[Int]) extends Expr[Int]

    def eval[A](expr: Expr[A]): A = expr match {
      case B(boolean) => boolean
      case I(int) => int
      case Or(left, right) => eval(left) || eval(right)
      case And(left, right) => eval(left) && eval(right)
      case Not(expr) => !eval(expr)
      case Sum(left, right) => eval(left) + eval(right)
    }
  }

  def demoTagless(): Unit = {
    import TaglessInitialSolution._
    println(eval(Or(B(true), B(false))))
    println(eval(Sum(I(10), I(1))))
  }

  object TaglessFinalSolution {
    sealed abstract class Expr[A](val value: A)

    def b(boolean: Boolean): Expr[Boolean] = new Expr(boolean) {}
    def i(int: Int): Expr[Int] = new Expr(int) {}
    def not(expr: Expr[Boolean]): Expr[Boolean] = new Expr(!expr.value) {}
    def or(left: Expr[Boolean], right: Expr[Boolean]): Expr[Boolean] = new Expr(left.value || right.value) {}
    def and(left: Expr[Boolean], right: Expr[Boolean]): Expr[Boolean] = new Expr(left.value && right.value) {}
    def sum(left: Expr[Int], right: Expr[Int]): Expr[Int] = new Expr(left.value + right.value) {}

    def eval[A](expr: Expr[A]): A = expr.value
  }

  def demoFinalTagless(): Unit = {
    import TaglessFinalSolution._
    println(eval(or(b(true), b(false))))
    println(eval(sum(i(10), i(1))))
  }

  // F[_]: Monad = "tagless final"

  object TaglessFinalSolutionV2 {
    // Algebra typeclass
    trait Algebra[E[_]] {
      def b(boolean: Boolean): E[Boolean]
      def i(int: Int): E[Int]
      def not(expr: E[Boolean]): E[Boolean]
      def or(left: E[Boolean], right: E[Boolean]): E[Boolean]
      def and(left: E[Boolean], right: E[Boolean]): E[Boolean]
      def sum(left: E[Int], right: E[Int]): E[Int]
    }

    case class SimpleExpr[A](value: A)
    implicit val simpleAlgebra = new Algebra[SimpleExpr] {
      override def b(boolean: Boolean): SimpleExpr[Boolean] =
        SimpleExpr(boolean)

      override def i(int: Int): SimpleExpr[Int] =
        SimpleExpr(int)

      override def not(expr: SimpleExpr[Boolean]): SimpleExpr[Boolean] =
        SimpleExpr(!expr.value)

      override def or(left: SimpleExpr[Boolean], right: SimpleExpr[Boolean]): SimpleExpr[Boolean] =
        SimpleExpr(left.value || right.value)

      override def and(left: SimpleExpr[Boolean], right: SimpleExpr[Boolean]): SimpleExpr[Boolean] =
        SimpleExpr(left.value && right.value)

      override def sum(left: SimpleExpr[Int], right: SimpleExpr[Int]): SimpleExpr[Int] =
        SimpleExpr(left.value + right.value)
    }

    def program1[E[_]](implicit alg: Algebra[E]): E[Boolean] = {
      import alg._
      or(b(true), b(false))
    }

    def program2[E[_]](implicit alg: Algebra[E]): E[Int] = {
      import alg._
      sum(i(10), i(1))
    }
  }

  def demoFinalTaglessV2(): Unit = {
    import TaglessFinalSolutionV2._
    println(program1[SimpleExpr].value)
    println(program2[SimpleExpr].value)
  }

  object TaglessFinalSolutionV3 {
    // Algebra typeclass
    trait UserLogin[E[_]] {
      def checkLogin(mfa: Boolean): E[Boolean]
      def mfaV1(email: E[Boolean], sms: E[Boolean]): E[Boolean]
      def mfaV2(phone: E[Boolean], mobileApp: E[Boolean]): E[Boolean]
      def lastErrorStatus(code: Int): E[Int]
      def totalSessionLogins(server1Logins: E[Int], server2Logins: E[Int]): E[Int]
    }

    case class UserLoginStatus[A](value: A)

    implicit val loginCapability = new UserLogin[UserLoginStatus] {
      override def checkLogin(mfa: Boolean): UserLoginStatus[Boolean] =
        UserLoginStatus(mfa)

      override def lastErrorStatus(code: Int): UserLoginStatus[Int] =
        UserLoginStatus(code)

      override def mfaV1(email: UserLoginStatus[Boolean], sms: UserLoginStatus[Boolean]): UserLoginStatus[Boolean] =
        UserLoginStatus(email.value || sms.value)

      override def mfaV2(phone: UserLoginStatus[Boolean], mobileApp: UserLoginStatus[Boolean]): UserLoginStatus[Boolean] =
        UserLoginStatus(phone.value || mobileApp.value)

      override def totalSessionLogins(server1Logins: UserLoginStatus[Int], server2Logins: UserLoginStatus[Int]): UserLoginStatus[Int] =
        UserLoginStatus(server1Logins.value + server2Logins.value)
    }

    def userLoginFlow[E[_]](implicit alg: UserLogin[E]): E[Boolean] = {
      import alg._
      mfaV1(checkLogin(true), mfaV2(checkLogin(true), checkLogin(false)))
    }

    def checkLastStatus[E[_]](implicit alg: UserLogin[E]): E[Int] = {
      import alg._
      totalSessionLogins(lastErrorStatus(24), lastErrorStatus(-3))
    }
  }

  def main(args: Array[String]): Unit = {
    demoTagless()
    demoFinalTagless()
    demoFinalTaglessV2()
  }
}
