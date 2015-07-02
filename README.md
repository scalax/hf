# slick-summer -- slick update功能增强

[English](https://github.com/scalax/slick-summer/blob/master/README_en.md)

## 简介
```scala
Persons.filter(_.id === id).map { p =>
  (p.name, p.address, p.postcode, p.updateTime)
}.update(("foo", "bar", "baz", now))
```
以上是一个典型的slick update例子，api设计的很合理和collection基本一致
但很难实现以下需求
+ 选择性更新某几列
+ 更新超过22列

本项目实现了以上功能，然而一定程度上破坏了和collection的一致性

```scala
import org.xarcher.summer._

Persons
  .filter(_.id === id)
  .change(_.name, "foo")
  .change(_.address, "bar")
  .change(_.postcode, "baz")
  .change(_.updateTime, now)
  .result
```

另外新增了`changeIf`来做选择性更新

```scala
import org.xarcher.summer._
Persons
  .filter(_.id === id)
  .changeIf("foo" == "bar")(_.name, "foo")
  .result
```

如何使用可以查看[单元测试](https://github.com/scalax/slick-summer/blob/master/src/test/scala/org/xarcher/summer/test/DynamicUpdateTest.scala)

## 实现思路

将更新列转化为`C1 -> C2 -> C3 -> ... -> Cn`的形式，然后自动提供`Shape`
