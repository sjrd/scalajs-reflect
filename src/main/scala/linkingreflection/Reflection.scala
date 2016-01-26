package linkingreflection

import scala.collection.immutable
import scala.reflect.{ClassTag, classTag}

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

  private val classesByName = makeClassesByName()

  // Filled in by the linker plugin
  // private[Reflection] for a predictable IR name
  private[Reflection] def makeClassesByName(): js.Dictionary[Class[_]] = ???

  private val moduleAccessorByClass =
    listAllModuleAccessors().map(t => t: (Class[_], js.Function0[Any])).toMap

  // Filled in by the linker plugin
  // private[Reflection] for a predictable IR name
  private[Reflection]
  def listAllModuleAccessors(): js.Array[js.Tuple2[Class[_], js.Function0[Any]]] = ???

  @noinline // I receive a ClassTag, but I'm really no good to inline
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

  @noinline // I receive a ClassTag, but I'm really no good to inline
  def getClassFor[T: ClassTag](fqcn: String): Class[_ <: T] = {
    val result = classesByName.getOrElse(fqcn,
        throw new ClassNotFoundException("Cannot find class " + fqcn))

    val expected = classTag[T].runtimeClass
    if (!expected.isAssignableFrom(result))
      throw new ClassCastException(expected + " is not assignable from " + result)

    result.asInstanceOf[Class[_ <: T]]
  }

  @noinline // I receive a ClassTag, but I'm really no good to inline
  def getObjectFor[T: ClassTag](fqcn: String): T = {
    val clazz =
      if (fqcn.endsWith("$")) getClassFor[T](fqcn)
      else getClassFor[T](fqcn + "$")

    val accessor = moduleAccessorByClass.getOrElse(clazz,
        throw new NoSuchFieldException("Cannot load the module of " + clazz))

    accessor().asInstanceOf[T]
  }
}
