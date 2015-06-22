package org.xarcher.summer

import slick.ast.BaseTypedType
import slick.collection.heterogeneous._
import slick.collection.heterogeneous.syntax._
import slick.driver.JdbcDriver.api._

/**
 * Created by djx314 on 15-6-22.
 */

sealed trait DynData[E, U, T] {
  type DataType = T
}

case class DynBase[E, U, T](colTra: E => Rep[T], value: T)(implicit val typedType: BaseTypedType[T]) extends DynData[E, U, T]

case class DynOpt[E, U, T](colTra: E => Rep[Option[T]], value: Option[T])(implicit val typedType: BaseTypedType[T]) extends DynData[E, U, Option[T]]

trait DynUpdate {

  private def dynUpdateAction[E, U, ColType <: Product, ValType <: Product, Level <: FlatShapeLevel](baseQuery: Query[E, U, Seq])(dataList: List[DynData[E, U, _]])(hColunms: E => ColType)(hValues: ValType)(implicit shape: Shape[Level, ColType, ValType, ColType]): DBIOAction[Int, NoStream, Effect.Write] = {

    dataList.headOption match {

      case Some(change@DynBase(currentColTran, currentValue)) => {
        import change._
        val colunmHList: E => (ColType, Rep[DataType]) = (table: E) => hColunms(table) -> currentColTran(table)
        dynUpdateAction(baseQuery)(dataList.tail)(colunmHList)(hValues -> currentValue)
      }

      case Some(change@DynOpt(currentColTran, currentValue)) => {
        import change._
        val colunmHList: E => (ColType, Rep[DataType]) = (table: E) => hColunms(table) -> currentColTran(table)
        dynUpdateAction(baseQuery)(dataList.tail)(colunmHList)(hValues -> currentValue)
      }

      case _ =>
        baseQuery.map(s => { println(hColunms(s)); hColunms(s) }).update(hValues)

    }

  }

  def update[E, U](baseQuery: Query[E, U, Seq])(dataList: List[DynData[E, U, _]]): DBIOAction[Int, NoStream, Effect.Write] = {

    dataList.head match {

      case change@DynBase(currentColTran, currentValue) => {
        import change._
        val colunmHList: E => Tuple1[Rep[DataType]] = (table: E) => Tuple1(currentColTran(table))
        dynUpdateAction(baseQuery)(dataList.tail)(colunmHList)(Tuple1(currentValue))
      }

      case change@DynOpt(currentColTran, currentValue) => {
        import change._
        val colunmHList: E => Tuple1[Rep[DataType]] = (table: E) => Tuple1(currentColTran(table))
        dynUpdateAction(baseQuery)(dataList.tail)(colunmHList)(Tuple1(currentValue))
      }

    }

  }

}

object DynUpdate extends DynUpdate