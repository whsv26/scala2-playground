import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.Logger
import org.http4s.{HttpApp, HttpRoutes}
import org.slf4j.LoggerFactory

object Main extends IOApp {

  private val logger = LoggerFactory.getLogger("Logger")

  val appId = System.getenv("APP_ID")

  val dsl = new Http4sDsl[IO] { }
  import dsl._

  val helloWorldRoute: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "hello" =>
        val msg = s"[$appId] not implemented hello endpoint"
        IO(logger.error(msg)) >> NotImplemented(msg)
    }

  override def run(args: List[String]): IO[ExitCode] = {
    val httpApp: HttpApp[IO] = Logger.httpApp(
      logHeaders = false,
      logBody = false
    )(helloWorldRoute.orNotFound)


    IO(logger.info(s"App #$appId has been started")) >>
      BlazeServerBuilder[IO]
        .bindHttp(80, "0.0.0.0")
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
  }
}
