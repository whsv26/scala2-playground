lazy val skaffold = (project in file("./skaffold"))
  .settings(
    scalaVersion := "2.13.10",
    name := "Skaffold with Helm",
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

lazy val k8s = (project in file("."))
  .aggregate(
    skaffold,
  )
