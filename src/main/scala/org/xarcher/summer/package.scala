package org.xarcher

import scala.language.existentials
import scala.language.higherKinds
import scala.language.implicitConversions
import slick.dbio._
import slick.lifted._

/**
  * Created by djx314 on 2015/11/23.
  */
package object summer {

  implicit class queryToSlickSummerUpdateInfoContent[E <: AbstractTable[_], F[_]](query: Query[E, _, F]) {

    def change[T](col: E => Rep[T], value: T)
                 (implicit dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) = {
      val data = DynData(col, value)
      val dataList = data :: Nil
      DynUpdate.withChanges(query, dataList)
    }

    def changeIf[T](b: Boolean)(col: E => Rep[T], value: T)
                   (implicit dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) = {
      val dataList = if (b) DynData(col, value) :: Nil else Nil
      DynUpdate.withChanges(query, dataList)
    }

  }

}