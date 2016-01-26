package linkingreflection

import scala.scalajs.js

class SomeConstructible(val x: Int, val y: String) extends ReflectConstructors {
  def this(s: String) = this(s.length, s)

  def this(o: Any) = this(o.toString())

  def this(array: js.Array[Int]) = this(array.size, array.mkString(", "))

  def this(array: Array[Int]) = this(array.size, array.mkString(", "))

  def this(c: Char) = this(c.toInt, c.toString)
}
