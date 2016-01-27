import sbt._

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import be.doeraene.sjsreflect.sbtplugin.ScalaJSReflectionPlugin.autoImport._

object TestSettings {
  private final val pack = "be.doeraene.sjsreflect"

  val testSettings = inConfig(Test)(Seq(
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
  ))
}
