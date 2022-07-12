package org.whsv26.playground.kafka

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.{GlobalKTable, JoinWindows}
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream.{KStream, KTable}
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.Properties

object KafkaStreamsApp {
  object Domain {
    type UserId = String
    type Profile = String
    type Product  = String
    type OrderId  = String
    type Status = String

    case class Order(
      orderId: OrderId,
      user: UserId,
      products: List[Product],
      amount: Double
    )

    case class Discount(profile: Profile, amount: Double)
    case class Payment(orderId: OrderId, status: Status)
  }

  object Topics {
    val OrdersByUser = "orders-by-user"
    val DiscountProfilesByUser = "discount-profiles-by-user"
    val Discounts = "discounts"
    val Orders = "orders"
    val Payments = "payments"
    val PaidOrders = "paid-orders"
  }

  import Domain._
  import Topics._

  implicit def serde[A >: Null: Decoder: Encoder]: Serde[A] = {
    // Serde = Серде
    val serializer = (a: A) => a.asJson.noSpaces.getBytes()
    val deserializer = (bytes: Array[Byte]) => {
      val string = new String(bytes)
      decode[A](string).toOption
    }

    Serdes.fromFn[A](serializer, deserializer)
  }

  def main(args: Array[String]): Unit = {
    // topology
    val builder = new StreamsBuilder()

    // KStream - linear stream
    val usersOrdersStream: KStream[UserId, Order] = builder
      .stream[UserId, Order](OrdersByUser)

    // KTable
    val userProfilesTable: KTable[UserId, Profile] = builder
      .table[UserId, Profile](DiscountProfilesByUser)

    // GlobalKTable
    val discountProfilesGTable: GlobalKTable[Profile, Discount] = builder
      .globalTable[Profile, Discount](Discounts)

    val expensiveOrders: KStream[UserId, Order] = usersOrdersStream.filter { (userId, order) =>
      order.amount > 1000
    }

    val listOfProducts: KStream[UserId, List[Product]] = usersOrdersStream.mapValues { order =>
      order.products
    }

    val productsStream: KStream[UserId, Product] = usersOrdersStream.flatMapValues(_.products)

    // join
    val ordersWithUserProfiles: KStream[UserId, (Order, Profile)] = usersOrdersStream
      .join(userProfilesTable) { (order, profile) =>
        (order, profile)
      }

    val discountedOrdersStream: KStream[UserId, Order] = ordersWithUserProfiles
      .join(discountProfilesGTable)(
        { case (userId, (order, profile)) => profile },
        { case ((order, profile), discount) => order.copy(amount = order.amount - discount.amount) }
      )

    val ordersStream: KStream[OrderId, Order] = discountedOrdersStream
      .selectKey((userId, order) => order.orderId)

    val paymentsStream: KStream[OrderId, Payment] = builder
      .stream[OrderId, Payment](Payments)

    val joinWindow: JoinWindows = JoinWindows.of(Duration.of(5, ChronoUnit.MINUTES))

    val joinOrdersPayments: (Order, Payment) => Option[Order] = (order, payment) =>
      Option.when(payment.status == "PAID")(order)

    val ordersPaid: KStream[OrderId, Order] = ordersStream
      .join(paymentsStream)(joinOrdersPayments, joinWindow)
      .flatMapValues(_.toList)

    // sink
    ordersPaid.to(PaidOrders) // redirect to another topic
    productsStream.foreach((userId, product) => println(product))

    val topology: Topology = builder.build()

    val props = new Properties()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "orders-application")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass)

    println(topology.describe())

    val application = new KafkaStreams(topology, props)
    application.start()
  }
}
