
crossScalaVersions := Seq("2.10.6", "2.11.7", "2.12.0-M3")

val commonSettings: Seq[Setting[_]] = Seq(
  organization := "be.doeraene",

  version := "0.1.0",

  scalacOptions ++= Seq(
      "-deprecation", "-feature", "-Xfatal-warnings",
      "-encoding", "utf-8"),

  homepage := Some(url("https://github.com/sjrd/scalajs-reflect")),
  licenses += ("BSD 3-Clause", url("http://opensource.org/licenses/BSD-3-Clause")),

  scmInfo := Some(ScmInfo(
      url("https://github.com/sjrd/scalajs-reflect"),
      "scm:git:git@github.com:sjrd/scalajs-reflect.git",
      Some("scm:git:git@github.com:sjrd/scalajs-reflect.git"))),

  publishMavenStyle := true,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },

  pomExtra := (
    <developers>
      <developer>
        <id>sjrd</id>
        <name>Sébastien Doeraene</name>
        <url>https://github.com/sjrd/</url>
      </developer>
    </developers>
  ),

  pomIncludeRepository := { _ => false }
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
  enablePlugins(ScalaJSJUnitPlugin).
  settings(commonSettings: _*).
  settings(
    scalaVersion := "2.11.7"
  ).
  settings(TestSettings.testSettings)
