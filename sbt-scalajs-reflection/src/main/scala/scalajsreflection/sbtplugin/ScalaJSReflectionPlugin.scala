package scalajsreflection.sbtplugin

import sbt._
import sbt.Keys._

import org.scalajs.core.tools.linker._
import frontend._
import backend._

import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPluginInternal._

object ScalaJSReflectionPlugin extends AutoPlugin {
  override def requires: Plugins = ScalaJSPlugin

  object autoImport {

  }

  override def projectSettings: Seq[Setting[_]] = {
    inConfig(Compile)(configSettings) ++
    inConfig(Test)(configSettings)
  }

  lazy val configSettings: Seq[Setting[_]] = {
    reflectionLinkerSettings(fastOptJS) ++
    reflectionLinkerSettings(fullOptJS)
  }

  private def reflectionLinkerSettings(
      key: TaskKey[Attributed[File]]): Seq[Setting[_]] = Seq(

    scalaJSLinker in key := {
      // Mostly copy-pasted from ScalaJSPluginInternal

      val opts = (scalaJSOptimizerOptions in key).value

      val semantics = (scalaJSSemantics in key).value
      val outputMode = (scalaJSOutputMode in key).value
      val withSourceMap = (emitSourceMaps in key).value

      val relSourceMapBase = {
        if ((relativeSourceMaps in key).value)
          Some((artifactPath in key).value.getParentFile.toURI())
        else
          None
      }

      val frontendConfig = LinkerFrontend.Config()
        .withCheckIR(opts.checkScalaJSIR)

      val backendConfig = LinkerBackend.Config()
        .withRelativizeSourceMapBase(relSourceMapBase)
        .withCustomOutputWrapper(scalaJSOutputWrapper.value)
        .withPrettyPrint(opts.prettyPrintFullOptJS)

      val newLinker = { () =>
        val underlying = Linker(semantics, outputMode, withSourceMap,
            opts.disableOptimizer, opts.parallel, opts.useClosureCompiler,
            frontendConfig, backendConfig)
        new ReflectionLinker(underlying)
      }

      new ClearableLinker(newLinker, opts.batchMode)
    }
  )

}
