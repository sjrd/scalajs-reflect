package linkingreflection

class SomeConstructible(val x: Int, val y: String) extends ReflectConstructors {
  def this(s: String) = this(s.length, s)
}
