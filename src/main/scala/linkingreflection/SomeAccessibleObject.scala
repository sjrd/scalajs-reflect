package linkingreflection

object SomeAccessibleObject extends AccessModule with FindClassByName {
  override def toString(): String = "found it"
}
