
val commonSettings: Seq[Setting[_]] = Seq(
  version := "0.0.1-SNAPSHOT",

  scalacOptions ++= Seq(
      "-deprecation", "-feature", "-Xfatal-warnings",
      "-encoding", "utf-8")
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
