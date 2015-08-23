package org.xarcher.summer

import slick.driver.{JdbcActionComponent, JdbcProfile}

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

trait UpdateActionExtensionMethodsImpl {

  def transaform[U, F[_]](query: Query[_, U, F]): JdbcActionComponent#UpdateActionExtensionMethodsImpl[U]

}

trait UpdateInfoContent[E <: AbstractTable[_], F[_]] {

  val query: Query[E, _, F]
  val dataList: List[DynData[E, _]]
  val updateExtensionImpl: UpdateActionExtensionMethodsImpl

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

  def change[T](col: E => Rep[T], value: T)
               (implicit dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) = {
    val data = DynData(col, value)
    val subQuery = query
    val subDataList = data :: dataList
    val subImpl = updateExtensionImpl
    new UpdateInfoContent[E, F] {
      override val query = subQuery
      override val dataList = subDataList
      override val updateExtensionImpl = subImpl
    }
  }

  def changeIf[T](b: Boolean)(col: E => Rep[T], value: T)
                 (implicit dynShape: Shape[_ <: ShapeLevel, Rep[T], T, Rep[T]]) = {
    if(b) change(col, value) else this
  }

  def result: DBIOAction[Int, NoStream, Effect.Write] = {
    dataList match {
      case change :: tail =>
        val changes = tail.foldLeft(changHead(change))({ (r, c) =>
          r.append(c)
        })
        import changes._
        val repsQuery = query.map(col(_))(shape)
        updateExtensionImpl.transaform(repsQuery).update(data)
      case Nil => DBIO successful 0
    }
  }

}

object UpdateInfoContent {

  def apply[E <: AbstractTable[_], F[_]](initQuery: Query[E, _, F], extensionImpl: UpdateActionExtensionMethodsImpl) = {
    new UpdateInfoContent[E, F] {
      override val query = initQuery
      val dataList = Nil
      val updateExtensionImpl = extensionImpl
    }
  }

}

private trait DynamicUpdateChange[E <: AbstractTable[_]] {
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
      new DynamicUpdateChange[E] {
        type ColType = NewColType
        type ValType = NewValType
        val col = colunm
        val data = value
        val shape = dynTuple2Shape
      }
  }

}

/*trait DynUpdate {

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
object DynUpdate extends DynUpdate*/