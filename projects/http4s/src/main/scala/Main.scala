package org.whsv26.playground.http4s

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{Pipe, Stream}
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.Logger
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import cats.syntax.semigroupk._
import fs2.concurrent.Topic
import org.whsv26.playground.http4s.WebsocketRoutes.OutputMessage

import scala.concurrent.duration.DurationInt
import scala.util.Random

object Main extends IOApp {

  val dsl = new Http4sDsl[IO] { }
  import dsl._

  def helloWorldRoute: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "hello" => Ok.apply("hello there!")
    }

  override def run(args: List[String]): IO[ExitCode] = {
    Topic[IO, OutputMessage].flatMap { topic =>
      BlazeServerBuilder[IO]
        .bindHttp(8080, "0.0.0.0")
        .withHttpWebSocketApp { wsBuilder =>
          val routes = new WebsocketRoutes(topic, wsBuilder).routes <+> helloWorldRoute

          Logger.httpApp(
            logHeaders = false,
            logBody = false
          )(routes.orNotFound)
        }
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    }
  }
}
