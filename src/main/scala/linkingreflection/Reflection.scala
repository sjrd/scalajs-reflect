package linkingreflection

import scala.collection.immutable
import scala.reflect.ClassTag

import scala.scalajs.js
import js.JSConverters._

object Reflection {
  private final class Constructor(
      val clazz: Class[_],
      val params: js.Array[Class[_]],
      val invoke: js.Dynamic)

  private val ctorsByClass = {
    listAllCtors().groupBy(_.clazz).withDefault(_ => js.Array())
  }

  // Filled in by the linker plugin
  // private[Reflection] for a predictable IR name
  private[Reflection] def listAllCtors(): js.Array[Constructor] = ???

  def createInstanceFor[T: ClassTag](clazz: Class[_],
      args: immutable.Seq[(Class[_], AnyRef)]): T = {

    val ctors = ctorsByClass(clazz)
    val argsArray = args.toJSArray
    val expectedParams = argsArray.map(_._1)

    ctors.find(_.params.sameElements(expectedParams)) match {
      case Some(ctor) =>
        val actualArgs = argsArray.map(_._2).asInstanceOf[js.Array[js.Any]]
        ctor.invoke(actualArgs: _*).asInstanceOf[T]

      case None =>
        throw new java.lang.NoSuchMethodException
    }
  }
}
