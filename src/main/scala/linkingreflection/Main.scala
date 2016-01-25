package linkingreflection

import scala.scalajs.js

object Main extends js.JSApp {
  def main(): Unit = {
    val foo = Reflection.createInstanceFor[SomeConstructible](
        classOf[SomeConstructible],
        List(classOf[Int] -> (42: Integer), classOf[String] -> "hello"))

    println(foo.getClass())
    println(foo.x)
    println(foo.y)
  }
}
