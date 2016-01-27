package be.doeraene.sjsreflect.sbtplugin

import java.net.URI

import scala.collection.immutable

import org.scalajs.core.ir
import ir.ClassKind
import ir.Infos._
import ir.Trees._
import ir.Types._

import org.scalajs.core.tools.io._
import org.scalajs.core.tools.logging.Logger
import org.scalajs.core.tools.linker.GenLinker

import ReflectSelectors._

final class ReflectionLinker(underlying: GenLinker,
    selectors: immutable.Seq[ReflectSelector])
    extends IRPatchingLinker(underlying) {

  private final val pack = "be.doeraene.sjsreflect"

  private val ReflectClass =
    ir.Definitions.encodeClassName(s"$pack.Reflect$$")
  private val ConstructorClass =
    ir.Definitions.encodeClassName(s"$pack.Constructor")

  protected def patchIRFiles(irFiles: Seq[VirtualScalaJSIRFile],
      logger: Logger): Seq[VirtualScalaJSIRFile] = {

    val infos = irFiles.map(_.info).toList
    val infoByName = infos.map(info => info.encodedName -> info).toMap.withDefault(_ => null)

    val operationSelectors = {
      selectors.groupBy(_.operation).map {
        case (op, selectors) => (op, selectors.map(_.entitySelector).toList)
      }
    }

    def implements(cls: ClassInfo, ancestorName: String): Boolean = {
      // TODO Memoize this function?
      cls != null && {
        cls.encodedName == ancestorName ||
        cls.superClass.exists(superName => implements(infoByName(superName), ancestorName)) ||
        cls.interfaces.exists(intfName => implements(infoByName(intfName), ancestorName))
      }
    }

    def satisfiesSelector(cls: ClassInfo, selector: EntitySelector): Boolean = {
      selector match {
        case SingleClassSelector(fullName) =>
          cls.encodedName == ir.Definitions.encodeClassName(fullName)
        case DescendentClassesSelector(ancestorFullName) =>
          implements(cls, ir.Definitions.encodeClassName(ancestorFullName))
      }
    }

    def satisfiesSelectors(cls: ClassInfo, selectors: List[EntitySelector]): Boolean =
      selectors.exists(s => satisfiesSelector(cls, s))

    def shouldEnableOperation(op: Operation): ClassInfo => Boolean = {
      val selectors = operationSelectors.getOrElse(op, Nil)
      cls => satisfiesSelectors(cls, selectors)
    }

    def erasesToAny(cls: String): Boolean = {
      cls == ir.Definitions.ObjectClass || {
        val info = infoByName(cls)
        info == null || info.kind == ClassKind.RawJSType || info.kind.isJSClass
      }
    }

    def listAllCtors()(implicit pos: ir.Position): Tree = {
      val shouldEnable = shouldEnableOperation(ReflectDeclaredConstructors)

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
        if info.kind == ClassKind.Class && shouldEnable(info)
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

    def makeClassesByName()(implicit pos: ir.Position): Tree = {
      val shouldEnable = shouldEnableOperation(ReflectClassByName)

      val items = for {
        info <- infos
        if shouldEnable(info)
      } yield {
        val decodedName = ir.Definitions.decodeClassName(info.encodedName)
        StringLiteral(decodedName) -> ClassOf(ClassType(info.encodedName))
      }

      JSObjectConstr(items)
    }

    def listAllModuleAccessors()(implicit pos: ir.Position): Tree = {
      val shouldEnable = shouldEnableOperation(ReflectModuleAccessor)

      val items = for {
        info <- infos
        if info.kind == ClassKind.ModuleClass && shouldEnable(info)
      } yield {
        val classType = ClassType(info.encodedName)
        JSArrayConstr(List(
            ClassOf(classType),
            Closure(Nil, Nil, LoadModule(classType), Nil)))
      }

      JSArrayConstr(items)
    }

    def transformReflectionClass(
        irFile: VirtualScalaJSIRFile): VirtualScalaJSIRFile = {
      val classDef = irFile.tree

      def fillMethodWith(m: MethodDef, body: ir.Position => Tree): MethodDef = {
        implicit val pos = m.pos
        val newDef = m.copy(body = body(pos))(m.optimizerHints, None)
        ir.Hashers.hashMethodDef(newDef)
      }

      val newDefs = classDef.defs.map {
        case m: MethodDef if m.name.name == "listAllCtors__sjs_js_Array" =>
          fillMethodWith(m, listAllCtors()(_))

        case m: MethodDef if m.name.name == "makeClassesByName__sjs_js_Dictionary" =>
          fillMethodWith(m, makeClassesByName()(_))

        case m: MethodDef if m.name.name == "listAllModuleAccessors__sjs_js_Array" =>
          fillMethodWith(m, listAllModuleAccessors()(_))

        case m =>
          m
      }

      val newClassDef =
        classDef.copy(defs = newDefs)(classDef.optimizerHints)(classDef.pos)
      val newInfo =
        generateClassInfo(newClassDef)

      new PatchedMemVirtualScalaJSIRFile(irFile, newInfo, newClassDef)
    }

    for (irFile <- irFiles) yield {
      irFile.info.encodedName match {
        case ReflectClass =>
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
