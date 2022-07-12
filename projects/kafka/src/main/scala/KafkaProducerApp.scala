package org.whsv26.playground.kafka

import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Encoder
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{Serializer, StringSerializer}
import java.util.Properties
import scala.util.Random.nextInt

object KafkaProducerApp extends App {
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

  def serializer[A: Encoder]: Serializer[A] = (topic: String, data: A) => {
    data.asJson.noSpaces.getBytes
  }

  def ordersGenerator: LazyList[Order] = LazyList.continually(())
    .map(_ => Order(
      "Order %s" format nextInt(1000),
      "User %s" format nextInt(1000),
      List("Product %s" format nextInt(1000)),
      nextInt(1000)
    ))

  val props = new Properties()
  props.put(ProducerConfig.ACKS_CONFIG, "all")
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val producer = new KafkaProducer[OrderId, Order](
    props,
    new StringSerializer,
    serializer[Order]
  )

  ordersGenerator
    .map { order =>
      new ProducerRecord[OrderId, Order](OrdersByUser, order.orderId, order)
    }
    .foreach { record =>
      producer.send(record)
      println(s"Order ${record.value()} has been sent")
      Thread.sleep(3000)
    }
}
