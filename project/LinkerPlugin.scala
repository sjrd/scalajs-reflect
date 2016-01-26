import java.net.URI

import org.scalajs.core.ir
import ir.ClassKind
import ir.Infos._
import ir.Trees._
import ir.Types._

import org.scalajs.core.tools.sem._
import org.scalajs.core.tools.javascript.ESLevel
import org.scalajs.core.tools.io._
import org.scalajs.core.tools.logging.Logger
import org.scalajs.core.tools.linker._
import org.scalajs.core.tools.linker.analyzer.SymbolRequirement

object LinkerPlugin {
  class CustomLinker(underlying: GenLinker) extends GenLinker {
    def semantics: Semantics = underlying.semantics
    def esLevel: ESLevel = underlying.esLevel

    def linkUnit(irFiles: Seq[VirtualScalaJSIRFile],
        symbolRequirements: SymbolRequirement, logger: Logger): LinkingUnit = {
      underlying.linkUnit(patchIRFiles(irFiles), symbolRequirements, logger)
    }

    def link(irFiles: Seq[VirtualScalaJSIRFile],
        output: WritableVirtualJSFile, logger: Logger): Unit = {
      underlying.link(patchIRFiles(irFiles), output, logger)
    }
  }

  private def patchIRFiles(
      irFiles: Seq[VirtualScalaJSIRFile]): Seq[VirtualScalaJSIRFile] = {

    val infos = irFiles.map(_.info).toList
    val infoByName = infos.map(info => info.encodedName -> info).toMap.withDefault(_ => null)

    def implements(cls: ClassInfo, ancestorName: String): Boolean = {
      // TODO Memoize this function?
      cls != null && {
        cls.encodedName == ancestorName ||
        cls.superClass.exists(superName => implements(infoByName(superName), ancestorName)) ||
        cls.interfaces.exists(intfName => implements(infoByName(intfName), ancestorName))
      }
    }

    def erasesToAny(cls: String): Boolean = {
      cls == ir.Definitions.ObjectClass || {
        val info = infoByName(cls)
        info == null || info.kind == ClassKind.RawJSType || info.kind.isJSClass
      }
    }

    val ReflectConstructorsClass =
      ir.Definitions.encodeClassName("linkingreflection.ReflectConstructors")
    val ReflectionClass =
      ir.Definitions.encodeClassName("linkingreflection.Reflection$")
    val ConstructorClass =
      ir.Definitions.encodeClassName("linkingreflection.Reflection$Constructor")

    def listAllCtors()(implicit pos: ir.Position): Tree = {
      def listCtorsOfClass(info: ClassInfo): List[Tree] = {
        for {
          method <- info.methods
          if ir.Definitions.isConstructorName(method.encodedName)
        } yield {
          makeConstructorData(info, method)
        }
      }

      val ctorTrees = for {
        info <- infos
        if info.kind == ClassKind.Class && implements(info, ReflectConstructorsClass)
        ctor <- listCtorsOfClass(info)
      } yield {
        ctor
      }

      JSArrayConstr(ctorTrees)
    }

    def makeConstructorData(cls: ClassInfo, ctor: MethodInfo)(
        implicit pos: ir.Position): Tree = {

      val (_, paramRefTypes, _) = ir.Definitions.decodeMethodName(ctor.encodedName)

      val params =
        for (i <- (1 to paramRefTypes.size).toList)
          yield ParamDef(Ident("arg" + i, None), AnyType, mutable = false, rest = false)

      val actualArgs = for ((param, paramRefType) <- params.zip(paramRefTypes)) yield {
        val paramRef = param.ref

        paramRefType match {
          case ClassType(paramRefCls) =>
            if (ir.Definitions.isPrimitiveClass(paramRefCls)) {
              assert(paramRefCls.length == 1)
              val charCode = paramRefCls.charAt(0)
              if (charCode == 'C') {
                // Good old Char, never doing anything like the others
                Apply(
                    LoadModule(ClassType("sr_BoxesRunTime$")),
                    Ident("unboxToChar__O__C", None),
                    List(paramRef))(IntType)
              } else {
                Unbox(paramRef, charCode)
              }
            } else if (erasesToAny(paramRefCls)) {
              paramRef
            } else {
              AsInstanceOf(paramRef, paramRefType)
            }

          case ArrayType(_, _) =>
            AsInstanceOf(paramRef, paramRefType)
        }
      }

      val classType = ClassType(cls.encodedName)

      New(ClassType(ConstructorClass),
          Ident("init___jl_Class__sjs_js_Array__sjs_js_Dynamic", None),
          List(
              ClassOf(classType),
              JSArrayConstr(paramRefTypes.map(ClassOf(_))),
              Closure(
                  Nil,
                  params,
                  New(classType,
                      Ident(ctor.encodedName, None),
                      actualArgs),
                  Nil)))
    }

    def transformReflectionClass(
        irFile: VirtualScalaJSIRFile): VirtualScalaJSIRFile = {
      val classDef = irFile.tree

      val newDefs = classDef.defs.map {
        case m: MethodDef if m.name.name == "listAllCtors__sjs_js_Array" =>
          implicit val pos = m.pos
          val newDef = m.copy(body = listAllCtors())(m.optimizerHints, None)
          ir.Hashers.hashMethodDef(newDef)

        case m =>
          m
      }

      val newClassDef =
        classDef.copy(defs = newDefs)(classDef.optimizerHints)(classDef.pos)
      val newInfo =
        generateClassInfo(newClassDef)

      val stdout = new java.io.PrintWriter(System.out)
      new ir.Printers.IRTreePrinter(stdout).printTopLevelTree(newClassDef)
      stdout.flush()

      new PatchedMemVirtualScalaJSIRFile(irFile, newInfo, newClassDef)
    }

    for (irFile <- irFiles) yield {
      irFile.info.encodedName match {
        case ReflectionClass =>
          transformReflectionClass(irFile)
        case _ =>
          irFile
      }
    }
  }

  private final class PatchedMemVirtualScalaJSIRFile(
      original: VirtualFile,
      override val info: ClassInfo,
      override val tree: ClassDef)
      extends VirtualScalaJSIRFile {

    def path: String = original.path
    override def name: String = original.name
    override def version: Option[String] = None // TODO Version this?
    def exists: Boolean = true
    override def toURI: URI = original.toURI

    def infoAndTree = (info, tree)
  }
}
