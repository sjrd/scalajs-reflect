
val commonSettings: Seq[Setting[_]] = Seq(
  version := "0.0.1-SNAPSHOT",

  scalacOptions ++= Seq("-deprecation", "-feature", "-encoding", "utf-8")
)

lazy val `sbt-scalajs-reflection` = project.in(file("sbt-scalajs-reflection")).
  settings(commonSettings: _*).
  settings(
    sbtPlugin := true,
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.6")
  )

lazy val `scalajs-reflection` = project.in(file(".")).
  enablePlugins(ScalaJSPlugin).
  enablePlugins(ScalaJSReflectionPlugin).
  settings(
    scalaVersion := "2.11.7",

    scalaJSOptimizerOptions ~= { _.withCheckScalaJSIR(true) }
  )
