ThisBuild / version := "0.1.0-SNAPSHOT"

scalacOptions ++= Seq(
  "-language:experimental.macros",
)

lazy val macroses = (project in file("./projects/macroses"))
  .settings(
    scalaVersion := "2.13.8",
    name := "stdlib",
    idePackagePrefix := Some("org.whsv26.playground.macroses"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.chuusai" %% "shapeless" % "2.3.3",
      "io.estatico" %% "newtype" % "0.4.4",
    ),
    scalacOptions ++= Seq(
      "-language:experimental.macros",
    )
  )

lazy val stdlib = (project in file("./projects/stdlib"))
  .settings(
    scalaVersion := "2.13.8",
    name := "stdlib",
    idePackagePrefix := Some("org.whsv26.playground.stdlib"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.9.0",
    )
  )

lazy val newtypes = (project in file("./projects/newtypes"))
  .dependsOn(macroses)
  .settings(
    scalaVersion := "2.13.8",
    name := "newtypes",
    idePackagePrefix := Some("org.whsv26.playground.newtypes"),
    libraryDependencies ++= Seq(
      "io.estatico" %% "newtype" % "0.4.4",
      "io.circe" %% "circe-core" % "0.14.1",
      "io.circe" %% "circe-generic" % "0.14.1",
      "io.circe" %% "circe-parser" % "0.14.1",
    ),
    scalacOptions ++= Seq(
      "-Ymacro-annotations",
    )
  )

lazy val shapeless = (project in file("./projects/shapeless"))
  .settings(
    scalaVersion := "2.13.8",
    name := "shapeless",
    idePackagePrefix := Some("org.whsv26.playground.shapeless"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.7.0",
      "com.chuusai" %% "shapeless" % "2.3.9"
    )
  )

lazy val kafka = (project in file("./projects/kafka"))
  .settings(
    scalaVersion := "2.13.8",
    name := "kafka",
    idePackagePrefix := Some("org.whsv26.playground.kafka"),
    libraryDependencies ++= Seq(
      "org.apache.kafka" % "kafka-clients" % "2.8.0",
      "org.apache.kafka" % "kafka-streams" % "2.8.0",
      "org.apache.kafka" %% "kafka-streams-scala" % "2.8.0",
      "io.circe" %% "circe-core" % "0.14.1",
      "io.circe" %% "circe-generic" % "0.14.1",
      "io.circe" %% "circe-parser" % "0.14.1",
      "ch.qos.logback"  % "logback-classic" % "1.2.10"
    )
  )

lazy val http4s = (project in file("./projects/http4s"))
  .settings(
    scalaVersion := "2.13.8",
    name := "http4s",
    idePackagePrefix := Some("org.whsv26.playground.http4s"),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % "0.23.10",
      "org.http4s" %% "http4s-blaze-server" % "0.23.10",
      "org.http4s" %% "http4s-blaze-client" % "0.23.10",
      "ch.qos.logback" % "logback-classic" % "1.2.11",
    )
  )

lazy val sandbox = (project in file("./projects/sandbox"))
  .settings(
    scalaVersion := "2.13.10",
    name := "sandbox",
    idePackagePrefix := Some("org.whsv26.playground.sandbox"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.4.5",
      "org.typelevel" %% "mouse" % "1.2.1",
      "co.fs2" %% "fs2-core" % "3.5.0",
    )
  )

lazy val kubernetes = (project in file("./projects/kubernetes"))
  .settings(
    scalaVersion := "2.13.8",
    name := "kubernetes",
    version := "1.0.0-SNAPSHOT",
    idePackagePrefix := Some("org.whsv26.playground.kubernetes"),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % "0.23.10",
      "org.http4s" %% "http4s-blaze-server" % "0.23.10",
      "org.http4s" %% "http4s-blaze-client" % "0.23.10",
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "org.mongodb.scala" %% "mongo-scala-driver" % "4.5.1"
    ),
    docker / buildOptions := BuildOptions(cache = false),
    docker / imageNames := Seq(ImageName(s"whsv26/${name.value}:latest")),
    docker / dockerfile := {
      val artifact: File = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"
      new Dockerfile {
        from("openjdk:11-jre")
        add(artifact, artifactTargetPath)
        entryPoint("java", "-jar", artifactTargetPath)
        expose(8080)
      }
    }
  )
  .enablePlugins(DockerPlugin)
