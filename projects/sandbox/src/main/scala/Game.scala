package org.whsv26.playground.sandbox

import cats.effect._
import cats.effect.std.Queue
import cats.implicits._
import fs2._
import fs2.concurrent.Topic

import scala.collection.immutable.ListSet
import scala.concurrent.duration.DurationInt

object Game extends IOApp.Simple {

  case class Player(name: String)

  case class Game(round: List[Player], players: List[Player], moves: List[PlayerMoved])

  sealed trait GameEvent
  case class PlayerTurn(player: Player) extends GameEvent
  case class PlayerMoved(player: Player, position: String) extends GameEvent

  def fsm(players: List[Player], first: Player)(topic: Topic[IO, GameEvent]): Stream[IO, (Game, GameEvent)] = {
    val gameEvents = Stream.eval(IO.pure(PlayerTurn(first))) ++ topic.subscribe(1)
    val round = players.dropWhile(_ != first)

    gameEvents
      .evalMapAccumulate(Game(, players, Nil)) {
        case (Game(next :: tail, players, moves), PlayerMoved(position, player)) =>
          IO((
            Game(tail, players, PlayerMoved(position, player) :: moves),
            PlayerTurn(next)
          ))
        case (Game(Nil, next :: tail, moves), PlayerMoved(position, player)) =>
          IO((
            Game(tail, next :: tail, PlayerMoved(position, player) :: moves),
            PlayerTurn(next)
          ))

      }
  }

  override def run: IO[Unit] =
    program.flatMap(_.compile.drain)

  def program: IO[Stream[IO, Unit]] = {

    for {
      topic <- Topic[IO, PlayerMoved]

      redPlayer = Player("RED")
      yellowPlayer = Player("YELLOW")
      _ <- fsm(List(redPlayer, yellowPlayer), redPlayer)



      players = List(
        play(topic, gameState)(redPlayer),
        play(topic, gameState) (yellowPlayer)
      )

    } yield {
      Stream
        .emits(players)
        .covary[IO]
        .parJoinUnbounded
        .concurrently {
          val startTheGame =
            IO.println("Start the game!") >>
              IO.println("RED player makes first move\n") >>
              topic.publish1(PlayerMoved("start position", yellowPlayer)).void

          topic.subscribers.find(_ == players.length).void ++
            Stream.eval(startTheGame)
        }
        .interruptAfter(5.seconds)
    }
  }

  def play(topic: Topic[IO, PlayerMoved], game: Ref[IO, Game])(player: Player): Stream[IO, Unit] = {
    val subscribe = topic
      .subscribeAwait(1)
      .flatMap { subscription =>
        val join = game.update(state => state.copy(players = state.players.incl(player)))
        val leave = game.update(state => state.copy(players = state.players.excl(player)))
        Resource.make(join)(_ => leave).as(subscription)
      }

    Stream
      .resource(subscribe)
      .flatten
      .filter(_.player != player)
      .evalMap(pos =>
        move(topic)(game, player, pos.toString)
      )
  }

  def move(topic: Topic[IO, PlayerMoved])(player: Player, pos: String): IO[Unit] =
    for {
      _ <- IO.println(s"${player.name} starting the move: $pos")
      _ <- IO.sleep(100.millis)
      _ <- IO.println(s"${player.name} made his move: $pos")
      _ <- topic.publish1(PlayerMoved(player, pos))
    } yield ()

}
