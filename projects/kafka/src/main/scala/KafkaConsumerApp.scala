package org.whsv26.playground.kafka

import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.jawn.decode
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import java.time.Duration
import java.util.Properties
import scala.jdk.CollectionConverters._

object KafkaConsumerApp extends App {
  object Domain {
    type UserId = String
    type Product  = String
    type OrderId  = String

    case class Order(
      orderId: OrderId,
      user: UserId,
      products: List[Product],
      amount: Double
    )
  }

  object Topics {
    val OrdersByUser = "orders-by-user"
  }

  import Domain._
  import Topics._

  def deserializer[A: Decoder]: Deserializer[A] = (topic: String, data: Array[Byte]) => {
    val str = new String(data)
    decode[A](str).toTry.get
  }

  val props = new Properties()
  props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000")
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  props.put(ConsumerConfig.GROUP_ID_CONFIG, "app-consumer-group-id")

  val consumer = new KafkaConsumer[OrderId, Order](
    props,
    new StringDeserializer,
    deserializer[Order]
  )

  def handle(records: ConsumerRecords[OrderId, Order]): Unit = {
    LazyList.from(records.asScala).foreach { record =>
      println("Message consumed: %s at offset %s" format(record.value(), record.offset()))
    }
  }

  try {
    consumer.subscribe(List(OrdersByUser).asJavaCollection)

    while (true) {
      val records = consumer.poll(Duration.ofSeconds(1))
      handle(records)
    }

  } finally {
    consumer.close()
  }
}
