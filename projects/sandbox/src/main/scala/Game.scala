package org.whsv26.playground.sandbox

import cats.Monad
import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import fs2._
import fs2.concurrent.Topic

object Game extends IOApp.Simple {

  override def run: IO[Unit] =
    program.flatMap(_.compile.drain)

  def program: IO[Stream[IO, Unit]] =
    for {
      topic <- Topic[IO, GameMessage]
      players = NonEmptyList.of(Player("RED"), Player("YELLOW"))
    } yield {
      Stream.resource(players.traverse(play(topic)))
        .flatMap { interactions =>
          game(players)(topic)
            .concurrently(Stream.emits(interactions.toList).parJoinUnbounded)
        }
    }

  def game(players: NonEmptyList[Player])(topic: Topic[IO, GameMessage]): Stream[IO, Unit] = {
    val printStartMsg =
      IO.println("Start the game!") >>
        IO.println(s"${players.head.name} player makes first move\n")

    val makeFirstMove = Stream.eval(printStartMsg) ++
      Stream.eval(topic.publish1(Move(players.head, Position(0, 0))).void)

    val gameInitialState = Game(players.tail, players.toList, Nil, players.toList.map(_ -> Position(0, 0)).toMap)

    makeFirstMove ++ topic
      .subscribe(1)
      .collect { case event: GameEvent => event }
      .evalMapAccumulate(gameInitialState)(GameFsm.run)
      .evalMap { case (_, command) => topic.publish1(command).void }
  }

  def play(topic: Topic[IO, GameMessage])(player: Player): Resource[IO, Stream[IO, Unit]] =
    topic
      .subscribeAwait(1)
      .map { stream =>
        stream
          .collect {
            case Move(nextPlayer, currentPosition) if nextPlayer == player =>
              currentPosition
          }
          .evalMap(move(topic)(player, _))
      }

  def move(topic: Topic[IO, GameMessage])(player: Player, currentPosition: Position): IO[Unit] =
    for {
      _ <- IO.println(s"${player.name} starting the move: $currentPosition")
      nextPosition <- readPosition
      _ <- IO.println(s"${player.name} made his move: $nextPosition")
      _ <- topic.publish1(PlayerMoved(player, currentPosition, nextPosition))
    } yield ()

  def readPosition: IO[Position] =
    Monad[IO]
      .iterateUntil(IO.readLine)(Position.isValid)
      .map(Position.parse(_).get)

  trait Fsm[F[_], S, I, O] {
    def run: (S, I) => F[(S, O)]
  }

  case object GameFsm extends Fsm[IO, Game, GameEvent, GameCommand] {
    override def run: (Game, GameEvent) => IO[(Game, GameCommand)] = {
      case (Game(next :: tail, players, moves, map), move @ PlayerMoved(player, _, to)) =>
        IO((
          Game(tail, players, move :: moves, map.updated(player, to)),
          Move(next, map(next))
        ))
      case (Game(Nil, next :: tail, moves, map), move @ PlayerMoved(player, _, to)) =>
        IO((
          Game(tail, next :: tail, move :: moves, map.updated(player, to)),
          Move(next, map(next))
        ))
    }
  }

  case class Player(name: String)

  case class Position(x: Int, y: Int)

  object Position {
    def isValid(str: String): Boolean = parse(str).isDefined
    def parse(str: String): Option[Position] = {
      val parts = str.split(',').toList
      val first = parts.headOption.flatMap(_.toIntOption)
      val seconds = parts.tail.headOption.flatMap(_.toIntOption)
      (first, seconds).mapN(Position.apply)
    }
  }

  case class Game(round: List[Player], players: List[Player], moves: List[PlayerMoved], map: Map[Player, Position])

  sealed trait GameMessage

  sealed trait GameEvent extends GameMessage
  case class PlayerMoved(player: Player, from: Position, to: Position) extends GameEvent

  sealed trait GameCommand extends GameMessage
  case class Move(player: Player, at: Position) extends GameCommand

}
