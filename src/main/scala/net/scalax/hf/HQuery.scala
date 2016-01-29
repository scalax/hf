package net.scalax.hf

import slick.dbio.DBIO
import slick.driver.JdbcActionComponent
import slick.lifted.Query

trait HQuery {

  type E
  type U
  val query: Query[E, U, Seq]
  val value: U
  def update(implicit convert: Query[E, U, Seq] => JdbcActionComponent#UpdateActionExtensionMethods[U]): DBIO[Int] = {
    if (value == (()))
      DBIO.successful(0)
    else
      convert(query).update(value)
  }
  def insert(implicit convert: Query[E, U, Seq] => JdbcActionComponent#InsertActionExtensionMethods[U]): DBIO[Int] = {
    if (value == (()))
      DBIO.successful(0)
    else
      convert(query).+=(value)
  }

}