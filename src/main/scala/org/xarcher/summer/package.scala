package org.xarcher

import scala.language.existentials
import slick.lifted._
import scala.language.higherKinds

package object summer {

  implicit class QuerySyntax[E <: AbstractTable[_], F[_]](val baseQuery: Query[E, _, F]) {
    def change[T](col: E => Rep[T], value: T)(implicit dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) = {
      val data = DynData(col, value)
      UpdateBuilder(data :: Nil, baseQuery)
    }

    def changeIf[T](b: => Boolean)(col: E => Rep[T], value: T)(implicit dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) = {
      if(b) change(col, value) else UpdateBuilder(Nil, baseQuery)
    }
  }

  case class UpdateBuilder[E <: AbstractTable[_], F[_]](changes: List[DynData[E, _]], query: Query[E, _, F]) {
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
