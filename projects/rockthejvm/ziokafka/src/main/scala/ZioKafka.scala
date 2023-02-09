import zio._
import zio.json._
import zio.kafka.consumer.{CommittableRecord, Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde
import zio.stream.ZStream

object ZioKafka extends ZIOAppDefault {

  val consumerSettings = ConsumerSettings(List("localhost:9092"))
    .withGroupId("updates-consumer")

  // ZManaged = resource

  val scopedConsumer: ZIO[Scope, Throwable, Consumer] = // effectful resource
    Consumer.make(consumerSettings)

  val consumer: ZLayer[Any, Throwable, Consumer] = // effectful DI
    ZLayer.scoped(scopedConsumer)


  // SerDe = Serializer + Deserialized
  // Stream of strings, read from kafka topic
  val footballMatchesStream: ZStream[Any with Consumer, Throwable, CommittableRecord[String, String]] =
    Consumer
      .subscribeAnd(Subscription.topics("updates"))
      .plainStream(Serde.string, Serde.string)

  case class MatchPlayer(name: String, score: Int)

  object MatchPlayer {
    implicit val encoder: JsonEncoder[MatchPlayer] = DeriveJsonEncoder.gen[MatchPlayer]
    implicit val decoder: JsonDecoder[MatchPlayer] = DeriveJsonDecoder.gen[MatchPlayer]
  }

  case class Match(players: Array[MatchPlayer])

  object Match {
    implicit val encoder: JsonEncoder[Match] = DeriveJsonEncoder.gen[Match]
    implicit val decoder: JsonDecoder[Match] = DeriveJsonDecoder.gen[Match]
  }

  val matchSerde: Serde[Any, Match] = Serde.string.inmapM { string =>
    val decoded = string.fromJson[Match].left.map(new RuntimeException(_))
    ZIO.fromEither(decoded)
  } { theMatch =>
    ZIO.succeed(theMatch.toJson)
  }

  // json strings -> kafka -> jsons -> Match instances

  override def run: ZIO[Any, Any, Int] = {
    ZIO.succeed(1)
  }
}