package org.xarcher

import scala.language.existentials
import slick.driver.JdbcDriver.api._
import slick.lifted.AbstractTable

package object summer {
  implicit class QuerySyntax[E <: AbstractTable[_]](val baseQuery: Query[E, _, Seq]) {
    def change[T](col: E => Rep[T], value: T)(implicit dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) = {
      val data = DynData(col, value)
      UpdateBuilder(data :: Nil, baseQuery)
    }

    def changeIf[T](b: => Boolean)(col: E => Rep[T], value: T)(implicit dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) = {
      if(b) change(col, value) else UpdateBuilder(Nil, baseQuery)
    }
  }

  case class UpdateBuilder[E <: AbstractTable[_]](changes: List[DynData[E, _]], query: Query[E, _, Seq]) {
    def change[T](col: E => Rep[T], value: T)
      (implicit dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) = {
      val data = DynData(col, value)
      UpdateBuilder(data :: changes, query)
    }

    def changeIf[T](b: Boolean)(col: E => Rep[T], value: T)
      (implicit dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) = {
      if(b) change(col, value) else this
    }

    def result = DynUpdate.update(query)(changes)
  }
}
