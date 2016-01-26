package scalajsreflection.sbtplugin

import org.scalajs.core.tools.sem._
import org.scalajs.core.tools.javascript.ESLevel
import org.scalajs.core.tools.io._
import org.scalajs.core.tools.logging.Logger
import org.scalajs.core.tools.linker._
import org.scalajs.core.tools.linker.analyzer.SymbolRequirement

abstract class IRPatchingLinker(underlying: GenLinker) extends GenLinker {

  final def semantics: Semantics = underlying.semantics
  final def esLevel: ESLevel = underlying.esLevel

  final def linkUnit(irFiles: Seq[VirtualScalaJSIRFile],
      symbolRequirements: SymbolRequirement, logger: Logger): LinkingUnit = {
    underlying.linkUnit(patchIRFilesInternal(irFiles, logger),
        symbolRequirements, logger)
  }

  final def link(irFiles: Seq[VirtualScalaJSIRFile],
      output: WritableVirtualJSFile, logger: Logger): Unit = {
    underlying.link(patchIRFilesInternal(irFiles, logger), output, logger)
  }

  private def patchIRFilesInternal(irFiles: Seq[VirtualScalaJSIRFile],
      logger: Logger): Seq[VirtualScalaJSIRFile] = {
    logger.time("Patching IR") {
      patchIRFiles(irFiles, logger)
    }
  }

  protected def patchIRFiles(irFiles: Seq[VirtualScalaJSIRFile],
      logger: Logger): Seq[VirtualScalaJSIRFile]
}
