
crossScalaVersions := Seq("2.10.6", "2.11.7", "2.12.0-M3")

val commonSettings: Seq[Setting[_]] = Seq(
  organization := "be.doeraene",

  version := "0.0.1-SNAPSHOT",

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

val pack = "be.doeraene.sjsreflect"

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
  settings(inConfig(Test)(Seq(
    scalaJSOptimizerOptions ~= { _.withCheckScalaJSIR(true) },

    scalaJSReflectSelectors ++= Seq(
      // ReflectionTest.getClassForName()
      selectSingleClass(s"$pack.ExactGetClassForName") -> reflectClassByName(),
      selectDescendentClasses(s"$pack.GetClassForNameAncestor") -> reflectClassByName(),

      // ReflectionTest.getDeclaredConstructors()
      selectSingleClass(s"$pack.ExactGetDeclaredConstructors") -> reflectDeclaredConstructors(),
      selectDescendentClasses(s"$pack.GetDeclaredConstructorsAncestor") -> reflectDeclaredConstructors(),

      // ReflectionTest.loadModule()
      selectSingleClass(s"$pack.ExactAccessModule$$") -> reflectModuleAccessor(),
      selectSingleClass(s"$pack.ExactAccessModuleParent") -> reflectModuleAccessor(),
      selectDescendentClasses(s"$pack.AccessModuleAncestor") -> reflectModuleAccessor(),

      // AkkaLikeReflectionTest.getClassFor()
      selectSingleClass(s"$pack.AkkaGetClassForName") -> reflectClassByName(),

      // AkkaLikeReflectionTest.createInstanceFor()
      selectSingleClass(s"$pack.AkkaSomeConstructible") -> reflectDeclaredConstructors(),

      // AkkaLikeReflectionTest.getObjectFor()
      selectSingleClass(s"$pack.AkkaGetObjectFor$$") -> reflectClassByName(),
      selectSingleClass(s"$pack.AkkaGetObjectFor$$") -> reflectModuleAccessor()
    )
  )))
