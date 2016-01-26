package linkingreflection

import scala.reflect.{ClassTag, classTag}

import org.junit.Test
import org.junit.Assert._

class ExactGetClassForName
class ExactGetClassForNameChild extends ExactGetClassForName

trait GetClassForNameAncestor
class DescendentGetClassForName extends GetClassForNameAncestor

object ExactAccessModule

trait ExactAccessModuleParent
object ExactAccessModuleChild extends ExactAccessModuleParent

trait AccessModuleAncestor
object DescendentAccessModule extends AccessModuleAncestor

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

  @Test
  def loadModule(): Unit = {
    /* for some reason classOf[Module.type] does not work
     * but classOfModule[Module.type] does.
     */
    def classOfModule[A: ClassTag]: Class[_] = classTag[A].runtimeClass

    assertEquals(
        Some(ExactAccessModule),
        Reflection.loadModule(classOfModule[ExactAccessModule.type]))

    assertEquals(
        Some(DescendentAccessModule),
        Reflection.loadModule(classOfModule[DescendentAccessModule.type]))

    assertEquals(
        None,
        Reflection.loadModule(classOfModule[ExactAccessModuleChild.type]))

    assertEquals(
        None,
        Reflection.loadModule(classOfModule[None.type]))

    assertEquals(
        None,
        Reflection.loadModule(classOf[ExactAccessModuleParent]))
  }

}
