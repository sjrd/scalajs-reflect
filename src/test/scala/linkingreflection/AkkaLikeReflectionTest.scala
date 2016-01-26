package linkingreflection

import scala.util._

import scala.scalajs.js

import org.junit.Test
import org.junit.Assert._

import TestUtils._

trait AkkaGetClassForNameAncestor
class AkkaGetClassForName extends AkkaGetClassForNameAncestor

final case class AkkaSomeConstructible(x: Int, y: String) {
  def this(s: String) = this(s.length, s)

  def this(o: Any) = this(o.toString())

  def this(array: js.Array[Int]) = this(array.size, array.mkString(", "))

  def this(array: Array[Int]) = this(array.size, array.mkString(", "))

  def this(c: Char) = this(c.toInt, c.toString)
}

trait AkkaGetObjectForAncestor
object AkkaGetObjectFor extends AkkaGetObjectForAncestor

class AkkaLikeReflectionTest {

  import linkingreflection.{AkkaLikeReflection => AkkaReflect}

  @Test
  def getClassFor(): Unit = {
    assertSuccess(
        classOf[AkkaGetClassForName],
        AkkaReflect.getClassFor[AkkaGetClassForName](
            "linkingreflection.AkkaGetClassForName"))

    assertSuccess(
        classOf[AkkaGetClassForName],
        AkkaReflect.getClassFor[AkkaGetClassForNameAncestor](
            "linkingreflection.AkkaGetClassForName"))

    assertSuccess(
        classOf[AkkaGetClassForName],
        AkkaReflect.getClassFor[AnyRef](
            "linkingreflection.AkkaGetClassForName"))

    assertFailureSuchThat(
        AkkaReflect.getClassFor[String](
            "linkingreflection.AkkaGetClassForName")) {
      case e: ClassCastException =>
    }

    assertFailureSuchThat(
        AkkaReflect.getClassFor[AkkaGetClassForNameAncestor](
            "linkingreflection.DoesNotExist")) {
      case e: ClassNotFoundException =>
    }

    assertFailureSuchThat(
        AkkaReflect.getClassFor[AnyRef](
            "java.lang.Object")) {
      case e: ClassNotFoundException =>
    }
  }

  @Test
  def createInstanceFor(): Unit = {
    assertSuccess(
        AkkaSomeConstructible(42, "hello"),
        AkkaReflect.createInstanceFor[AkkaSomeConstructible](
            classOf[AkkaSomeConstructible],
            List(classOf[Int] -> (42: Integer), classOf[String] -> "hello")))

    assertSuccess(
        AkkaSomeConstructible(42, "hello"),
        AkkaReflect.createInstanceFor[AnyRef](
            classOf[AkkaSomeConstructible],
            List(classOf[Int] -> (42: Integer), classOf[String] -> "hello")))

    assertSuccess(
        AkkaSomeConstructible(5, "hello"),
        AkkaReflect.createInstanceFor[AkkaSomeConstructible](
            classOf[AkkaSomeConstructible],
            List(classOf[String] -> "hello")))

    assertSuccess(
        AkkaSomeConstructible(11, "(false,iop)"),
        AkkaReflect.createInstanceFor[AkkaSomeConstructible](
            classOf[AkkaSomeConstructible],
            List(classOf[AnyRef] -> (false, "iop"))))

    assertSuccess(
        AkkaSomeConstructible(2, "42, 53"),
        AkkaReflect.createInstanceFor[AkkaSomeConstructible](
            classOf[AkkaSomeConstructible],
            List(classOf[js.Array[_]] -> js.Array(42, 53))))

    assertSuccess(
        AkkaSomeConstructible(2, "42, 53"),
        AkkaReflect.createInstanceFor[AkkaSomeConstructible](
            classOf[AkkaSomeConstructible],
            List(classOf[Array[Int]] -> Array(42, 53))))

    assertSuccess(
        AkkaSomeConstructible(65, "A"),
        AkkaReflect.createInstanceFor[AkkaSomeConstructible](
            classOf[AkkaSomeConstructible],
            List(classOf[Char] -> ('A': Character))))

    assertFailureSuchThat(
        AkkaLikeReflection.createInstanceFor[String](
            classOf[AkkaSomeConstructible],
            List(classOf[Int] -> (42: Integer), classOf[String] -> "hello"))) {
      case e: ClassCastException =>
    }

    assertFailureSuchThat(
        AkkaLikeReflection.createInstanceFor[AkkaSomeConstructible](
            classOf[AkkaSomeConstructible],
            List(classOf[String] -> "hi", classOf[Int] -> (5: Integer),
                classOf[Array[AnyRef]] -> Array[AnyRef]()))) {
      case e: NoSuchMethodException
          if e.getMessage == (classOf[AkkaSomeConstructible].getName +
              ".<init>(java.lang.String, int, [Ljava.lang.Object;)") =>
    }

  }

  @Test
  def getObjectFor(): Unit = {
    assertSuccess(
        AkkaGetObjectFor,
        AkkaReflect.getObjectFor[AkkaGetObjectFor.type](
            "linkingreflection.AkkaGetObjectFor$"))

    assertSuccess(
        AkkaGetObjectFor,
        AkkaReflect.getObjectFor[AkkaGetObjectForAncestor](
            "linkingreflection.AkkaGetObjectFor$"))

    assertSuccess(
        AkkaGetObjectFor,
        AkkaReflect.getObjectFor[AkkaGetObjectForAncestor](
            "linkingreflection.AkkaGetObjectFor"))

    assertSuccess(
        AkkaGetObjectFor,
        AkkaReflect.getObjectFor[AnyRef](
            "linkingreflection.AkkaGetObjectFor$"))

    assertFailureSuchThat(
        AkkaReflect.getObjectFor[String](
            "linkingreflection.AkkaGetObjectFor$")) {
      case e: ClassCastException =>
    }

    assertFailureSuchThat(
        AkkaReflect.getObjectFor[AkkaGetObjectForAncestor](
            "linkingreflection.DoesNotExist$")) {
      case e: ClassNotFoundException =>
    }

    assertFailureSuchThat(
        AkkaReflect.getObjectFor[AnyRef](
            "java.lang.Object")) {
      case e: ClassNotFoundException =>
    }

    assertFailureSuchThat(
        AkkaReflect.getObjectFor[AnyRef](
            "linkingreflection.AkkaGetClassForName")) {
      case e: NoSuchFieldException if e.getMessage == "MODULE$" =>
    }

  }

}
