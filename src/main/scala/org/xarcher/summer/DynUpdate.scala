package org.xarcher.summer

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

  private def dynUpdateAction[E <: AbstractTable[_], ColType <: Product, ValType <: Product](baseQuery: Query[E, _, Seq])(dataList: List[DynData[E, _]])(hColunms: E => ColType)(hValues: ValType)(implicit shape: Shape[FlatShapeLevel, ColType, ValType, ColType]): DBIOAction[Int, NoStream, Effect.Write] = {

    dataList.headOption match {

      case Some(change@DynData(currentColTran, currentValue)) =>
        import change._
        val colunmHList: E => (Rep[DataType], ColType) = (table: E) => currentColTran(table) -> hColunms(table)
        implicit val dynTuple2Shape = new TupleShape[FlatShapeLevel, (Rep[DataType], ColType), (DataType, ValType), (Rep[DataType], ColType)](dynShape, shape)
        dynUpdateAction(baseQuery)(dataList.tail)(colunmHList)(currentValue -> hValues)

      case _ => baseQuery.map(s => hColunms(s)).update(hValues)

    }

  }

  def update[E <: AbstractTable[_]](baseQuery: Query[E, _, Seq])(dataList: List[DynData[E, _]]): DBIOAction[Int, NoStream, Effect.Write] = {

    dataList.headOption match {

      case Some(change@DynData(currentColTran, currentValue)) =>
        import change._
        val colunmHList: E => Tuple1[Rep[DataType]] = (table: E) => Tuple1(currentColTran(table))
        implicit val dynTuple1Shape = new TupleShape[FlatShapeLevel, Tuple1[Rep[DataType]], Tuple1[DataType], Tuple1[Rep[DataType]]](dynShape)
        dynUpdateAction(baseQuery)(dataList.tail)(colunmHList)(Tuple1(currentValue))
      case _ => DBIO.successful(0)
    }

  }

}

object DynUpdate extends DynUpdate
