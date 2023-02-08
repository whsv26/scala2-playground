lazy val scalaTestingWithScalaTest = (project in file("."))
  .settings(
    scalaVersion := "2.13.10",
    name := "Scala Testing With ScalaTest",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.15",
      "org.scalatest" %% "scalatest" % "3.2.15" % "test",
    )
  )