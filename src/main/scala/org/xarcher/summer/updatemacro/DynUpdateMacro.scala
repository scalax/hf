package org.xarcher.summer.updatemacro

import org.xarcher.summer.DynamicUpdateChange

import slick.lifted._
import slick.dbio._
import scala.language.higherKinds
import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

/**
 * Created by djx314 on 15-6-22.
 */

object UpdateAction {
  def tran[E <: AbstractTable[_], F[_]](updateInfo: (Query[E, _, F], Option[DynamicUpdateChange[E]])): DBIOAction[Int, NoStream, Effect.Write] = macro tranImpl[E, F]
  def tranImpl[E <: AbstractTable[_], F[_]](c: Context)(updateInfo: c.Expr[(Query[E, _, F], Option[DynamicUpdateChange[E]])]): c.Expr[DBIOAction[Int, NoStream, Effect.Write]] = {
    import c.universe._
    c.Expr[DBIOAction[Int, NoStream, Effect.Write]](
      q"""{
        val query = ${updateInfo}._1
        val changesOpt = ${updateInfo}._2
        lazy val zeroDBIO = DBIO.successful(0): slick.dbio.DBIOAction[Int, slick.dbio.NoStream, slick.dbio.Effect.Write]
        changesOpt.fold(zeroDBIO)(changes => {
          query.map(changes.col)(changes.shape).update(changes.data)
        })
      }""")
  }
}