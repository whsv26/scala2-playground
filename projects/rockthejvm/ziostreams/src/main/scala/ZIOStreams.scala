import zio._
import zio.stream._
import zio.json._

object ZIOStreams extends ZIOAppDefault {

  // effects
  val aSuccess: ZIO[Any, Nothing, Int] = ZIO.succeed(42)

  // ZStream = "collection" (source) of 0 or more (maybe inf) elements
  val aStream: ZStream[Any, Nothing, Int] = ZStream.fromIterable(1 to 10)
  val intStream: ZStream[Any, Nothing, Int] = ZStream(1, 2, 3, 4, 5, 6, 7, 8)
  val stringStream: ZStream[Any, Nothing, String] = intStream.map(_.toString)

  // sink = destination of your elements
  // terminal operation
  val sum: ZSink[Any, Nothing, Int, Nothing, Int] =
    ZSink.sum[Int]

  val take5: ZSink[Any, Nothing, Int, Int, Chunk[Int]] =
    ZSink.take(5)

  val take5Map: ZSink[Any, Nothing, Int, Int, Chunk[String]] =
    take5.map(chunk => chunk.map(_.toString))

  // leftovers (4th type param of ZSink)
  val take5Leftovers: ZSink[Any, Nothing, Int, Nothing, (Chunk[String], Chunk[Int])] =
    take5Map.collectLeftover

  val take5Ignore: ZSink[Any, Nothing, Int, Nothing, Chunk[Int]] =
    take5.ignoreLeftover

  // contramap
  val take5Strings: ZSink[Any, Nothing, String, Int, Chunk[Int]] =
    take5.contramap(_.toInt)

  // ZStream[String] -> ZSink[Int].contramap(...)
  // ZStream[String].map(...) -> ZSink[Int]

  val zio: ZIO[Any, Nothing, Int] = intStream.run(sum)

  // ZPipeline
  val businessLogic: ZPipeline[Any, Nothing, String, Int] =
    ZPipeline.map(_.toInt)

  val zioV2: ZIO[Any, Nothing, Int] =
    stringStream.via(businessLogic).run(sum)

  // many pipelines
  val filterLogic: ZPipeline[Any, Nothing, Int, Int] =
    ZPipeline.filter(_ % 2 == 0)

  val appLogic: ZPipeline[Any, Nothing, String, Int] =
    businessLogic >>> filterLogic

  val zioV3: ZIO[Any, Nothing, Int] =
    stringStream.via(appLogic).run(sum)

  override def run: ZIO[Any, Nothing, Int] =
    zioV3.debug
}