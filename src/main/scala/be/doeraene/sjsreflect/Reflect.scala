package be.doeraene.sjsreflect

import scala.collection.immutable
import scala.reflect.{ClassTag, classTag}

import scala.scalajs.js
import js.JSConverters._

object Reflect {
  private val classesByName =
    makeClassesByName()

  private val ctorsByClass =
    listAllCtors().groupBy(_.clazz).withDefault(_ => js.Array())

  private val moduleAccessorsByClass =
    listAllModuleAccessors().map(t => t: (Class[_], js.Function0[Any])).toMap

  /* The following methods are filled in by the linker plugin.
   * They are `private[Reflect]` rather than `private` so that they have a
   * predictable IR name for the linker manipulate.
   */

  private[Reflect] def makeClassesByName(): js.Dictionary[Class[_]] = stub()

  private[Reflect] def listAllCtors(): js.Array[Constructor[_]] = stub()

  private[Reflect]
  def listAllModuleAccessors(): js.Array[js.Tuple2[Class[_], js.Function0[Any]]] = stub()

  private def stub(): Nothing = {
    throw new NotImplementedError(
        "A method of be.doeraene.sjsreflect.Reflect was not implemented. " +
        "Make sure you have enabled the ScalaJSReflectionPlugin on your " +
        "project with `enablePlugins(ScalaJSReflectionPlugin)`")
  }

  // The API starts here

  /** Looks up a `java.lang.Class[_]` given its fully qualified name.
   *
   *  Only classes registered with the `reflectClassByName()` operation can
   *  be found with this method.
   */
  def getClassForName(fqcn: String): Option[Class[_]] =
    classesByName.get(fqcn)

  /** Lists the constructors declared in a given `Class`.
   *
   *  Only the constructors of classes registered with the
   *  `reflectDeclaredConstructors()` operation can be found with this method.
   *
   *  If the given `Class` is an interface or a JavaScript type, or if its
   *  constructors have not been registered, an empty array is returned.
   */
  def getDeclaredConstructors[T](clazz: Class[T]): Array[Constructor[T]] =
    ctorsByClass(clazz).asInstanceOf[js.Array[Constructor[T]]].toArray

  /** Loads the module instance of a given `Class`.
   *
   *  The `Class` must identify a module class (not its companion class).
   *
   *  Only the modules of classes registered with the `reflectModuleAccessor()`
   *  operation can be found with this method.
   */
  def loadModule[T](clazz: Class[T]): Option[T] =
    moduleAccessorsByClass.get(clazz).map(f => f().asInstanceOf[T])
}
