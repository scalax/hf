package org.xarcher.summer.updatemacro

import org.xarcher.summer.{DynData, UpdateInfoContent}

import slick.lifted._
import scala.language.higherKinds
import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

/**
 * Created by djx314 on 15-6-22.
 */

/*object UpdateAction {
  def tran[E <: AbstractTable[_], F[_]](updateBuilder: UpdateBuilder[E, F]): DBIOAction[Int, NoStream, Effect.Write] = macro tranImpl[E, F]
  def tranImpl[E <: AbstractTable[_], F[_]](c: Context)(updateBuilder: c.Expr[UpdateBuilder[E, F]]): c.Expr[DBIOAction[Int, NoStream, Effect.Write]] = {
    import c.universe._
    c.Expr[DBIOAction[Int, NoStream, Effect.Write]](
      q"""{
        val query = ${updateBuilder}.query
        val changesOpt = _root_.org.xarcher.summer.DynUpdate.update(${updateBuilder}.changes)
        lazy val zeroDBIO = _root_.slick.dbio.DBIO.successful(0): _root_.slick.dbio.DBIOAction[Int, _root_.slick.dbio.NoStream, _root_.slick.dbio.Effect.Write]
        changesOpt.fold(zeroDBIO)(changes => {
          query.map(changes.col)(changes.shape).update(changes.data)
        })
      }""")
  }
}*/

/*object UpdateQuery {

  def apply[E <: AbstractTable[_], F[_]](query: Query[E, _, F]): UpdateInfoContent[E, F] = macro applyImpl[E, F]
  def applyImpl[E <: AbstractTable[_], F[_]](c: Context)(query: c.Expr[Query[E, _, F]]): c.Expr[UpdateInfoContent[E, F]] = {
    import c.universe._
    c.Expr[UpdateInfoContent[E, F]](
      q"""{
        val updateExtensionImpl = new _root_.org.xarcher.summer.UpdateActionExtensionMethodsImpl {
          def transaform[U, F[_]](query: _root_.slick.lifted.Query[_, U, F]): _root_.slick.driver.JdbcActionComponent#UpdateActionExtensionMethodsImpl[U] = {
            query: _root_.slick.driver.JdbcActionComponent#UpdateActionExtensionMethodsImpl[U]
          }
        }
        _root_.org.xarcher.summer.UpdateInfoContent($query)(updateExtensionImpl)
      }""")
  }

  def withChanges[E <: AbstractTable[_], F[_]](query: Query[E, _, F], dataList: List[DynData[E, _]]): UpdateInfoContent[E, F] = macro withChangesImpl[E, F]
  def withChangesImpl[E <: AbstractTable[_], F[_]](c: Context)(query: c.Expr[Query[E, _, F]], dataList: c.Expr[List[DynData[E, _]]]): c.Expr[UpdateInfoContent[E, F]] = {
    import c.universe._
    c.Expr[UpdateInfoContent[E, F]](
      q"""{
        val updateExtensionImpl = new _root_.org.xarcher.summer.UpdateActionExtensionMethodsImpl {
          def transaform[U, F[_]](query: _root_.slick.lifted.Query[_, U, F]): _root_.slick.driver.JdbcActionComponent#UpdateActionExtensionMethodsImpl[U] = {
            query: _root_.slick.driver.JdbcActionComponent#UpdateActionExtensionMethodsImpl[U]
          }
        }
        _root_.org.xarcher.summer.UpdateInfoContent.withChanges($query, $dataList)(updateExtensionImpl)
      }""")
  }

}*/
