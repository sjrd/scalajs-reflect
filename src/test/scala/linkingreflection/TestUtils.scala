package linkingreflection

import scala.util._

object TestUtils {

  def assertFailureSuchThat(value: Try[_])(
      pf: PartialFunction[Throwable, Unit]): Unit = {
    val exception = value.failed.get
    pf(exception) // throws MatchError if does not apply
  }

}
