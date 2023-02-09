import zio.Console.printLine
import zio._
import zio.json._
import zio.kafka.consumer.{CommittableRecord, Consumer, ConsumerSettings, OffsetBatch, Subscription}
import zio.kafka.serde.Serde
import zio.stream.{ZSink, ZStream}

// kafka-topics --bootstrap-server localhost:9092 --topic updates --create
// kafka-console-producer --topic updates --broker-list localhost:9092 --property parse.key=true --property key.separator=,
object ZioKafkaConsumer extends ZIOAppDefault {

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

  case class MatchPlayer(name: String, score: Int) {
    override def toString: String = s"$name: $score"
  }

  object MatchPlayer {
    implicit val encoder: JsonEncoder[MatchPlayer] = DeriveJsonEncoder.gen[MatchPlayer]
    implicit val decoder: JsonDecoder[MatchPlayer] = DeriveJsonDecoder.gen[MatchPlayer]
  }

  case class Match(players: Array[MatchPlayer]) {
    def score: String = s"${players(0)} - ${players(1)}"
  }

  object Match {
    implicit val encoder: JsonEncoder[Match] = DeriveJsonEncoder.gen[Match]
    implicit val decoder: JsonDecoder[Match] = DeriveJsonDecoder.gen[Match]
  }

  // json strings -> kafka -> jsons -> Match instances
  val matchSerde: Serde[Any, Match] = Serde.string.inmapM { string =>
    val decoded = string.fromJson[Match].left.map(new RuntimeException(_))
    ZIO.fromEither(decoded)
  } { theMatch =>
    ZIO.succeed(theMatch.toJson)
  }

  val matchesStream: ZStream[Any with Consumer, Throwable, CommittableRecord[String, Match]] =
    Consumer
      .subscribeAnd(Subscription.topics("updates"))
      .plainStream(Serde.string, matchSerde)

  val matchesPrintableStream: ZStream[Any with Consumer, Throwable, OffsetBatch] =
    matchesStream
      .map(cr => (cr.value.score, cr.offset))
      .tap { case (score, _) => printLine(s"| $score |") }
      .map { case (_, offset) => offset }
      .aggregateAsync(Consumer.offsetBatches)

  val streamEffect = matchesPrintableStream.run(ZSink.foreach(_.commit))

  override def run: ZIO[Any, Throwable, Unit] =
    streamEffect.provideSomeLayer(consumer)
}