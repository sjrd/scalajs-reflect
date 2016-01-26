package linkingreflection

import scala.reflect.{ClassTag, classTag}
import scala.util._

import scala.scalajs.js

object Main extends js.JSApp {
  def main(): Unit = {
    show(AkkaLikeReflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[Int] -> (42: Integer), classOf[String] -> "hello")))

    show(AkkaLikeReflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[String] -> "hello")))

    show(AkkaLikeReflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[AnyRef] -> (false, "iop"))))

    show(AkkaLikeReflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[js.Array[_]] -> js.Array(42, 53))))

    show(AkkaLikeReflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[Array[Int]] -> Array(42, 53))))

    show(AkkaLikeReflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[Char] -> ('A': Character))))

    expect[NoSuchMethodException] {
      AkkaLikeReflection.createInstanceFor[SomeConstructible](
          classOf[SomeConstructible],
          List(classOf[List[_]] -> List(42, 53)))
    }

    println(AkkaLikeReflection.getClassFor[FindClassByName](
        "linkingreflection.FindClassByName"))
    println()

    expect[ClassCastException] {
      AkkaLikeReflection.getClassFor[String]("linkingreflection.FindClassByName")
    }

    expect[ClassNotFoundException] {
      AkkaLikeReflection.getClassFor[String]("linkingreflection.DoesNotExist")
    }

    println(AkkaLikeReflection.getObjectFor[FindClassByName](
        "linkingreflection.SomeAccessibleObject"))
    println(AkkaLikeReflection.getObjectFor[FindClassByName](
        "linkingreflection.SomeAccessibleObject$"))
    println()

    expect[ClassCastException] {
      AkkaLikeReflection.getObjectFor[String](
          "linkingreflection.SomeAccessibleObject$")
    }

    expect[ClassNotFoundException] {
      AkkaLikeReflection.getObjectFor[String](
          "linkingreflection.SomeAccessibleObject")
    }

    expect[ClassNotFoundException] {
      AkkaLikeReflection.getObjectFor[String]("linkingreflection.DoesNotExist")
    }
  }

  def show(objTry: Try[SomeConstructible]): Unit = {
    val obj = objTry.get
    println(obj.getClass())
    println(obj.x)
    println(obj.y)
    println()
  }

  def expect[T <: Throwable: ClassTag](objTry: Try[_]): Unit = {
    println("expecting " + classTag[T].runtimeClass)
    val e = objTry.failed.filter(classTag[T].runtimeClass.isInstance(_)).get
    println("Caught: " + e)
    println()
  }
}
