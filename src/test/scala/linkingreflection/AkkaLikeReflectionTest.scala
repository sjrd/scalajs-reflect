package linkingreflection

import scala.util._

import org.junit.Test
import org.junit.Assert._

import TestUtils._

trait AkkaGetClassForNameAncestor
class AkkaGetClassForName extends AkkaGetClassForNameAncestor

class AkkaLikeReflectionTest {

  import linkingreflection.{AkkaLikeReflection => AkkaReflect}

  @Test
  def getClassFor(): Unit = {
    assertEquals(
        Success(classOf[AkkaGetClassForName]),
        AkkaReflect.getClassFor[AkkaGetClassForName](
            "linkingreflection.AkkaGetClassForName"))

    assertEquals(
        Success(classOf[AkkaGetClassForName]),
        AkkaReflect.getClassFor[AkkaGetClassForNameAncestor](
            "linkingreflection.AkkaGetClassForName"))

    assertEquals(
        Success(classOf[AkkaGetClassForName]),
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

}
