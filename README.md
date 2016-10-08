# scalajs-reflection

[![Build Status](https://travis-ci.org/sjrd/scalajs-reflect.svg?branch=master)](https://travis-ci.org/sjrd/scalajs-reflect)
[![Scala.js](http://scala-js.org/assets/badges/scalajs-0.6.6.svg)](http://scala-js.org/)

scalajs-reflection is a combination of an sbt plugin and a run-time API
enabling a controlled amount of run-time reflection in Scala.js projects.

## Usage

Add the following line to your `project/plugins.sbt` file:

```scala
addSbtPlugin("be.doeraene" % "sbt-scalajs-reflection" % "0.1.0")
```

and the following settings to your `build.sbt` file:

```scala
enablePlugins(ScalaJSReflectionPlugin)

libraryDependencies += "be.doeraene" %%% "scalajs-reflection" % "0.1.0"
```

In addition, you need to specify what classes will be reflectible, and what
reflective operations will be enabled on them.
To do so, use the `scalaJSReflectSelectors` setting.
For example, to enable `Reflect.getClassForName` on all subclasses of some
class or trait `foo.Bar`, use:

```scala
scalaJSReflectSelectors ++= Seq(
  selectDescendentClasses("foo.Bar") -> reflectClassByName()
)
```

Each element of the `Seq` must be a pair of an *entity selector* and a
*reflective operation*. The operation will be enabled on all classes matched
by the entity selector.

Available selectors are:

* `selectSingleClass("foo.Bar")`: select exclusively `foo.Bar`
* `selectDescendentClasses("foo.Bar")`: select `foo.Bar` and all its descendants

Note that, to select the class of a Scala `object`, you need to append a `$`
at the end of its full name, e.g., `"foo.Bar$"`.

Available operations are:

* `reflectClassByName()`: enables `Reflect.getClassForName`
* `reflectEnumerateClass()` enables `Reflect.enumerateClasses`
* `reflectDeclaredConstructors()`: enables `Reflect.getDeclaredConstructors`
* `reflectModuleAccessor()`: enables `Reflect.loadModule`

## API

* `be.doeraene.sjsreflect.Reflect`
  * `Reflect.getClassForName(fqcn: String): Class[_]`:
    find a class by its name
  * `Reflect.enumerateClasses: Array[Class[_]]`:
    an array of the specified classes
  * `Reflect.getDeclaredConstructors[T](clazz: Class[T]): Array[Constructor[T]]`:
    lists the declared constructors of the given class
  * `Reflect.loadModule[T](clazz: Class[T]): T`:
    loads the module instance of the given module class (a Scala `object`)
* `be.doeraene.sjsreflect.Constructor[T]`
  * `ctor.getParameterTypes(): Array[Class[_]]`:
    returns the list of the parameter types of the constructor
  * `ctor.newInstance(actualArgs: Any*): T`:
    invokes the constructor with the given arguments, and returns the created instance
