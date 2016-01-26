import org.scalajs.core.tools.linker._
import frontend._
import backend._

import org.scalajs.sbtplugin.ScalaJSPluginInternal._

enablePlugins(ScalaJSPlugin)

name := "linking-reflection-for-scalajs"
version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.7"
scalacOptions ++= Seq("-deprecation", "-feature", "-encoding", "utf-8")

scalaJSOptimizerOptions ~= { _.withCheckScalaJSIR(true) }

def linkerSettings(key: TaskKey[Attributed[File]]): Seq[Setting[_]] = Seq(
  scalaJSLinker in key := {
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
      new LinkerPlugin.ReflectionLinker(underlying)
    }

    new ClearableLinker(newLinker, opts.batchMode)
  }
)

inConfig(Compile)(linkerSettings(fastOptJS))
inConfig(Compile)(linkerSettings(fullOptJS))
inConfig(Test)(linkerSettings(fastOptJS))
inConfig(Test)(linkerSettings(fullOptJS))
