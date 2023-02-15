lazy val skaffold = (project in file("./skaffold"))
  .settings(
    scalaVersion := "2.13.10",
    name := "skaffold",
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

lazy val logging = (project in file("./logging"))
  .settings(
    scalaVersion := "2.13.10",
    name := "logging",
    version := "1.0.0",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % "0.23.18",
      "org.http4s" %% "http4s-blaze-server" % "0.23.13",
      "org.http4s" %% "http4s-blaze-client" % "0.23.13",

      "org.slf4j" % "slf4j-api" % "2.0.5",
      "ch.qos.logback" % "logback-classic" % "1.4.5"
    ),
    assembly / assemblyMergeStrategy := {
      case path if path.endsWith("module-info.class") => MergeStrategy.discard
      case path =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(path)
    },
    docker / buildOptions := BuildOptions(cache = false),
    docker / imageNames := Seq(ImageName(s"localhost:5000/whsv26/${name.value}:latest")),
    docker / dockerfile := {
      val artifact: File = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"
      new Dockerfile {
        from("openjdk:11-jre")
        add(artifact, artifactTargetPath)
        run("groupadd", "--system", "app")
        run("useradd", "--gid", "app", "app")
        user("app")
        entryPoint("java", "-jar", artifactTargetPath)
        expose(8080)
      }
    }
  )
  .enablePlugins(DockerPlugin)

lazy val k8s = (project in file("."))
  .aggregate(
    skaffold,
    logging
  )