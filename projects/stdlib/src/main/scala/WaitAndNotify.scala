package org.whsv26.playground.stdlib

import scala.util.Try

object WaitAndNotify {
  def main(args: Array[String]): Unit = {
    val store = new Store
    val producer = new Producer(store)
    val consumer = new Consumer(store)
    new Thread(producer).start()
    new Thread(consumer).start()
  }

  // Класс Магазин, хранящий произведенные товары
  class Store {
    private var product = 0

    def get(): Unit = synchronized {
      while (product <= 0) Try(wait())
      product -= 1
      println("Покупатель купил 1 товар")
      println("Товаров на складе: " + product)
      notify()
    }

    def put(): Unit = synchronized {
      while (product >= 3) Try(wait())
      product += 1
      println("Производитель добавил 1 товар")
      println("Товаров на складе: " + product)
      notify()
    }
  }

  private class Producer(var store: Store) extends Runnable {
    override def run(): Unit = {
      List.fill(6)(store.put())
    }
  }

  private class Consumer(var store: Store) extends Runnable {
    override def run(): Unit = {
      List.fill(6)(store.get())
    }
  }
}
