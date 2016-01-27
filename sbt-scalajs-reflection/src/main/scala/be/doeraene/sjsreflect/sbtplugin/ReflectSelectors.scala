package be.doeraene.sjsreflect.sbtplugin

object ReflectSelectors {
  /** A selector to enable some operations on a set of entities.
   *
   *  @param entitySelector Selector for a set of classes in which to look
   *  @param operation      Operation to enable on those classes
   */
  final case class ReflectSelector(
      entitySelector: EntitySelector,
      operation: Operation)

  /** A selector for a set of classes. */
  abstract sealed class EntitySelector

  /** Select a given class. */
  private[sbtplugin] final case class SingleClassSelector(fullName: String)
      extends EntitySelector

  /** Select a given class and all its descendants. */
  private[sbtplugin] final case class DescendentClassesSelector(
      ancestorFullName: String) extends EntitySelector

  /** A reflective operation to be enabled on selected entities. */
  abstract sealed class Operation

  /** Allows to find the selected by its fully qualified name. */
  private[sbtplugin] case object ReflectClassByName extends Operation

  /** Reflect all the declared constructors of a class.
   *
   *  This operation is silently ignored for classes that are interfaces or
   *  JavaScript types.
   */
  private[sbtplugin] case object ReflectDeclaredConstructors extends Operation

  /** Reflect the module accessor of a module class, i.e., an object.
   *
   *  This allows to load the singleton instance of an object given its Class.
   *
   *  This operation is silently ignored for classes that are not module
   *  classes.
   */
  private[sbtplugin] case object ReflectModuleAccessor extends Operation
}
