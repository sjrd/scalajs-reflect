package linkingreflection

import scala.scalajs.js

object Main extends js.JSApp {
  def main(): Unit = {
    show(Reflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[Int] -> (42: Integer), classOf[String] -> "hello")))

    show(Reflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[String] -> "hello")))

    show(Reflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[AnyRef] -> (false, "iop"))))

    show(Reflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[js.Array[_]] -> js.Array(42, 53))))

    show(Reflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[Array[Int]] -> Array(42, 53))))

    show(Reflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[Char] -> ('A': Character))))

    try {
      show(Reflection.createInstanceFor[SomeConstructible](
          classOf[SomeConstructible],
          List(classOf[List[_]] -> List(42, 53))))
    } catch {
      case _: NoSuchMethodException =>
        println("Caught NoSuchMethodException")
    }
  }

  def show(obj: SomeConstructible): Unit = {
    println(obj.getClass())
    println(obj.x)
    println(obj.y)
    println()
  }
}
