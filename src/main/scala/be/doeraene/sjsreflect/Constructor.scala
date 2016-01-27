package be.doeraene.sjsreflect

import scala.scalajs.js

final class Constructor[T] private[sjsreflect] (
    private[sjsreflect] val clazz: Class[T],
    private[sjsreflect] val params: js.Array[Class[_]],
    private[sjsreflect] val invoke: js.Dynamic) {

  def getParameterTypes(): Array[Class[_]] = params.toArray

  def newInstance(args: Any*): T =
    invoke(args.asInstanceOf[Seq[js.Any]]: _*).asInstanceOf[T]
}
