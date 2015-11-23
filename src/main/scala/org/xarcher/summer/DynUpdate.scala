package org.xarcher.summer

import slick.driver.{JdbcActionComponent, JdbcProfile}

import scala.language.existentials
import scala.language.higherKinds
import slick.dbio._
import slick.lifted._

/**
 * Created by djx314 on 15-6-22.
 */

case class DynData[E <: AbstractTable[_], U](colTra: E => Rep[U], value: U)(implicit val dynShape: Shape[_ <: ShapeLevel, Rep[U], U, Rep[U]]) {
  type DataType = U
}

trait UpdateInfoContent[E <: AbstractTable[_], F[_]] {

  val query: Query[E, _, F]
  val dataList: List[DynData[E, _]]

  private def changHead[E <: AbstractTable[_]](change: DynData[E, _]): Change[E] = change match {
    case change@DynData(currentColTran, currentValue) =>
      import change._
      val colunm: E => Tuple1[Rep[DataType]] = (table: E) => Tuple1(currentColTran(table))
      val value =  Tuple1(currentValue)
      implicit val dynTuple1Shape = new TupleShape[FlatShapeLevel, Tuple1[Rep[DataType]], Tuple1[DataType], Tuple1[Rep[DataType]]](dynShape)
      new Change[E] {
        override type ColType = Tuple1[Rep[DataType]]
        override type ValType = Tuple1[DataType]
        override val col = colunm
        override val data = value
        override val shape = dynTuple1Shape
      }
  }

  def change[T](col: E => Rep[T], value: T)
    (implicit dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) = {
    val data = DynData(col, value)
    val subQuery = query
    val subDataList = data :: dataList
    new UpdateInfoContent[E, F] {
      override val query = subQuery
      override val dataList = subDataList
    }
  }

  def changeIf[T](b: Boolean)(col: E => Rep[T], value: T)
    (implicit dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) = {
    if(b) change(col, value) else this
  }

  private lazy val changes = dataList match {
    case change :: tail =>
      tail.foldLeft(changHead(change))((r, c) =>
        r.append(c)
      )
    case _ => throw new IllegalArgumentException("Cannot convert a empty list to Chage[_]")
  }

  def result(implicit convert: Query[changes.ColType, changes.ValType, F] => JdbcActionComponent#UpdateActionExtensionMethods[changes.ValType]): DBIO[Int] = {
    dataList match {
      case head :: tail =>
        convert(query.map(changes.col(_))(changes.shape)).update(changes.data)
      case _ =>
        DBIO successful 0
    }
  }

}

object DynUpdate {

  def apply[E <: AbstractTable[_], F[_]](initQuery: Query[E, _, F]) =
    withChanges(initQuery, Nil)

  def withChanges[E <: AbstractTable[_], F[_]](initQuery: Query[E, _, F], updateDataList: List[DynData[E, _]]) =
    new UpdateInfoContent[E, F] {
      override val query = initQuery
      override val dataList = updateDataList
    }

}

private trait Change[E <: AbstractTable[_]] {
  type ColType <: Product
  type ValType <: Product
  val col: E => ColType
  val data: ValType
  val shape:  Shape[_ <: FlatShapeLevel, ColType, ValType, ColType]

  def append(change: DynData[E, _]) = change match {
    case change@DynData(currentColTran, currentValue) =>
      import change._
      type NewColType = (Rep[DataType], ColType)
      type NewValType = (DataType, ValType)
      val colunm: E => NewColType = (table: E) => currentColTran(table) -> col(table)
      val dynTuple2Shape = new TupleShape[FlatShapeLevel, (Rep[DataType], ColType), (DataType, ValType), (Rep[DataType], ColType)](dynShape, shape)
      val value =  currentValue -> data
      new Change[E] {
        override type ColType = NewColType
        override type ValType = NewValType
        override val col = colunm
        override val data = value
        override val shape = dynTuple2Shape
      }
  }

}