addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.6")

unmanagedSourceDirectories in Compile +=
  baseDirectory.value.getParentFile / "sbt-scalajs-reflection/src/main/scala"
