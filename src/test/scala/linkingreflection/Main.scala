package linkingreflection

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

    try {
      show(AkkaLikeReflection.createInstanceFor[SomeConstructible](
          classOf[SomeConstructible],
          List(classOf[List[_]] -> List(42, 53))))
    } catch {
      case _: NoSuchMethodException =>
        println("Caught NoSuchMethodException")
        println()
    }

    println(AkkaLikeReflection.getClassFor[FindClassByName](
        "linkingreflection.FindClassByName"))
    println()

    try {
      AkkaLikeReflection.getClassFor[String]("linkingreflection.FindClassByName")
    } catch {
      case e: ClassCastException =>
        println("Caught ClassCastException: " + e.getMessage)
        println()
    }

    try {
      AkkaLikeReflection.getClassFor[String]("linkingreflection.DoesNotExist")
    } catch {
      case e: ClassNotFoundException =>
        println("Caught ClassNotFoundException: " + e.getMessage)
        println()
    }

    println(AkkaLikeReflection.getObjectFor[FindClassByName](
        "linkingreflection.SomeAccessibleObject"))
    println(AkkaLikeReflection.getObjectFor[FindClassByName](
        "linkingreflection.SomeAccessibleObject$"))
    println()

    try {
      AkkaLikeReflection.getObjectFor[String](
          "linkingreflection.SomeAccessibleObject")
    } catch {
      case e: ClassCastException =>
        println("Caught ClassCastException: " + e.getMessage)
        println()
    }

    try {
      AkkaLikeReflection.getObjectFor[String]("linkingreflection.DoesNotExist")
    } catch {
      case e: ClassNotFoundException =>
        println("Caught ClassNotFoundException: " + e.getMessage)
        println()
    }
  }

  def show(obj: SomeConstructible): Unit = {
    println(obj.getClass())
    println(obj.x)
    println(obj.y)
    println()
  }
}
