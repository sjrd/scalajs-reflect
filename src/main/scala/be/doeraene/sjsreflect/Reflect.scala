package be.doeraene.sjsreflect

import scala.collection.immutable
import scala.reflect.{ClassTag, classTag}

import scala.scalajs.js
import js.JSConverters._

object Reflect {
  private val ctorsByClass =
    listAllCtors().groupBy(_.clazz).withDefault(_ => js.Array())

  // Filled in by the linker plugin
  // private[Reflect] for a predictable IR name
  private[Reflect] def listAllCtors(): js.Array[Constructor[_]] = ???

  private val classesByName = makeClassesByName()

  // Filled in by the linker plugin
  // private[Reflect] for a predictable IR name
  private[Reflect] def makeClassesByName(): js.Dictionary[Class[_]] = ???

  private val moduleAccessorsByClass =
    listAllModuleAccessors().map(t => t: (Class[_], js.Function0[Any])).toMap

  // Filled in by the linker plugin
  // private[Reflect] for a predictable IR name
  private[Reflect]
  def listAllModuleAccessors(): js.Array[js.Tuple2[Class[_], js.Function0[Any]]] = ???

  def getDeclaredConstructors[T](clazz: Class[T]): Array[Constructor[T]] =
    ctorsByClass(clazz).asInstanceOf[js.Array[Constructor[T]]].toArray

  def getClassForName(fqcn: String): Option[Class[_]] =
    classesByName.get(fqcn)

  def loadModule[T](clazz: Class[T]): Option[T] =
    moduleAccessorsByClass.get(clazz).map(f => f().asInstanceOf[T])
}
