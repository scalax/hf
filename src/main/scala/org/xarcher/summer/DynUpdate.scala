package org.xarcher.summer

import scala.language.existentials
import slick.driver.JdbcDriver.api._
import slick.lifted.{TupleShape, ShapeLevel}
import slick.lifted.AbstractTable

/**
 * Created by djx314 on 15-6-22.
 */

case class DynData[E <: AbstractTable[_], T](colTra: E => Rep[T], value: T)(implicit val dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) {
  type DataType = T
}

trait DynUpdate {

  def update[E <: AbstractTable[_]](q: Query[E, _, Seq])(dataList: List[DynData[E, _]]): DBIOAction[Int, NoStream, Effect.Write] = {
    dataList match {
      case change :: tail =>
        val init = Change(change)
        val changes = tail.foldLeft(init) { (r, c) =>
          r.append(c)
        }
        import changes._
        q.map(col).update(data)
      case Nil => DBIO.successful(0)
    }
  }
}

private trait Change[E <: AbstractTable[_]] {
  type ColType <: Product
  type ValType <: Product
  val col: E => ColType
  val data: ValType
  implicit val shape:  Shape[_ <: FlatShapeLevel, ColType, ValType, ColType]

  def append(change: DynData[E, _]) = change match {
    case change@DynData(currentColTran, currentValue) =>
      import change._
      type NewColType = (Rep[DataType], ColType)
      type NewValType = (DataType, ValType)
      val colunm: E => NewColType = (table: E) => currentColTran(table) -> col(table)
      implicit val dynTuple2Shape = new TupleShape[FlatShapeLevel, (Rep[DataType], ColType), (DataType, ValType), (Rep[DataType], ColType)](dynShape, shape)
      val value =  currentValue -> data
      new Change[E] {
        type ColType = NewColType
        type ValType = NewValType
        val col = colunm
        val data = value
        val shape = dynTuple2Shape
      }
  }
}

private object Change {
  def apply[E <: AbstractTable[_]](change: DynData[E, _]): Change[E] = change match {
    case change@DynData(currentColTran, currentValue) =>
      import change._
      val colunm: E => Tuple1[Rep[DataType]] = (table: E) => Tuple1(currentColTran(table))
      val value =  Tuple1(currentValue)
      implicit val dynTuple1Shape = new TupleShape[FlatShapeLevel, Tuple1[Rep[DataType]], Tuple1[DataType], Tuple1[Rep[DataType]]](dynShape)
      new Change[E] {
        type ColType = Tuple1[Rep[DataType]]
        type ValType = Tuple1[DataType]
        val col = colunm
        val data = value
        val shape = dynTuple1Shape
      }
  }
}

object DynUpdate extends DynUpdate
