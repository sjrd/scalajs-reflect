package linkingreflection

import org.junit.Test
import org.junit.Assert._

class ExactGetClassForName
class ExactGetClassForNameChild extends ExactGetClassForName

trait GetClassForNameAncestor
class DescendentGetClassForName extends GetClassForNameAncestor

class ReflectionTest {

  @Test
  def getClassForName(): Unit = {
    assertEquals(
        Some(classOf[ExactGetClassForName]),
        Reflection.getClassForName("linkingreflection.ExactGetClassForName"))

    assertEquals(
        Some(classOf[GetClassForNameAncestor]),
        Reflection.getClassForName("linkingreflection.GetClassForNameAncestor"))

    assertEquals(
        Some(classOf[DescendentGetClassForName]),
        Reflection.getClassForName("linkingreflection.DescendentGetClassForName"))

    assertEquals(
        None,
        Reflection.getClassForName("linkingreflection.ExactGetClassForNameChild"))

    assertEquals(
        None,
        Reflection.getClassForName("java.lang.Object"))
  }

}
