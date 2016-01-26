package linkingreflection

import scala.language.existentials

import scala.collection.immutable
import scala.reflect.{ClassTag, classTag}
import scala.util._

import Reflection._

object AkkaLikeReflection {

  @noinline // I receive a ClassTag, but I'm really no good to inline
  def getClassFor[T: ClassTag](fqcn: String): Try[Class[_ <: T]] = {
    getClassForName(fqcn) match {
      case None =>
        Failure(new ClassNotFoundException("Cannot find class " + fqcn))

      case Some(result) =>
        val expected = classTag[T].runtimeClass
        if (!expected.isAssignableFrom(result))
          Failure(new ClassCastException(expected + " is not assignable from " + result))
        else
          Success(result.asInstanceOf[Class[_ <: T]])
    }
  }

  @noinline // I receive a ClassTag, but I'm really no good to inline
  def createInstanceFor[T: ClassTag](clazz: Class[_],
      args: immutable.Seq[(Class[_], AnyRef)]): Try[T] = {

    val ctors = getDeclaredConstructors(clazz)
    val expectedParams = args.map(_._1)

    ctors.find(_.getParameterTypes().sameElements(expectedParams)) match {
      case Some(ctor) =>
        Try {
          val obj = ctor.newInstance(args.map(_._2): _*)
          val t = implicitly[ClassTag[T]].runtimeClass
          if (t.isInstance(obj)) obj.asInstanceOf[T]
          else throw new ClassCastException(clazz.getName + " is not a subtype of " + t)
        }

      case None =>
        val signature = expectedParams.map(_.getName).mkString("(", ", ", ")")
        Failure(new NoSuchMethodException(
            clazz.getName + ".<init>" + signature))
    }
  }

  @noinline // I receive a ClassTag, but I'm really no good to inline
  def getObjectFor[T: ClassTag](fqcn: String): Try[T] = {
    val classTry =
      if (fqcn.endsWith("$")) getClassFor(fqcn)
      else getClassFor(fqcn + "$") recoverWith { case _ => getClassFor(fqcn) }

    classTry.flatMap { clazz =>
      Try {
        loadModule(clazz) match {
          case Some(obj) =>
            val t = implicitly[ClassTag[T]].runtimeClass
            if (t.isInstance(obj)) obj
            else throw new ClassCastException(fqcn + " is not a subtype of " + t)

          case None =>
            throw new NoSuchFieldException("MODULE$") // mimic the JVM
        }
      }
    }
  }

}
