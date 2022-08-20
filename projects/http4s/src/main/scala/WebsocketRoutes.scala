package org.whsv26.playground.http4s

import cats.syntax.applicative._
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

class WebsocketRoutes(
  topic: Topic[IO, OutputMessage],
  builder: WebSocketBuilder2[IO]
) {
  val dsl = new Http4sDsl[IO] {}
  import dsl._

  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "ws" / username => {
        val receive: Pipe[IO, WebSocketFrame, Unit] = { frames =>
          frames
            .evalMapFilter(_.data.decodeUtf8.toOption.pure[IO])
            .filter(frame => frame == "ping")
            .evalMap(_ => topic.publish1(OutputMessage(username, "pong")))
            .map(_ => ())
        }
        val send: fs2.Stream[IO, WebSocketFrame] = {
          val greeting = Stream.eval(IO.pure(s"$username connected!"))
          val randomNums = Stream
            .repeatEval(IO.delay(s"Random number: " + Random.nextInt()))
            .metered(5.second)

          val subscription = topic
            .subscribe(100)
            .filter(_.username == username)
            .map(_.text)

          greeting
            .append(Stream(randomNums, subscription).parJoinUnbounded)
            .map(WebSocketFrame.Text(_))
        }

        builder.build(send, receive)
      }
    }
}

object WebsocketRoutes {
  case class OutputMessage(username: String, text: String)
}
