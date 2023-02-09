import org.apache.kafka.clients.producer.ProducerRecord
import zio.Console.printLine
import zio._
import zio.json._
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.stream.{ZSink, ZStream}

// kafka-topics --bootstrap-server localhost:9092 --topic updates --create
// kafka-console-producer --topic updates --broker-list localhost:9092 --property parse.key=true --property key.separator=,
object ZioKafkaProducer extends ZIOAppDefault {

  val producerSettings = ProducerSettings(List("localhost:9092"))
  val producerResource = Producer.make(producerSettings)
  val producer = ZLayer.scoped(producerResource)

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

  val finalScore = Match(Array(
    MatchPlayer("ITA", 1),
    MatchPlayer("ENG", 2),
  ))

  val record = new ProducerRecord[String, Match]("updates", "update-3", finalScore)

  val producerEffect = Producer.produce(record, Serde.string, matchSerde)

  override def run: URIO[Any, ExitCode] =
    producerEffect
      .provideSomeLayer(producer)
      .exitCode
}