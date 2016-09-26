package be.doeraene.sjsreflect

import scala.reflect.{ClassTag, classTag}

import scala.scalajs.js

import org.junit.Test
import org.junit.Assert._

class ExactGetClassForName
class ExactGetClassForNameChild extends ExactGetClassForName

trait GetClassForNameAncestor
class DescendentGetClassForName extends GetClassForNameAncestor

trait EnumeratedClass
class EnumeratedClassChild extends EnumeratedClass

trait EnumeratedClassAncestor
class DescendentEnumeratedClass extends EnumeratedClassAncestor

final case class ExactGetDeclaredConstructors(x: Int, y: String) {
  def this(s: String) = this(s.length, s)

  def this(o: Any) = this(o.toString())

  def this(array: js.Array[Int]) = this(array.size, array.mkString(", "))

  def this(array: Array[Int]) = this(array.size, array.mkString(", "))

  def this(c: Char) = this(c.toInt, c.toString)
}

trait GetDeclaredConstructorsAncestor
case class DescendentGetDeclaredConstructors(x: Int)
    extends GetDeclaredConstructorsAncestor

object ExactAccessModule

trait ExactAccessModuleParent
object ExactAccessModuleChild extends ExactAccessModuleParent

trait AccessModuleAncestor
object DescendentAccessModule extends AccessModuleAncestor

class ReflectTest {

  import TestUtils._

  @Test
  def getClassForName(): Unit = {
    assertEquals(
        Some(classOf[ExactGetClassForName]),
        Reflect.getClassForName(s"$pack.ExactGetClassForName"))

    assertEquals(
        Some(classOf[GetClassForNameAncestor]),
        Reflect.getClassForName(s"$pack.GetClassForNameAncestor"))

    assertEquals(
        Some(classOf[DescendentGetClassForName]),
        Reflect.getClassForName(s"$pack.DescendentGetClassForName"))

    assertEquals(
        None,
        Reflect.getClassForName(s"$pack.ExactGetClassForNameChild"))

    assertEquals(
        None,
        Reflect.getClassForName("java.lang.Object"))
  }

  @Test
  def enumerateClasses(): Unit = {
    assertTrue(
      Reflect.enumerateClasses.contains(classOf[EnumeratedClass]))

    assertTrue(
      Reflect.enumerateClasses.contains(classOf[EnumeratedClassAncestor]))

    assertTrue(
      Reflect.enumerateClasses.contains(classOf[DescendentEnumeratedClass]))

    assertFalse(
      Reflect.enumerateClasses.contains(classOf[EnumeratedClassChild]))

    assertTrue(Reflect.enumerateClasses.length == 3)
  }

  @Test
  def getDeclaredConstructors(): Unit = {
    val ctors1 = Reflect.getDeclaredConstructors(
        classOf[ExactGetDeclaredConstructors])
    assertEquals(6, ctors1.length)

    assertRefArrayEquals(
        Array[AnyRef](classOf[Int], classOf[String]),
        ctors1(0).getParameterTypes())
    assertEquals(
        ExactGetDeclaredConstructors(42, "hello"),
        ctors1(0).newInstance(42, "hello"))

    assertRefArrayEquals(
        Array[AnyRef](classOf[String]),
        ctors1(1).getParameterTypes())
    assertEquals(
        ExactGetDeclaredConstructors(5, "hello"),
        ctors1(1).newInstance("hello"))

    assertRefArrayEquals(
        Array[AnyRef](classOf[AnyRef]),
        ctors1(2).getParameterTypes())
    assertEquals(
        ExactGetDeclaredConstructors(11, "(false,iop)"),
        ctors1(2).newInstance((false, "iop")))

    assertRefArrayEquals(
        Array[AnyRef](classOf[js.Array[_]]),
        ctors1(3).getParameterTypes())
    assertEquals(
        ExactGetDeclaredConstructors(2, "42, 53"),
        ctors1(3).newInstance(js.Array(42, 53)))

    assertRefArrayEquals(
        Array[AnyRef](classOf[Array[Int]]),
        ctors1(4).getParameterTypes())
    assertEquals(
        ExactGetDeclaredConstructors(2, "42, 53"),
        ctors1(4).newInstance(Array(42, 53)))

    assertRefArrayEquals(
        Array[AnyRef](classOf[Char]),
        ctors1(5).getParameterTypes())
    assertEquals(
        ExactGetDeclaredConstructors(65, "A"),
        ctors1(5).newInstance('A'))

    val ctors2 = Reflect.getDeclaredConstructors(
        classOf[DescendentGetDeclaredConstructors])
    assertEquals(1, ctors2.length)

    assertRefArrayEquals(
        Array[AnyRef](classOf[Int]),
        ctors2(0).getParameterTypes())
    assertEquals(
        DescendentGetDeclaredConstructors(42),
        ctors2(0).newInstance(42))

    val ctors3 = Reflect.getDeclaredConstructors(
        classOf[Object])
    assertEquals(0, ctors3.length)
  }

  @Test
  def loadModule(): Unit = {
    /* for some reason classOf[Module.type] does not work
     * but classOfModule[Module.type] does.
     */
    def classOfModule[A: ClassTag]: Class[_] = classTag[A].runtimeClass

    assertEquals(
        Some(ExactAccessModule),
        Reflect.loadModule(classOfModule[ExactAccessModule.type]))

    assertEquals(
        Some(DescendentAccessModule),
        Reflect.loadModule(classOfModule[DescendentAccessModule.type]))

    assertEquals(
        None,
        Reflect.loadModule(classOfModule[ExactAccessModuleChild.type]))

    assertEquals(
        None,
        Reflect.loadModule(classOfModule[None.type]))

    assertEquals(
        None,
        Reflect.loadModule(classOf[ExactAccessModuleParent]))
  }

}
