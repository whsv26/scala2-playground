package org.whsv26.playground.monix

import cats.effect.ExitCode
import monix.eval.{Task, TaskApp}
import monix.execution.Scheduler

object Blocking extends TaskApp {
  def run(args: List[String]): Task[ExitCode] = {
    val io = Scheduler.io()

    for {
      v <- Task.delay(10)

      _ <- Task.shift(io)
      _ <- Task(println(Thread.currentThread.getName + " " + v))
      _ <- Task.shift

      _ <- Task(println(Thread.currentThread.getName))

    } yield ExitCode.Success
  }

}
