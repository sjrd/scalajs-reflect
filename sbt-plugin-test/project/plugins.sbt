addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.6")
addSbtPlugin("be.doeraene" % "sbt-scalajs-reflection" % "0.1.2-SNAPSHOT")

sources in Compile +=
  baseDirectory.value.getParentFile.getParentFile / "project/TestSettings.scala"
