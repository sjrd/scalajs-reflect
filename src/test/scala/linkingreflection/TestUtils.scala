package linkingreflection

import scala.util._

import org.junit.Assert._

object TestUtils {

  def assertRefArrayEquals(expecteds: Array[_ <: AnyRef],
      actuals: Array[_ <: AnyRef]): Unit = {
    assertArrayEquals(expecteds.asInstanceOf[Array[AnyRef]],
        actuals.asInstanceOf[Array[AnyRef]])
  }

  def assertSuccess(expected: Any, value: Try[Any]): Unit =
    assertEquals(Success(expected), value)

  def assertFailureSuchThat(value: Try[_])(
      pf: PartialFunction[Throwable, Unit]): Unit = {
    val exception = value.failed.get
    pf(exception) // throws MatchError if does not apply
  }

}
