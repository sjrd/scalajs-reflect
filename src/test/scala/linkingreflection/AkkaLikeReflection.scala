package linkingreflection

import scala.collection.immutable
import scala.reflect.{ClassTag, classTag}

import Reflection._

object AkkaLikeReflection {

  @noinline // I receive a ClassTag, but I'm really no good to inline
  def createInstanceFor[T: ClassTag](clazz: Class[_],
      args: immutable.Seq[(Class[_], AnyRef)]): T = {

    val ctors = getDeclaredConstructors(clazz)
    val expectedParams = args.map(_._1)

    ctors.find(_.getParameterTypes().sameElements(expectedParams)) match {
      case Some(ctor) =>
        ctor.newInstance(args.map(_._2): _*).asInstanceOf[T]

      case None =>
        throw new java.lang.NoSuchMethodException
    }
  }

  @noinline // I receive a ClassTag, but I'm really no good to inline
  def getClassFor[T: ClassTag](fqcn: String): Class[_ <: T] = {
    val result = getClassForName(fqcn).getOrElse(
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

    loadModule(clazz).getOrElse(
        throw new NoSuchFieldException("Cannot load the module of " + clazz))
  }

}
