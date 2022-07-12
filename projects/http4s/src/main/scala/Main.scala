package org.whsv26.playground.http4s

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.Logger

object Main extends IOApp {

  val dsl = new Http4sDsl[IO] { }
  import dsl._

  val helloWorldRoute: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "hello-world" => {
        Ok.apply("123")
      }
    }

  override def run(args: List[String]): IO[ExitCode] = {
    val httpApp: HttpApp[IO] = Logger.httpApp(
      logHeaders = false,
      logBody = false
    )(helloWorldRoute.orNotFound)

    BlazeServerBuilder[IO]
      .bindHttp(8080, "127.0.0.1")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
