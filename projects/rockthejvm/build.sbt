lazy val scalaTestingWithScalaTest = project in file("./scalatestingwithscalatest")

lazy val rockTheJvm = (project in file("."))
  .aggregate(
    scalaTestingWithScalaTest,
  )
