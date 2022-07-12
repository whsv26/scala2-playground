package org.whsv26.playground.kubernetes

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.Logger
import org.http4s.{HttpApp, HttpRoutes}
import org.mongodb.scala._
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object Main extends IOApp {

  val appId = System.getenv("APP_ID")
  val dbName = System.getenv("DB_NAME")
  val dbUrl = System.getenv("DB_URL")
  val dbUsername = System.getenv("DB_USERNAME")
  val dbPassword = System.getenv("DB_PASSWORD")

  val dsl = new Http4sDsl[IO] { }
  import dsl._

  val uri: String = s"mongodb://$dbUsername:$dbPassword@$dbUrl"
  System.setProperty("org.mongodb.async.type", "netty")
  val client: MongoClient = MongoClient(uri)
  val db: MongoDatabase = client.getDatabase(dbName)

  db.createCollection("colFoo")

  val helloWorldRoute: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "hello" => {
        def fromFuture[T](f: => Future[T]): IO[T] = IO.fromFuture(IO.delay(f))
        val coll = db.getCollection("colFoo")
        val insert = fromFuture(coll.insertOne(Document("ip" -> appId)).toFuture())
        val find = fromFuture(coll.find().map(_.toJson()).collect.toFuture())

        Ok(insert >> IO.sleep(1.second) >> find.map(_.mkString("\n")))
      }
    }

  override def run(args: List[String]): IO[ExitCode] = {
    val httpApp: HttpApp[IO] = Logger.httpApp(
      logHeaders = false,
      logBody = false
    )(helloWorldRoute.orNotFound)

    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
