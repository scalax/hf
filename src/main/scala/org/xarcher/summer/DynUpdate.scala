package org.xarcher.summer

import slick.driver.JdbcProfile

import scala.language.existentials
import slick.lifted._
import slick.dbio._
import scala.language.higherKinds

/**
 * Created by djx314 on 15-6-22.
 */

case class DynData[E <: AbstractTable[_], U](colTra: E => Rep[U], value: U)(implicit val dynShape: Shape[_ <: ShapeLevel, Rep[U], U, Rep[U]]) {
  type DataType = U
}

trait DynUpdate {

  private def changHead[E <: AbstractTable[_]](change: DynData[E, _]): DynamicUpdateChange[E] = change match {
    case change@DynData(currentColTran, currentValue) =>
      import change._
      val colunm: E => Tuple1[Rep[DataType]] = (table: E) => Tuple1(currentColTran(table))
      val value =  Tuple1(currentValue)
      implicit val dynTuple1Shape = new TupleShape[FlatShapeLevel, Tuple1[Rep[DataType]], Tuple1[DataType], Tuple1[Rep[DataType]]](dynShape)
      new DynamicUpdateChange[E] {
        type ColType = Tuple1[Rep[DataType]]
        type ValType = Tuple1[DataType]
        val col = colunm
        val data = value
        val shape = dynTuple1Shape
      }
  }

  def update[E <: AbstractTable[_]](dataList: List[DynData[E, _]]): Option[DynamicUpdateChange[E]] = {
    dataList match {
      case change :: tail =>
        Option(tail.foldLeft(changHead(change)) { (r, c) =>
          r.append(c)
        })
      case Nil => None
    }
  }

}

trait DynamicUpdateChange[E <: AbstractTable[_]] {
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
      new DynamicUpdateChange[E] {
        type ColType = NewColType
        type ValType = NewValType
        val col = colunm
        val data = value
        val shape = dynTuple2Shape
      }
  }

}

object DynUpdate extends DynUpdate