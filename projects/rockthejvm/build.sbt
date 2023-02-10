lazy val scalaTestingWithScalaTest = (project in file("./scalatestingwithscalatest"))
  .settings(
    scalaVersion := "2.13.10",
    name := "Scala Testing With ScalaTest",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.15",
      "org.scalatest" %% "scalatest" % "3.2.15" % "test"
    )
  )

lazy val zioStreams = (project in file("./ziostreams"))
  .settings(
    scalaVersion := "2.13.10",
    name := "ZIO Streams",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.7",
      "dev.zio" %% "zio-streams" % "2.0.7",
      "dev.zio" %% "zio-json" % "0.4.2"
    )
  )

lazy val zioKafka = (project in file("./ziokafka"))
  .settings(
    scalaVersion := "2.13.10",
    name := "ZIO Streams",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.7",
      "dev.zio" %% "zio-kafka" % "2.0.7",
      "dev.zio" %% "zio-json" % "0.4.2",
      "org.slf4j" % "slf4j-api" % "2.0.5",
      "ch.qos.logback" % "logback-classic" % "1.4.5"
    )
  )

lazy val taglessFinal = (project in file("./taglessfinal"))
  .settings(
    scalaVersion := "2.13.10",
    name := "Tagless Final in Scala"
  )

lazy val rockTheJvm = (project in file("."))
  .aggregate(
    scalaTestingWithScalaTest,
    zioStreams,
    zioKafka,
    taglessFinal
  )
