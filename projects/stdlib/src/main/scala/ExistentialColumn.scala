package org.whsv26.playground.stdlib

import cats.Semigroup

object ExistentialColumn extends App {
  trait Column {
    type V
    val semi: Semigroup[V]
    def set(v: V): Unit
    def get(): V
  }

  class StringColumn(value: String) extends Column {
    type V = String
    override val semi = Semigroup[String]
    private var _v = value

    def set(v: String): Unit = _v = v
    def get(): String = _v

    override def toString = "\"" + get() + "\""
  }

  class IntColumn(value: Int) extends Column {
    type V = Int
    override val semi = Semigroup[Int]
    private var _v = value

    def set(v: Int): Unit = _v = v
    def get(): Int = _v

    override def toString = get().toString
  }

  case class Row(cols: List[Column])

  object Row {
    def apply(cols: Column*): Row = new Row(cols.toList)
  }

  val row: Row = Row(
    new StringColumn("col 1"),
    new StringColumn("col 2"),
    new IntColumn(3),
  )

  println(row)

  row.cols.foreach { col =>
    // doubled type is col.V
    // i.e. path dependent type
    val doubled = col.semi.combine(col.get(), col.get())
    col.set(doubled)
  }

  println(row)

  // Row(List(col 1, col 2, 3))
  // Row(List(col 1col 1, col 2col 2, 6))
}